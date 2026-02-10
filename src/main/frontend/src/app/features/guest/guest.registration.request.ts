import {GuestModel} from './guest.model';

export interface GuestRegistrationRequest {
  guestDetails: GuestModel;
  username: string;
  password: string;
}
