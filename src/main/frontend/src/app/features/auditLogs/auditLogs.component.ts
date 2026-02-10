import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {AuditLogsModel} from './auditLogs.model';
import {ApiResponse} from '../../core/models/api/ApiResponse';
import {AuditLogService} from './auditLogs.service';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './auditLogs.component.html',
  styleUrls: ['./auditLogs.component.css']
})
export class AuditLogsComponent implements OnInit {
  logs: AuditLogsModel[] = [];

  constructor(
    private auditLogService: AuditLogService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadLogs();
  }

  loadLogs() {
    this.auditLogService.getLogs().subscribe({
      next: (res: ApiResponse<AuditLogsModel[]>) => {
        if (res.success && res.data) {
          this.logs = [...res.data];
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Log loading error:', err)
    });
  }
}
