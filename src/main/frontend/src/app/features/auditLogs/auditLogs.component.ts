import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import {environment} from '../../../environments/environment';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './auditLogs.component.html',
  styleUrls: ['./auditLogs.component.css']
})
export class AuditLogsComponent implements OnInit {
  logs: any[] = [];
  private readonly API_URL = `${environment.apiUrl}/audit-logs`;

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
