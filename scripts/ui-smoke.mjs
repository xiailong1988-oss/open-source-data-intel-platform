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
  { name: 'desktop-1600', width: 1600, height: 1000 },
  { name: 'desktop-2880', width: 2880, height: 1620 },
]

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

    for (const viewport of viewports) {
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

      for (const route of routes) {
        console.log(`[ui-smoke] ${viewport.name} -> ${route.path}`)
        await page.goto(`${baseURL}${route.path}`, { waitUntil: 'networkidle' })
        await page.locator(route.selector).first().waitFor({ timeout: 15000 })
        await page.waitForTimeout(300)

        if (route.slug === 'dashboard') {
          await page.locator('.haidian-cockpit__chrome-actions .el-button').first().click()
          await page.locator('.platform-layout__aside--hidden').waitFor({ timeout: 10000 })
          await page.locator('.haidian-cockpit__chrome-actions .el-button').first().click()
          await page.locator('.platform-layout__aside--hidden').waitFor({ state: 'detached', timeout: 10000 })
          await page.locator('.haidian-cockpit__signal-chip').first().click()
          await page.locator('.haidian-cockpit__toolbar-button').nth(2).click()
          await page.locator('.haidian-cockpit__option-button').nth(1).click()
          await page.locator('.intel-rail__stream-item').first().click()
          await page.locator('.intel-rail__focus-card').first().waitFor({ timeout: 10000 })
          await page.locator('.intel-rail__actions .el-button--primary').first().click()
          await page.waitForURL(/\/knowledge-build\/event-management|\/analysis-warning\/records|\/smart-search|\/knowledge-build\/entity-management/)
          await page.goBack({ waitUntil: 'networkidle' })
          await page.locator('.cockpit-map').first().waitFor({ timeout: 10000 })
        }

        if (route.slug === 'smart-search') {
          await page.locator('.result-card__title-link').first().click()
          await page.locator('.el-drawer__header').filter({ hasText: '检索结果详情' }).first().waitFor({ timeout: 10000 })
          await page.locator('.el-drawer__close-btn').first().click()
          await page.locator('.result-card').first().waitFor({ timeout: 10000 })
        }

        if (route.slug === 'topic-economy' || route.slug === 'topic-innovation') {
          await page.locator('.spatial-card__headline-item').first().click()
          await page.locator('.support-card__item').first().click()
          await page.locator('.question-item').first().waitFor({ timeout: 10000 })
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
      await page.goto(`${baseURL}/dashboard`, { waitUntil: 'networkidle' })
      await page.locator('.side-menu .el-menu-item', { hasText: '智能检索' }).click()
      await page.waitForURL(/\/smart-search/)
      await page.locator('.side-menu .el-menu-item', { hasText: '综合态势' }).click()
      await page.waitForURL(/\/dashboard/)
      await page.locator('.side-menu .el-menu-item', { hasText: '数据接入' }).click()
      await page.waitForURL(/\/data-access\/source-management/)
      await page.locator('.side-menu .el-menu-item', { hasText: '智能问答' }).click()
      await page.waitForURL(/\/smart-qa/)
      await page.locator('.side-menu .el-menu-item', { hasText: '报告中心' }).click()
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
