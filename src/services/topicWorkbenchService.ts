import { topicWorkbenchMockMap } from '../mock/topicWorkbench'
import type { TopicWorkbenchCode, TopicWorkbenchData } from '../types/topicWorkbench'
import { mockRequest } from './mockRequest'

/**
 * 专题工作台通过独立服务层读取数据，后续接真实专题接口时只需替换这里的实现。
 */
export const getTopicWorkbench = (topicCode: TopicWorkbenchCode) =>
  mockRequest<TopicWorkbenchData>(() => topicWorkbenchMockMap[topicCode])
