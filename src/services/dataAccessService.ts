import { collectionTaskLogMockMap, collectionTaskMockList, dataSourceMockList } from '../mock/dataAccess'
import { sourceCatalogMockList, sourceHealthMockList, sourceRefreshLogMockList } from '../mock/sourceCenter'
import type { CollectionTaskItem, DataSourceItem, TaskLogEntry } from '../types/dataAccess'
import type { SourceCatalogItem, SourceHealthItem, SourceRefreshLogItem } from '../types/sourceCenter'
import { mockRequest } from './mockRequest'

/** 数据接入域保留一层读取服务，用于承接信源、任务和健康数据。 */
export const getDataSources = () => mockRequest<DataSourceItem[]>(dataSourceMockList)

export const getCollectionTasks = () => mockRequest<CollectionTaskItem[]>(collectionTaskMockList)

export const getCollectionTaskLogs = (taskId: string) =>
  mockRequest<TaskLogEntry[]>(() => collectionTaskLogMockMap[taskId] ?? [])

export const getSourceCatalog = () => mockRequest<SourceCatalogItem[]>(sourceCatalogMockList)

export const getSourceHealthList = () => mockRequest<SourceHealthItem[]>(sourceHealthMockList)

export const getSourceRefreshLogs = () => mockRequest<SourceRefreshLogItem[]>(sourceRefreshLogMockList)
