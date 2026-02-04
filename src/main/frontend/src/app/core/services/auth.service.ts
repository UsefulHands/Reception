import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable, BehaviorSubject } from 'rxjs'; // 1. Bunu ekle

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/api/v1'; // 2. URL'in tam olduğundan emin ol
  private userRoleSubject = new BehaviorSubject<string>(this.getUserRoleFromToken());
  userRole$ = this.userRoleSubject.asObservable();
  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.API_URL}/users`, credentials).pipe(
      tap((res: any) => {
        this.handleToken(res);
      })
    );
  }

  register(registrationData: any): Observable<any> {
    return this.http.post(`${this.API_URL}/guests`, registrationData).pipe(
      tap((res: any) => {
        this.handleToken(res);
      })
    );
  }

  private handleToken(res: any) {
    console.log('1. Backendden Gelen Yanıt:', res);

    // Backend'den gelen nesnenin içinde 'isDeleted' veya 'active' durumu varsa kontrol edelim
    // Genelde UserDTO içinde bu bilgi gelir.
    if (res && res.isDeleted === true) {
      console.error('HATA: Bu kullanıcı hesabı pasif durumdadır (Soft Deleted).');
      this.logout();
      return;
    }

    let token = '';
    if (res && res.data) {
      token = typeof res.data === 'string' ? res.data : res.data.token;
    }

    if (token) {
      console.log('2. Token Başarıyla Ayıklandı:', token);
      sessionStorage.setItem('token', token);

      // Token içindeki payload'u çözüp isDeleted kontrolü yapalım (Eğer Claim olarak eklediysen)
      const payload = this.decodeToken(token);

      // EĞER Token içine 'isDeleted' claim'i eklediysen (Güvenlik için önerilir):
      if (payload && payload.isDeleted === true) {
        console.warn('Token geçerli ama kullanıcı silinmiş görünüyor!');
        this.logout();
        return;
      }

      const role = this.formatRole(payload);
      this.userRoleSubject.next(role);
    } else {
      console.error('HATA: Token bulunamadı!');
    }
  }

  private decodeToken(token: string): any {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      return null;
    }
  }

  private formatRole(payload: any): string {
    if (payload && payload.roles && payload.roles.length > 0) {
      return payload.roles[0].replace('ROLE_', '');
    }
    return 'GUEST';
  }

  private getUserRoleFromToken(): string {
    const token = sessionStorage.getItem('token');
    if (!token) return 'ANONYMOUS';

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));

      // 1. roles dizisi var mı ve içi dolu mu kontrol et
      if (payload.roles && Array.isArray(payload.roles) && payload.roles.length > 0) {
        // 2. 'ROLE_ADMIN' içindeki 'ROLE_' kısmını atıp sadece 'ADMIN' alalım (Navbar'daki kontrolüne uyması için)
        const rawRole = payload.roles[0]; // 'ROLE_ADMIN'
        return rawRole.replace('ROLE_', ''); // Sonuç: 'ADMIN'
      }

      return 'GUEST';
    } catch (e) {
      return 'ANONYMOUS';
    }
  }

  getUserRole(): string {
    return this.getUserRoleFromToken();
  }

  getToken() {
    return sessionStorage.getItem('token');
  }

  get userRole() {
    const token = sessionStorage.getItem('token');
    if (!token) return 'ANONYMOUS';

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role;
    } catch (e) {
      return 'ANONYMOUS';
    }
  }
  logout() {
    sessionStorage.removeItem('token');
    this.userRoleSubject.next('ANONYMOUS');
  }
}
