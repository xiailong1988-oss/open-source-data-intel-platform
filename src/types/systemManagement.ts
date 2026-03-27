export type UserStatus = '启用' | '停用'
export type UserRole = '平台管理员' | '分析师' | '审计员' | '运维工程师' | '业务主管'

export interface SystemUserItem {
  id: string
  username: string
  realName: string
  role: UserRole
  department: string
  status: UserStatus
  createdAt: string
  phone: string
  email: string
  reserved?: Record<string, unknown>
}

export interface PermissionNode {
  id: string
  label: string
  children?: PermissionNode[]
}

export interface RolePermissionItem {
  id: string
  roleName: string
  roleCode: string
  description: string
  userCount: number
  updatedAt: string
  permissions: string[]
  reserved?: Record<string, unknown>
}

export type PlatformTheme = '政务蓝' | '深海蓝' | '银灰白'

export interface SystemConfigModel {
  platformName: string
  defaultTheme: PlatformTheme
  dataRetentionDays: number
  modelPlaceholder: string
  retrievalPlaceholder: string
  warningThreshold: number
  logRetentionDays: number
  notificationEmail: string
  reserved?: Record<string, unknown>
}
