import {ReservationModel} from './reservation.model';

export interface ReservationGridResponse {
  [roomId: number]: ReservationModel[];
}
