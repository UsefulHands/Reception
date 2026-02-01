import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = 'api/v1';

  constructor(private http: HttpClient) {}

  login(credentials: any) {
    return this.http.post(`${this.API_URL}/users`, credentials).pipe(
      tap((res: any) => {
        if (res && res.token) {
          localStorage.setItem('token', res.token);
        } else if (typeof res === 'string') {
          localStorage.setItem('token', res);
        }
      })
    );
  }

  getToken() {
    return localStorage.getItem('token');
  }
}
