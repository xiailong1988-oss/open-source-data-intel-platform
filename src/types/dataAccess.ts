export type AccessMethod = 'API接入' | '数据库同步' | '文件导入' | '消息订阅'
export type SourceType = '政务系统' | '业务系统' | '互联网公开数据' | '物联网设备' | '第三方平台'
export type SourceStatus = '运行中' | '已停用' | '异常'

export interface DataSourceItem {
  id: string
  name: string
  sourceType: SourceType
  accessMethod: AccessMethod
  status: SourceStatus
  owner: string
  endpoint: string
  latestCollectTime: string
  createdAt: string
  description: string
  reserved?: Record<string, unknown>
}

export type TaskType = '增量采集' | '全量同步' | '实时订阅' | '定时巡检'
export type TaskStatus = '运行中' | '已暂停' | '异常'
export type TaskResult = '成功' | '失败' | '部分成功'

export interface CollectionTaskItem {
  id: string
  taskName: string
  sourceName: string
  taskType: TaskType
  frequency: string
  latestResult: TaskResult
  status: TaskStatus
  latestRunTime: string
  records: number
  successRate: number
  reserved?: Record<string, unknown>
}

export interface TaskLogEntry {
  id: string
  time: string
  level: 'INFO' | 'WARN' | 'ERROR'
  message: string
}
