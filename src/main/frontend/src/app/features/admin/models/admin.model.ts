export interface AdminModel {
  id: number;
  firstName: string;
  lastName: string;
  corporateEmail: string;
  adminTitle: string;
  userId?: number;
}

export interface AdminRegistrationRequest {
  adminDto: {
    firstName: string;
    lastName: string;
    corporateEmail: string;
    adminTitle: string;
  };
  username: string;
  password: string;
}

export interface AdminUpdateRequest {
  firstName: string;
  lastName: string;
  corporateEmail: string;
  adminTitle: string;
}
