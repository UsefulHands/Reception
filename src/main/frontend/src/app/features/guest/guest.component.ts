import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { GuestModel} from '../../core/models/guest/GuestModel';
import {GuestRegistrationRequest} from '../../core/models/guest/GuestRegisterationRequest';
import {environment} from '../../../environments/environment';

@Component({
  selector: 'app-guest',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './guest.component.html',
  styleUrls: ['./guest.component.css']
})
export class GuestComponent implements OnInit {
  guests: GuestModel[] = [];
  isEditMode = false;
  private readonly API_URL = `${environment.apiUrl}/guests`;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadGuests();
  }

  selectedGuest: GuestModel & { username?: string, password?: string } = this.getEmptyGuest();

  getEmptyGuest() {
    return {
      firstName: '',
      lastName: '',
      phoneNumber: '',
      identityNumber: '',
      username: '',
      password: ''
    };
  }

  loadGuests() {
    this.http.get<any>(this.API_URL).subscribe({
      next: (res) => {
        this.guests = res.data ? [...res.data] : [];
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Loading error:', err)
    });
  }

  saveGuest() {
    if (this.isEditMode && this.selectedGuest.id) {
      // UPDATE (PUT)
      const updatePayload: GuestModel = {
        firstName: this.selectedGuest.firstName,
        lastName: this.selectedGuest.lastName,
        phoneNumber: this.selectedGuest.phoneNumber,
        identityNumber: this.selectedGuest.identityNumber
      };

      if(confirm("Are you sure you want to update this guest?")) {
        this.http.put<any>(`${this.API_URL}/${this.selectedGuest.id}`, updatePayload).subscribe({
          next: (res) => {
            console.log('Update successful:', res.message);
            this.resetForm();
            this.loadGuests();
          },
          error: (err) => console.error('Update error:', err)
        });
      }
    } else {
      // CREATE (POST)
      const registrationPayload: GuestRegistrationRequest = {
        guestDetails: {
          firstName: this.selectedGuest.firstName,
          lastName: this.selectedGuest.lastName,
          phoneNumber: this.selectedGuest.phoneNumber,
          identityNumber: this.selectedGuest.identityNumber
        },
        username: this.selectedGuest.username || '',
        password: this.selectedGuest.password || ''
      };

      if(confirm("Are you sure you want to register this guest?")) {
        this.http.post<any>(this.API_URL, registrationPayload).subscribe({
          next: (res) => {
            console.log('Registration successful:', res.message);
            this.loadGuests();
            this.resetForm();
          },
          error: (err) => console.error('Save error:', err)
        });
      }
    }
  }

  editGuest(guest: GuestModel) {
    this.selectedGuest = { ...guest };
    this.isEditMode = true;
  }

  deleteGuest(id: number) {
    if (confirm('Are you sure you want to delete this guest?')) {
      this.http.delete(`${this.API_URL}/${id}`).subscribe(() => this.loadGuests());
    }
  }

  resetForm() {
    this.selectedGuest = this.getEmptyGuest();
    this.isEditMode = false;
    this.cdr.detectChanges();
  }
}
