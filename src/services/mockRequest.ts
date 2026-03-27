const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const cloneData = <T>(payload: T): T => {
  if (typeof structuredClone === 'function') {
    return structuredClone(payload)
  }

  return JSON.parse(JSON.stringify(payload)) as T
}

/**
 * 统一模拟 mock 数据请求延迟，并对返回值做深拷贝，
 * 避免页面直接持有 mock 源对象引用，后续切真实接口时只需替换这一层的实现。
 */
export const mockRequest = async <T>(payload: T | (() => T), delay = 180): Promise<T> => {
  await wait(delay)
  const data = typeof payload === 'function' ? (payload as () => T)() : payload
  return cloneData(data)
}
