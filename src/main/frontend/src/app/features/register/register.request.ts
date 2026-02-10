import {GuestDetails} from './guest.details';

export interface RegisterRequest {
  username: string;
  password: string;
  guestDetails: GuestDetails;
}
