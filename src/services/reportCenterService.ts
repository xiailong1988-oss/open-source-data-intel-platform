import { briefingListMock, briefingTemplateMockList } from '../mock/briefingCenter'
import { reportDetailMockMap, reportListMock } from '../mock/reportCenter'
import type { BriefingListItem, BriefingTemplateItem } from '../types/briefingCenter'
import type { ReportDetail, ReportListItem } from '../types/reportCenter'
import { mockRequest } from './mockRequest'

/** 报告与简报先统一收口到同一服务层，便于后续接入报告域真实接口。 */
export const getReportList = () => mockRequest<ReportListItem[]>(reportListMock)

export const getReportDetail = (id: string) =>
  mockRequest<ReportDetail | null>(() => reportDetailMockMap[id] ?? null)

export const getBriefingList = () => mockRequest<BriefingListItem[]>(briefingListMock)

export const getBriefingTemplates = () => mockRequest<BriefingTemplateItem[]>(briefingTemplateMockList)
