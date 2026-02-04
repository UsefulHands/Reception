import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './auditLogs.component.html'
})
export class AuditLogsComponent implements OnInit {
  logs: any[] = [];
  private readonly API_URL = 'http://localhost:8080/api/v1/audit-logs';

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadLogs();
  }

  loadLogs() {
    this.http.get<any>(this.API_URL).subscribe({
      next: (res) => {
        this.logs = res.data || [];
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Log loading error:', err)
    });
  }
}
