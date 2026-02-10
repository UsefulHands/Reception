export interface AuditLogsModel {
  id?: number;
  action: string;
  entityType: string;
  entityId: number;
  performedBy: string;
  timestamp: Date;
  details?: string;
  ipAddress?: string;
}
