import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {ApiResponse} from '../../core/models/api/ApiResponse';
import {ReservationGridResponse, ReservationModel} from './models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private readonly apiUrl = `${environment.apiUrl}/reservations`;
  private readonly storageKey = 'pendingReservation';

  constructor(private http: HttpClient) {}

  getGridData(start: string, end: string): Observable<ApiResponse<ReservationGridResponse>> {
    return this.http.get<ApiResponse<ReservationGridResponse>>(`${this.apiUrl}/grid?start=${start}&end=${end}`);
  }

  getPublicGridData(roomId: number, start: string, end: string):  Observable<ApiResponse<ReservationGridResponse>> {
    return this.http.get<ApiResponse<ReservationGridResponse>>(`${this.apiUrl}/grid/${roomId}?start=${start}&end=${end}`);
  }

  getById(id: number): Observable<ApiResponse<ReservationModel>> {
    return this.http.get<ApiResponse<ReservationModel>>(`${this.apiUrl}/${id}`);
  }

  create(data: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(this.apiUrl, data);
  }

  update(id: number, data: any): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.apiUrl}/${id}`, data);
  }

  cancel(id: number): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(`${this.apiUrl}/${id}/cancel`, {});
  }

  delete(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.apiUrl}/${id}`);
  }

  getRooms(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${environment.apiUrl}/rooms`);
  }

  setTemporaryBooking(data: any): void {
    localStorage.setItem(this.storageKey, JSON.stringify(data));
  }

  getTemporaryBooking(): any {
    const data = localStorage.getItem(this.storageKey);
    return data ? JSON.parse(data) : null;
  }

  clearStorage(): void {
    localStorage.removeItem(this.storageKey);
  }
}
