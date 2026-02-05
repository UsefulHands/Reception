import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AdminModel } from '../../core/models/admin/AdminModel';
import {AdminRegistrationRequest} from '../../core/models/admin/AdminRegisterationRequest';
import {environment} from '../../../environments/environment';

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
  private readonly API_URL = `${environment.apiUrl}/admins`;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadAdmins();
  }

  selectedAdmin: AdminModel & { username?: string, password?: string } = this.getEmptyAdmin();

  getEmptyAdmin() {
    return {
      firstName: '',
      lastName: '',
      corporateEmail: '',
      adminTitle: '',
      username: '',
      password: ''
    };
  }

  loadAdmins() {
    this.http.get<any>(this.API_URL).subscribe({
      next: (res) => {
        this.admins = res.data ? [...res.data] : [];
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Loading error:', err)
    });
  }

  saveAdmin() {
    if (this.isEditMode && this.selectedAdmin.id) {
      // UPDATE (PUT)
      const updatePayload: AdminModel = {
        firstName: this.selectedAdmin.firstName,
        lastName: this.selectedAdmin.lastName,
        corporateEmail: this.selectedAdmin.corporateEmail,
        adminTitle: this.selectedAdmin.adminTitle
      };
      if(confirm("Are you sure you want to update this admin?")) {
        this.http.put<any>(`${this.API_URL}/${this.selectedAdmin.id}`, updatePayload).subscribe({
          next: (res) => {
            console.log('Update successful:', res.message);
            this.resetForm();
            this.loadAdmins();
          },
          error: (err) => console.error('Update error:', err)
        });
      }

    } else {
      // CREATE (POST)
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

      if(confirm("Are you sure you want to create this admin?")) {
        this.http.post<any>(this.API_URL, registrationPayload).subscribe({
          next: (res) => {
            console.log('Registration successful:', res.message);
            this.loadAdmins();
            this.resetForm();
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
      this.http.delete(`${this.API_URL}/${id}`).subscribe(() => this.loadAdmins());
    }
  }

  resetForm() {
    this.selectedAdmin = this.getEmptyAdmin();
    this.isEditMode = false;
    this.cdr.detectChanges();
  }
}
