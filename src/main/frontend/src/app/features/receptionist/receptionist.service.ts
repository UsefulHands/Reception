import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReceptionistModel } from './receptionist.model';
import { ReceptionistRegistrationRequest } from './receptionist.registration.request';
import {ApiResponse} from '../../core/models/api/ApiResponse';

@Injectable({
  providedIn: 'root'
})
export class ReceptionistService {
  private readonly apiUrl = `${environment.apiUrl}/receptionists`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<ReceptionistModel[]>> {
    return this.http.get<ApiResponse<ReceptionistModel[]>>(this.apiUrl);
  }

  create(data: ReceptionistRegistrationRequest): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(this.apiUrl, data);
  }

  update(id: number, data: ReceptionistModel): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/${id}`);
  }
}
