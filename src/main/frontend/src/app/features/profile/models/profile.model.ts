export interface PasswordData {
  currentPassword: '';
  newPassword: '';
  confirmPassword: '';
}

export interface UserProfile {
  id: number | null;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  identityNumber: string;
  userName: string;
}

export interface ProfileUpdateRequest extends UserProfile {
  currentPassword?: string;
  newPassword?: string;
}
