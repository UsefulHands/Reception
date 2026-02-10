import {ReceptionistModel} from './receptionist.model';

export interface SelectedReceptionist extends ReceptionistModel {
  username?: string;
  password?: string;
}
