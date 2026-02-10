import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';
import {LoginRequest} from '../../features/login/login.request';
import {ApiResponse} from '../models/api/ApiResponse';

interface DecodedToken {
  sub: string;
  roles?: string[];
  role?: string;
  exp: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}`;

  private userRoleSubject = new BehaviorSubject<string>(this.getUserRoleFromToken());
  userRole$ = this.userRoleSubject.asObservable();

  private currentUserSubject = new BehaviorSubject<DecodedToken | null>(this.getDecodedToken());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.API_URL}/users`, credentials).pipe(
      tap((res: ApiResponse<string>) => this.handleToken(res))
    );
  }

  register(registrationData: any): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.API_URL}/guests`, registrationData).pipe(
      tap((res: ApiResponse<string>) => this.handleToken(res))
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

  getUserRole(): string {
    return this.userRoleSubject.value;
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  get currentUserValue(): DecodedToken | null {
    return this.currentUserSubject.value;
  }

  logout(): void {
    sessionStorage.removeItem('token');
    this.userRoleSubject.next('ANONYMOUS');
    this.currentUserSubject.next(null);
  }

  private handleToken(res: ApiResponse<string>): void {
    if (res.success && res.data) {
      const token = res.data;
      sessionStorage.setItem('token', token);

      const role = this.getUserRoleFromToken();
      this.userRoleSubject.next(role);

      const decoded = this.decodeToken(token);
      this.currentUserSubject.next(decoded);
    }
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

  private decodeToken(token: string): DecodedToken | null {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      console.error('Token decode error:', e);
      return null;
    }
  }

  private getDecodedToken(): DecodedToken | null {
    const token = this.getToken();
    return token ? this.decodeToken(token) : null;
  }
}
