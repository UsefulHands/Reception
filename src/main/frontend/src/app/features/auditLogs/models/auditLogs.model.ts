export interface AuditLogsModel {
  id: number;
  action: string;
  performedBy: string;
  details: string;
  createdAt: string | Date;
}
