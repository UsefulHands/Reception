import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable, BehaviorSubject } from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}`;

  private userRoleSubject = new BehaviorSubject<string>(this.getUserRoleFromToken());
  userRole$ = this.userRoleSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.API_URL}/users`, credentials).pipe(
      tap((res: any) => this.handleToken(res))
    );
  }

  register(registrationData: any): Observable<any> {
    return this.http.post(`${this.API_URL}/guests`, registrationData).pipe(
      tap((res: any) => this.handleToken(res))
    );
  }

  hasRole(role: string): boolean {
    return this.getUserRole() === role;
  }

  hasAnyRole(roles: string[]): boolean {
    const currentRole = this.getUserRole();
    return roles.includes(currentRole);
  }

  isLoggedIn(): boolean {
    return !!this.getToken() && this.getUserRole() !== 'ANONYMOUS';
  }

  private handleToken(res: any) {
    let token = '';
    // Backend ApiResponse yapısına göre data içinden tokenı alıyoruz
    if (res && res.data) {
      token = typeof res.data === 'string' ? res.data : res.data.token;
    }

    if (token) {
      sessionStorage.setItem('token', token);
      const role = this.getUserRoleFromToken();
      this.userRoleSubject.next(role); // Navbar ve componentleri haberdar et
    }
  }

  getUserRole(): string {
    return this.userRoleSubject.value;
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  private getUserRoleFromToken(): string {
    const token = this.getToken();
    if (!token) return 'ANONYMOUS';

    try {
      const payload = this.decodeToken(token);
      if (!payload) return 'ANONYMOUS';

      if (payload.roles && Array.isArray(payload.roles) && payload.roles.length > 0) {
        return payload.roles[0].replace('ROLE_', '');
      }

      if (payload.role) {
        return payload.role.replace('ROLE_', '');
      }

      return 'GUEST';
    } catch (e) {
      return 'ANONYMOUS';
    }
  }

  private decodeToken(token: string): any {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      console.error('Token decode error:', e);
      return null;
    }
  }

  logout() {
    sessionStorage.removeItem('token');
    this.userRoleSubject.next('ANONYMOUS');
  }
}
