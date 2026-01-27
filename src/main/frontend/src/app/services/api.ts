import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http'; // Bunu eklemeyi unutma
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Api {

  private baseUrl = '/api';

  constructor(private http: HttpClient) { }

  getMesaj() {
    return this.http.get(`${this.baseUrl}/merhaba`, { responseType: 'text' });
  }
}
