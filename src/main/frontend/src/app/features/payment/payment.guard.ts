import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ReservationService } from '../reservation/reservation.service';

export const PaymentGuard: CanActivateFn = (route, state) => {
  const reservationService = inject(ReservationService);
  const router = inject(Router);

  if (!!reservationService.getTemporaryBooking()) {
    return true;
  }

  alert("No active reservation found to pay for.");
  router.navigate(['/rooms']);
  return false;
};
