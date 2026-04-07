import { chromium } from 'playwright'
import { mkdir, rm } from 'node:fs/promises'
import { existsSync } from 'node:fs'
import { join } from 'node:path'
import { spawn, execFileSync } from 'node:child_process'

const cwd = process.cwd()
const baseURL = process.env.SMOKE_BASE_URL ?? 'http://127.0.0.1:4173'
const outputDir = join(cwd, 'test-results', 'ui-regression')
const isWindows = process.platform === 'win32'
const shouldStartServer = process.env.SMOKE_SKIP_SERVER !== '1'

const routes = [
  { slug: 'dashboard', path: '/dashboard', selector: '.cockpit-map' },
  { slug: 'topic-economy', path: '/topic-workbench/economy', selector: '.topic-workbench' },
  { slug: 'topic-innovation', path: '/topic-workbench/innovation', selector: '.topic-workbench' },
  { slug: 'smart-search', path: '/smart-search', selector: '.smart-search' },
  { slug: 'smart-qa', path: '/smart-qa', selector: '.smart-qa' },
  { slug: 'source-intelligence', path: '/data-access/source-intelligence', selector: '.source-intelligence' },
  { slug: 'data-health', path: '/data-access/data-health', selector: '.data-health' },
  { slug: 'warning-overview', path: '/analysis-warning/overview', selector: '.warning-overview' },
  { slug: 'warning-records', path: '/analysis-warning/records', selector: '.warning-records' },
  { slug: 'report-center', path: '/report-center/reports', selector: '.report-center' },
  { slug: 'briefing-center', path: '/report-center/briefings', selector: '.briefing-center' },
]

const viewports = [
  { name: 'desktop-1366', width: 1366, height: 900 },
  { name: 'desktop-1440', width: 1440, height: 960 },
  { name: 'desktop-1600', width: 1600, height: 1000 },
  { name: 'desktop-2880', width: 2880, height: 1620 },
]

const requestedRouteSlugs = process.env.SMOKE_ROUTES?.split(',').map((item) => item.trim()).filter(Boolean) ?? []
const requestedViewportNames = process.env.SMOKE_VIEWPORTS?.split(',').map((item) => item.trim()).filter(Boolean) ?? []
const activeRoutes = requestedRouteSlugs.length ? routes.filter((route) => requestedRouteSlugs.includes(route.slug)) : routes
const activeViewports = requestedViewportNames.length ? viewports.filter((viewport) => requestedViewportNames.includes(viewport.name)) : viewports

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

async function waitForServer(url, timeoutMs = 30000) {
  const start = Date.now()

  while (Date.now() - start < timeoutMs) {
    try {
      const response = await fetch(url)
      if (response.ok) {
        return
      }
    } catch {
      // ignore until server is ready
    }

    await wait(500)
  }

  throw new Error(`开发服务未在 ${timeoutMs}ms 内启动：${url}`)
}

async function run() {
  await rm(outputDir, { recursive: true, force: true })
  await mkdir(outputDir, { recursive: true })

  const viteProcess = shouldStartServer
    ? isWindows
      ? spawn(
          'cmd.exe',
          ['/c', 'npm', 'run', 'dev', '--', '--host', '127.0.0.1', '--port', '4173', '--strictPort'],
          {
            cwd,
            env: { ...process.env, BROWSER: 'none' },
            stdio: ['ignore', 'pipe', 'pipe'],
          },
        )
      : spawn('npm', ['run', 'dev', '--', '--host', '127.0.0.1', '--port', '4173', '--strictPort'], {
          cwd,
          env: { ...process.env, BROWSER: 'none' },
          stdio: ['ignore', 'pipe', 'pipe'],
        })
    : null

  let serverLog = ''
  viteProcess?.stdout.on('data', (chunk) => {
    serverLog += chunk.toString()
  })
  viteProcess?.stderr.on('data', (chunk) => {
    serverLog += chunk.toString()
  })

  try {
    await waitForServer(`${baseURL}/dashboard`)

    const browser = await chromium.launch({ headless: true })
    const diagnostics = []

    for (const viewport of activeViewports) {
      const context = await browser.newContext({ viewport })
      const page = await context.newPage()

      page.on('pageerror', (error) => {
        diagnostics.push(`[pageerror][${viewport.name}] ${error.message}`)
      })

      page.on('response', (response) => {
        if (response.status() >= 500 && response.url().startsWith(baseURL)) {
          diagnostics.push(`[response:${response.status()}][${viewport.name}] ${response.url()}`)
        }
      })

      for (const route of activeRoutes) {
        console.log(`[ui-smoke] ${viewport.name} -> ${route.path}`)
        await page.goto(`${baseURL}${route.path}`, { waitUntil: 'domcontentloaded' })
        await page.locator(route.selector).first().waitFor({ timeout: 15000 })
        await page.waitForTimeout(300)

        if (route.slug === 'dashboard') {
          await page.locator('.situation-region-switcher').first().waitFor({ timeout: 10000 })
          await page.locator('.situation-live-ticker').first().waitFor({ timeout: 10000 })
          await page.locator('.region-summary-panel').first().waitFor({ timeout: 10000 })
          await page.locator('.osint-stream-panel').first().waitFor({ timeout: 10000 })
          await page.locator('.focus-panel').first().waitFor({ timeout: 10000 })
          await page.locator('.event-list-panel').first().waitFor({ timeout: 10000 })
          await page.locator('.situation-bottom-band').first().waitFor({ timeout: 10000 })

          const stageLayout = await page.evaluate(() => {
            const mapRect = document.querySelector('.haidian-cockpit__map-stage-shell')?.getBoundingClientRect()
            const tickerRect = document.querySelector('.situation-live-ticker')?.getBoundingClientRect()
            const analyticsRect = document.querySelector('.haidian-cockpit__analytics-shell')?.getBoundingClientRect()
            const bottomRect = document.querySelector('.situation-bottom-band')?.getBoundingClientRect()

            return {
              map: mapRect
                ? { top: mapRect.top, right: mapRect.right, bottom: mapRect.bottom, left: mapRect.left, width: mapRect.width }
                : null,
              ticker: tickerRect
                ? { top: tickerRect.top, right: tickerRect.right, bottom: tickerRect.bottom, left: tickerRect.left, width: tickerRect.width, height: tickerRect.height }
                : null,
              analytics: analyticsRect
                ? { top: analyticsRect.top, right: analyticsRect.right, bottom: analyticsRect.bottom, left: analyticsRect.left, width: analyticsRect.width, height: analyticsRect.height }
                : null,
              bottom: bottomRect
                ? { top: bottomRect.top, right: bottomRect.right, bottom: bottomRect.bottom, left: bottomRect.left, width: bottomRect.width }
                : null,
              panelCount: document.querySelectorAll('.haidian-cockpit__panel-item').length,
            }
          })

          if (!stageLayout.map || !stageLayout.ticker || !stageLayout.analytics || !stageLayout.bottom) {
            throw new Error('首页四区结构未完整渲染')
          }

          if (viewport.width >= 1366) {
            if (stageLayout.ticker.top <= stageLayout.map.bottom - 8) {
              throw new Error(`首页在 ${viewport.width}px 桌面宽度下地图与快讯条层级顺序异常`)
            }

            if (stageLayout.analytics.top <= stageLayout.ticker.bottom - 8) {
              throw new Error(`首页在 ${viewport.width}px 桌面宽度下快讯条与联动面板发生重叠`)
            }

            if (stageLayout.analytics.height < 160) {
              throw new Error(`首页在 ${viewport.width}px 桌面宽度下联动面板高度过小`)
            }

            if (stageLayout.panelCount !== 4) {
              throw new Error(`首页联动面板数量异常，当前为 ${stageLayout.panelCount}`)
            }
          }

          const boardIds = await page.locator('.situation-bottom-band__item').evaluateAll((items) =>
            items.map((item) => item.getAttribute('data-board-id')),
          )
          for (const expected of [
            'industrial-development',
            'tech-innovation',
            'featured-economy',
            'public-service',
            'safety-governance',
            'urban-rural-construction',
          ]) {
            if (!boardIds.includes(expected)) {
              throw new Error(`首页底部六大板块缺少：${expected}`)
            }
          }

          await page.locator('.top-bar__layout-tools .el-button').nth(1).click()
          await page.locator('.platform-layout__aside--hidden').waitFor({ timeout: 10000 })
          await page.locator('.top-bar__layout-tools .el-button').nth(1).click()
          await page.locator('.platform-layout__aside--hidden').waitFor({ state: 'detached', timeout: 10000 })
          const initialRegionTitle = (await page.locator('.region-summary-panel__head strong').textContent())?.trim()
          await page.locator('.situation-region-switcher__chip').nth(1).click()
          await page.waitForTimeout(800)
          const nextRegionTitle = (await page.locator('.region-summary-panel__head strong').textContent())?.trim()
          const regionLabel = (await page.locator('.cockpit-map-region-label').textContent())?.trim()
          if (initialRegionTitle === nextRegionTitle || !regionLabel) {
            throw new Error('首页地区切换后未正确联动地图或区域摘要')
          }
          await page.locator('.situation-map-control__layer').nth(1).click()
          await page.locator('.situation-map-control__actions button').first().click()
          await page.waitForFunction(() => document.querySelectorAll('.map-highlight-layer__card').length > 0)
          await page.locator('.map-highlight-layer__card').first().hover()
          await page.locator('.map-highlight-layer__card').first().click({ force: true })
          await page.waitForFunction(() => location.pathname !== '/dashboard')
          await page.goBack({ waitUntil: 'domcontentloaded' })
          await page.locator('.cockpit-map').first().waitFor({ timeout: 10000 })
          await page.locator('.situation-live-ticker__item').first().click({ force: true })
          await page.waitForFunction(() => location.pathname !== '/dashboard')
          await page.goBack({ waitUntil: 'domcontentloaded' })
          await page.locator('.osint-stream-panel__detail').first().click()
          await page.waitForFunction(() => location.pathname !== '/dashboard')
          await page.goBack({ waitUntil: 'domcontentloaded' })
          await page.locator('.cockpit-map').first().waitFor({ timeout: 10000 })
          await page.locator('.focus-panel__hero strong').first().waitFor({ timeout: 10000 })
          await page.locator('.event-list-panel__item').first().click()
          await page.waitForTimeout(600)
          const focusTitle = (await page.locator('.focus-panel__hero strong').textContent())?.trim()
          if (!focusTitle) {
            throw new Error('首页事件列表点击后未正确更新焦点详情')
          }
          await page.locator('.situation-bottom-band__open').first().click({ force: true })
          await page.waitForFunction(() => location.pathname !== '/dashboard')
          await page.goBack({ waitUntil: 'domcontentloaded' })
          await page.locator('.situation-bottom-band').first().waitFor({ timeout: 10000 })
        }

        if (route.slug === 'smart-search') {
          await page.locator('.result-card__title-link').first().click()
          await page.locator('.el-drawer__header').filter({ hasText: '检索结果详情' }).first().waitFor({ timeout: 10000 })
          await page.locator('.el-drawer__close-btn').first().click()
          await page.locator('.result-card').first().waitFor({ timeout: 10000 })
        }

        if (route.slug === 'topic-economy' || route.slug === 'topic-innovation') {
          const loadingPanel = page.locator('.view-state-panel--loading')
          if (await loadingPanel.count()) {
            await loadingPanel.waitFor({ state: 'detached', timeout: 15000 }).catch(() => {})
          }
          await page.waitForTimeout(800)
          await page.locator('.topic-list__item').first().click()
          await page.waitForURL(/\/knowledge-build\/event-management/)
          await page.goBack({ waitUntil: 'domcontentloaded' })
          await page.locator('.topic-workbench').first().waitFor({ timeout: 10000 })
        }

        if (route.slug === 'smart-qa') {
          await page.locator('.history-item').first().waitFor({ timeout: 10000 })
          const secondHistory = page.locator('.history-item').nth(1)
          if (await secondHistory.count()) {
            await secondHistory.click()
          }
          await page.locator('.qa-card__header').filter({ hasText: '核心回答' }).first().waitFor({ timeout: 10000 })
          await page.locator('.qa-recommend').first().click()
          await page.locator('.qa-card__header').filter({ hasText: '依据来源' }).first().waitFor({ timeout: 10000 })
        }

        if (route.slug === 'warning-records') {
          await page.locator('.cell-link').first().click()
          await page.locator('.el-drawer__header').filter({ hasText: '预警记录详情' }).first().waitFor({ timeout: 10000 })
          await page.locator('.el-drawer__close-btn').first().click()
          await page.locator('.warning-records__list-item').first().waitFor({ timeout: 10000 })
        }

        if (route.slug === 'source-intelligence') {
          await page.locator('table .cell-link').first().click()
          await page.locator('.source-focus').first().waitFor({ timeout: 10000 })
        }

        if (route.slug === 'data-health') {
          await page.locator('table .cell-link').first().click()
          await page.locator('.health-focus').first().waitFor({ timeout: 10000 })
        }

        if (route.slug === 'briefing-center') {
          await page.locator('.template-item').first().click()
          await page.locator('.el-dialog').filter({ hasText: '新建简报' }).first().waitFor({ timeout: 10000 })
          await page.locator('.el-dialog__close').first().click()
          await page.locator('.briefing-preview').first().waitFor({ timeout: 10000 })
        }

        await page.screenshot({ path: join(outputDir, `${route.slug}-${viewport.name}.png`) })
      }

      console.log(`[ui-smoke] ${viewport.name} -> menu chain`)
      await page.goto(`${baseURL}/dashboard`, { waitUntil: 'domcontentloaded' })
      const menuItems = page.locator('.side-menu .el-menu-item')
      await menuItems.nth(4).click()
      await page.waitForURL(/\/smart-search/)
      await menuItems.nth(0).click()
      await page.waitForURL(/\/dashboard/)
      await menuItems.nth(1).click()
      await page.waitForURL(/\/data-access\/source-management/)
      await menuItems.nth(5).click()
      await page.waitForURL(/\/smart-qa/)
      await menuItems.nth(7).click()
      await page.waitForURL(/\/report-center\/reports$/)
      await page.screenshot({ path: join(outputDir, `menu-chain-${viewport.name}.png`) })

      await context.close()
    }

    await browser.close()

    if (diagnostics.length) {
      throw new Error(`UI 冒烟发现异常：\n${diagnostics.join('\n')}`)
    }

    console.log(`UI 冒烟与截图回归完成，结果目录：${outputDir}`)
  } catch (error) {
    if (serverLog) {
      console.error(serverLog)
    }
    throw error
  } finally {
    if (viteProcess) {
      if (isWindows) {
        try {
          execFileSync('taskkill', ['/pid', String(viteProcess.pid), '/t', '/f'])
        } catch {
          viteProcess.kill('SIGTERM')
        }
      } else {
        viteProcess.kill('SIGTERM')
      }
    }

    if (existsSync(join(cwd, '.vite-log.txt'))) {
      // keep existing log file untouched
    }
  }
}

run().catch((error) => {
  console.error(error)
  process.exitCode = 1
})
