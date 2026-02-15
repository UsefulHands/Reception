import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GuestService } from './guest.service';
import { ApiResponse} from '../../core/models/api/ApiResponse';
import {GuestModel, GuestRegistrationRequest, GuestUpdateRequest} from './models/guest.model';

@Component({
  selector: 'app-guest',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './guest.component.html',
  styleUrls: ['./guest.component.css']
})
export class GuestComponent implements OnInit {
  guests: GuestModel[] = [];
  isEditMode = false;
  selectedGuest: GuestModel & { username?: string; password?: string } = this.getEmptyGuest();

  constructor(
    private guestService: GuestService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadGuests();
  }

  getEmptyGuest() {
    return {
      id: 0,
      firstName: '',
      lastName: '',
      phoneNumber: '',
      identityNumber: '',
      createdAt: '',
      username: '',
      password: ''
    };
  }

  loadGuests() {
    this.guestService.getGuests().subscribe({
      next: (res: ApiResponse<GuestModel[]>) => {
        if (res.success && res.data) {
          this.guests = [...res.data];
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Loading error:', err)
    });
  }

  saveGuest() {
    if (this.isEditMode && this.selectedGuest.id) {
      const updatePayload: GuestUpdateRequest = {
        firstName: this.selectedGuest.firstName,
        lastName: this.selectedGuest.lastName,
        phoneNumber: this.selectedGuest.phoneNumber,
        identityNumber: this.selectedGuest.identityNumber
      };

      if (confirm('Are you sure you want to update this guest?')) {
        this.guestService.updateGuest(this.selectedGuest.id, updatePayload).subscribe({
          next: (res: ApiResponse<GuestModel>) => {
            if (res.success) {
              console.log('Update successful:', res.message);
              this.resetForm();
              this.loadGuests();
            }
          },
          error: (err) => console.error('Update error:', err)
        });
      }
    } else {
      const registrationPayload: GuestRegistrationRequest = {
        guestDto: {
          firstName: this.selectedGuest.firstName,
          lastName: this.selectedGuest.lastName,
          phoneNumber: this.selectedGuest.phoneNumber,
          identityNumber: this.selectedGuest.identityNumber
        },
        username: this.selectedGuest.username || '',
        password: this.selectedGuest.password || ''
      };

      if (confirm('Are you sure you want to register this guest?')) {
        this.guestService.createGuest(registrationPayload).subscribe({
          next: (res: ApiResponse<GuestModel>) => {
            if (res.success) {
              console.log('Registration successful:', res.message);
              this.loadGuests();
              this.resetForm();
            }
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
      this.guestService.deleteGuest(id).subscribe({
        next: (res: ApiResponse<void>) => {
          if (res.success) {
            console.log('Delete successful:', res.message);
            this.loadGuests();
          }
        },
        error: (err) => console.error('Delete error:', err)
      });
    }
  }

  resetForm() {
    this.selectedGuest = this.getEmptyGuest();
    this.isEditMode = false;
    this.cdr.detectChanges();
  }
}
