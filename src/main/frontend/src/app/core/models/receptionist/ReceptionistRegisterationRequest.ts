import {ReceptionistModel} from './ReceptionistModel';

export interface ReceptionistRegistrationRequest {
  receptionistDetails: ReceptionistModel;
  username: string;
  password: string;
}
