export interface ReservationModel {
  id?: number;
  roomId: number;
  guestId?: number;
  guestFirstName?: string;
  guestLastName?: string;
  roomNumber?: string;
  checkInDate: string;
  checkOutDate: string;
  status: ReservationStatus;
  totalPrice?: number;
  balance?: number;
  notes?: string;
  createdAt?: string;
  phoneNumber?: string;
  identityNumber?: string;
}

export interface ReservationGridResponse {
  [roomId: number]: ReservationModel[];
}

export enum ReservationStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  CHECKED_IN = 'CHECKED_IN',
  CHECKED_OUT = 'CHECKED_OUT',
  CANCELLED = 'CANCELLED',
  NO_SHOW = 'NO_SHOW'
}
