import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RoomModel } from '../../core/models/room/RoomModel';
import { ApiResponse } from '../../core/models/api/ApiResponse';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RoomService {
  private apiUrl = `${environment.apiUrl}/rooms`;

  constructor(private http: HttpClient) {}

  getAllRooms(): Observable<ApiResponse<RoomModel[]>> {
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
    if(confirm("Are you sure you want to create this room?")) {
      return this.http.post<ApiResponse<RoomModel>>(this.apiUrl, room);
    }
    throw new Error("Room creation cancelled by user.");
  }

  editRoom(id: number, room: RoomModel): Observable<ApiResponse<RoomModel>> {
    if(confirm("Are you sure you want to update this room?")){
      return this.http.put<ApiResponse<RoomModel>>(`${this.apiUrl}/${id}`, room);
    }
    throw new Error("Room update cancelled by user.");
  }

  deleteRoom(id: number): Observable<ApiResponse<void>> {
    if (!confirm("Are you sure you want to delete this room?")) {
      return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
    }
    throw new Error("Room deletion cancelled by user.");
  }
}
