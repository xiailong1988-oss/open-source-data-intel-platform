import * as dashboardMock from './dashboard'
import * as dashboardCockpitMock from './dashboardCockpit'
import * as dataAccessMock from './dataAccess'
import * as dataGovernanceMock from './dataGovernance'
import * as knowledgeBuildMock from './knowledgeBuild'
import * as smartSearchMock from './smartSearch'
import * as smartQaMock from './smartQa'
import * as analysisWarningMock from './analysisWarning'
import * as reportCenterMock from './reportCenter'
import * as systemManagementMock from './systemManagement'
import * as uiConfigMock from './uiConfig'
import * as topicWorkbenchMock from './topicWorkbench'
import * as briefingCenterMock from './briefingCenter'
import * as sourceCenterMock from './sourceCenter'

export const mockModuleRegistry = {
  dashboard: dashboardMock,
  dashboardCockpit: dashboardCockpitMock,
  dataAccess: dataAccessMock,
  dataGovernance: dataGovernanceMock,
  knowledgeBuild: knowledgeBuildMock,
  smartSearch: smartSearchMock,
  smartQa: smartQaMock,
  analysisWarning: analysisWarningMock,
  reportCenter: reportCenterMock,
  systemManagement: systemManagementMock,
  uiConfig: uiConfigMock,
  topicWorkbench: topicWorkbenchMock,
  briefingCenter: briefingCenterMock,
  sourceCenter: sourceCenterMock,
}

export * from './dashboard'
export * from './dashboardCockpit'
export * from './dataAccess'
export * from './dataGovernance'
export * from './knowledgeBuild'
export * from './smartSearch'
export * from './smartQa'
export * from './analysisWarning'
export * from './reportCenter'
export * from './systemManagement'
export * from './uiConfig'
export * from './topicWorkbench'
export * from './briefingCenter'
export * from './sourceCenter'
