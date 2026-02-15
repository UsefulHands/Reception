export interface ReceptionistModel {
  id?: number;
  firstName: string;
  lastName: string;
  shiftType: string;
  userId?: number;
}

export interface ReceptionistRegistrationRequest {
  receptionistDetails: ReceptionistModel;
  username: string;
  password: string;
}

export interface SelectedReceptionist extends ReceptionistModel {
  username?: string;
  password?: string;
}

export const SHIFT_TYPES = ['MORNING', 'AFTERNOON', 'NIGHT'];
