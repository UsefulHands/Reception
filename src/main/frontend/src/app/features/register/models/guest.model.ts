export interface GuestDetails {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  identityNumber: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  guestDetails: GuestDetails;
}
