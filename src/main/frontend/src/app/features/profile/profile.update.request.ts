import {UserProfile} from './user.profile';

export interface ProfileUpdateRequest extends UserProfile {
  currentPassword?: string;
  newPassword?: string;
}
