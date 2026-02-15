import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {AuditLogsModel} from './models/auditLogs.model';
import {ApiResponse} from '../../core/models/api/ApiResponse';

@Injectable({
  providedIn: 'root'
})
export class AuditLogService {
  private readonly API_URL = `${environment.apiUrl}/audit-logs`;

  constructor(private http: HttpClient) {}

  getLogs(): Observable<ApiResponse<AuditLogsModel[]>> {
    return this.http.get<ApiResponse<AuditLogsModel[]>>(this.API_URL);
  }
}
