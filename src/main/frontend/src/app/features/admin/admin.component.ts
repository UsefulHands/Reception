import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {AdminModel, AdminRegistrationRequest, AdminUpdateRequest} from './models/admin.model';
import { AdminService } from './admin.service';
import { ApiResponse} from '../../core/models/api/ApiResponse';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  admins: AdminModel[] = [];
  isEditMode = false;
  selectedAdmin: AdminModel & { username?: string; password?: string } = this.getEmptyAdmin();

  constructor(
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadAdmins();
  }

  getEmptyAdmin() {
    return {
      id: 0,
      firstName: '',
      lastName: '',
      corporateEmail: '',
      adminTitle: '',
      username: '',
      password: ''
    };
  }

  loadAdmins() {
    this.adminService.getAdmins().subscribe({
      next: (res: ApiResponse<AdminModel[]>) => {
        if (res.success && res.data) {
          this.admins = [...res.data];
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Loading error:', err)
    });
  }

  saveAdmin() {
    if (this.isEditMode && this.selectedAdmin.id) {
      const updatePayload: AdminUpdateRequest = {
        firstName: this.selectedAdmin.firstName,
        lastName: this.selectedAdmin.lastName,
        corporateEmail: this.selectedAdmin.corporateEmail,
        adminTitle: this.selectedAdmin.adminTitle
      };

      if (confirm('Are you sure you want to update this admin?')) {
        this.adminService.updateAdmin(this.selectedAdmin.id, updatePayload).subscribe({
          next: (res: ApiResponse<AdminModel>) => {
            if (res.success) {
              console.log('Update successful:', res.message);
              this.resetForm();
              this.loadAdmins();
            }
          },
          error: (err) => console.error('Update error:', err)
        });
      }
    } else {
      const registrationPayload: AdminRegistrationRequest = {
        adminDto: {
          firstName: this.selectedAdmin.firstName,
          lastName: this.selectedAdmin.lastName,
          corporateEmail: this.selectedAdmin.corporateEmail,
          adminTitle: this.selectedAdmin.adminTitle
        },
        username: this.selectedAdmin.username || '',
        password: this.selectedAdmin.password || ''
      };

      if (confirm('Are you sure you want to create this admin?')) {
        this.adminService.createAdmin(registrationPayload).subscribe({
          next: (res: ApiResponse<AdminModel>) => {
            if (res.success) {
              console.log('Registration successful:', res.message);
              this.loadAdmins();
              this.resetForm();
            }
          },
          error: (err) => console.error('Save error:', err)
        });
      }
    }
  }

  editAdmin(admin: AdminModel) {
    this.selectedAdmin = { ...admin };
    this.isEditMode = true;
  }

  deleteAdmin(id: number) {
    if (confirm('Are you sure you want to delete this admin?')) {
      this.adminService.deleteAdmin(id).subscribe({
        next: (res: ApiResponse<void>) => {
          if (res.success) {
            console.log('Delete successful:', res.message);
            this.loadAdmins();
          }
        },
        error: (err) => console.error('Delete error:', err)
      });
    }
  }

  resetForm() {
    this.selectedAdmin = this.getEmptyAdmin();
    this.isEditMode = false;
    this.cdr.detectChanges();
  }
}
