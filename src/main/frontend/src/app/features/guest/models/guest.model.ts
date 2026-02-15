export interface GuestModel {
  id: number;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  identityNumber: string;
  createdAt: string | Date;
  userId?: number;
}

export interface GuestRegistrationRequest {
  guestDto: {
    firstName: string;
    lastName: string;
    phoneNumber: string;
    identityNumber: string;
  };
  username: string;
  password: string;
}

export interface GuestUpdateRequest {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  identityNumber: string;
}
