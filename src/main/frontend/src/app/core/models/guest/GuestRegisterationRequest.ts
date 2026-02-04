import {GuestModel} from './GuestModel';

export interface GuestRegistrationRequest {
  guestDetails: GuestModel;
  username: string;
  password: string;
}
