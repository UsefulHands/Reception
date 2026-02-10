import {ReceptionistModel} from './receptionist.model';

export interface ReceptionistRegistrationRequest {
  receptionistDetails: ReceptionistModel;
  username: string;
  password: string;
}
