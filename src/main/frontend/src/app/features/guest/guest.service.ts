import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {ApiResponse} from '../../core/models/api/ApiResponse';
import {GuestModel, GuestUpdateRequest, GuestRegistrationRequest} from './models/guest.model';

@Injectable({
  providedIn: 'root'
})
export class GuestService {
  private readonly API_URL = `${environment.apiUrl}/guests`;

  constructor(private http: HttpClient) {}

  getGuests(): Observable<ApiResponse<GuestModel[]>> {
    return this.http.get<ApiResponse<GuestModel[]>>(this.API_URL);
  }

  createGuest(request: GuestRegistrationRequest): Observable<ApiResponse<GuestModel>> {
    return this.http.post<ApiResponse<GuestModel>>(this.API_URL, request);
  }

  updateGuest(id: number, guest: GuestUpdateRequest): Observable<ApiResponse<GuestModel>> {
    return this.http.put<ApiResponse<GuestModel>>(`${this.API_URL}/${id}`, guest);
  }

  deleteGuest(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/${id}`);
  }
}
