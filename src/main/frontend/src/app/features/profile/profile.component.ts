import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileService } from './profile.service';
import {PasswordData, UserProfile, ProfileUpdateRequest} from './models/profile.model';
import {ApiResponse} from '../../core/models/api/ApiResponse';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  passwordData: PasswordData = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  currentUser: UserProfile = {
    id: null,
    firstName: '',
    lastName: '',
    phoneNumber: '',
    identityNumber: '',
    userName: '',
  };

  isUpdating = false;

  constructor(
    private profileService: ProfileService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.profileService.getProfile().subscribe({
      next: (res: ApiResponse<UserProfile>) => {
        if (res.success) {
          this.currentUser = res.data;
          this.cdr.detectChanges();
        }
      },
      error: () => {
        this.router.navigate(['/login']);
      }
    });
  }

  onUpdateProfile(): void {
    if (this.passwordData.newPassword !== this.passwordData.confirmPassword) {
      alert("Passwords do not match!");
      return;
    }

    this.isUpdating = true;

    const updateRequest: ProfileUpdateRequest = {
      ...this.currentUser,
      currentPassword: this.passwordData.currentPassword,
      newPassword: this.passwordData.newPassword
    };

    this.profileService.updateProfile(updateRequest).subscribe({
      next: (res: ApiResponse<UserProfile>) => {
        if (res.success) {
          alert(res.message || 'Updated Successfully!');
          this.currentUser = res.data;
          this.resetPasswordForm();
        } else {
          alert(res.message || 'Update failed');
        }
        this.isUpdating = false;
      },
      error: (err) => {
        this.isUpdating = false;
        alert(err.error?.message || 'Update failed');
      }
    });
  }

  private resetPasswordForm(): void {
    this.passwordData = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  }
}
