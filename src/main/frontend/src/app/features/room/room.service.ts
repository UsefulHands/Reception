import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {RoomModel} from './models/room.model';
import {ApiResponse} from '../../core/models/api/ApiResponse';

@Injectable({
  providedIn: 'root'
})
export class RoomService {
  private readonly apiUrl = `${environment.apiUrl}/rooms`;

  constructor(private http: HttpClient) {}

  getAllRooms(): Observable<ApiResponse<RoomModel[]>> {
    console.log(environment.apiUrl);
    return this.http.get<ApiResponse<RoomModel[]>>(this.apiUrl);
  }

  getAvailableRooms(): Observable<ApiResponse<RoomModel[]>> {
    const params = new HttpParams().set('available', 'true');
    return this.http.get<ApiResponse<RoomModel[]>>(this.apiUrl, { params });
  }

  getRoom(id: number): Observable<ApiResponse<RoomModel>> {
    return this.http.get<ApiResponse<RoomModel>>(`${this.apiUrl}/${id}`);
  }

  createRoom(room: RoomModel): Observable<ApiResponse<RoomModel>> {
    return this.http.post<ApiResponse<RoomModel>>(this.apiUrl, room);
  }

  editRoom(id: number, room: RoomModel): Observable<ApiResponse<RoomModel>> {
    return this.http.put<ApiResponse<RoomModel>>(`${this.apiUrl}/${id}`, room);
  }

  deleteRoom(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
