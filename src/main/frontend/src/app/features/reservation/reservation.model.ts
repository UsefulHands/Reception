import {ReservationStatus} from './reservation.status';

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
