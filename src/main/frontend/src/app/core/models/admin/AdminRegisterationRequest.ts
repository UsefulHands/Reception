import {AdminModel} from './AdminModel';

export interface AdminRegistrationRequest {
  adminDto: AdminModel;
  username: string;
  password: string;
}
