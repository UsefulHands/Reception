import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse} from '../../core/models/api/ApiResponse';
import { AdminRegistrationRequest } from './admin.registration.request';
import { AdminModel } from './admin.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly API_URL = `${environment.apiUrl}/admins`;

  constructor(private http: HttpClient) {}

  getAdmins(): Observable<ApiResponse<AdminModel[]>> {
    return this.http.get<ApiResponse<AdminModel[]>>(this.API_URL);
  }

  createAdmin(request: AdminRegistrationRequest): Observable<ApiResponse<AdminModel>> {
    return this.http.post<ApiResponse<AdminModel>>(this.API_URL, request);
  }

  updateAdmin(id: number, admin: AdminModel): Observable<ApiResponse<AdminModel>> {
    return this.http.put<ApiResponse<AdminModel>>(`${this.API_URL}/${id}`, admin);
  }

  deleteAdmin(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/${id}`);
  }
}
