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
