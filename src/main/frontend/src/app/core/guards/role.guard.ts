import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service'; // AuthService yolun

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const expectedRoles = route.data['roles'] as Array<string>;
  const userRole = authService.getUserRole();

  if (expectedRoles.includes(userRole)) {
    return true; // Yetki var, geçebilir
  }

  // Yetkisi yoksa ana sayfaya veya login'e şutla
  router.navigate(['/home']);
  return false;
};
