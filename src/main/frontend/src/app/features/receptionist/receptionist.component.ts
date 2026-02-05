import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ReceptionistModel} from '../../core/models/receptionist/ReceptionistModel';
import { ReceptionistRegistrationRequest} from '../../core/models/receptionist/ReceptionistRegisterationRequest';
import {environment} from '../../../environments/environment';

@Component({
  selector: 'app-receptionist',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './receptionist.component.html',
  styleUrls: ['./receptionist.component.css']
})
export class ReceptionistComponent implements OnInit {
  receptionists: ReceptionistModel[] = [];
  isEditMode = false;
  private readonly API_URL = `${environment.apiUrl}/receptionists`;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadReceptionists();
  }

  selectedReceptionist: ReceptionistModel & { username?: string, password?: string } = this.getEmptyReceptionist();

  getEmptyReceptionist() {
    return {
      firstName: '',
      lastName: '',
      shiftType: '',
      username: '',
      password: ''
    };
  }

  loadReceptionists() {
    this.http.get<any>(this.API_URL).subscribe({
      next: (res) => {

        this.receptionists = res.data ? [...res.data] : [];
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Loading error:', err)
    });
  }

  saveReceptionist() {
    if (this.isEditMode && this.selectedReceptionist.id) {
      // UPDATE (PUT)
      const updatePayload: ReceptionistModel = {
        firstName: this.selectedReceptionist.firstName,
        lastName: this.selectedReceptionist.lastName,
        shiftType: this.selectedReceptionist.shiftType,
      };

      if(confirm("Are you sure you want to update this receptionist?")) {
        this.http.put<any>(`${this.API_URL}/${this.selectedReceptionist.id}`, updatePayload).subscribe({
          next: (res) => {
            console.log('Update successful:', res.message);
            this.resetForm();
            this.loadReceptionists();
          },
          error: (err) => console.error('Update error:', err)
        });
      }
    } else {
      // CREATE (POST)
      const registrationPayload: ReceptionistRegistrationRequest = {
        receptionistDetails: {
          firstName: this.selectedReceptionist.firstName,
          lastName: this.selectedReceptionist.lastName,
          shiftType: this.selectedReceptionist.shiftType
        },
        username: this.selectedReceptionist.username || '',
        password: this.selectedReceptionist.password || ''
      };

      if(confirm("Are you sure you want to add this receptionist?")) {
        this.http.post<any>(this.API_URL, registrationPayload).subscribe({
          next: (res) => {
            console.log('Registration successful:', res.message);
            this.loadReceptionists();
            this.resetForm();
          },
          error: (err) => console.error('Save error:', err)
        });
      }
    }
  }

  editReceptionist(receptionist: ReceptionistModel) {
    this.selectedReceptionist = { ...receptionist };
    this.isEditMode = true;
  }

  deleteReceptionist(id: number) {
    if (confirm('Are you sure you want to delete this receptionist?')) {
      this.http.delete(`${this.API_URL}/${id}`).subscribe(() => this.loadReceptionists());
    }
  }

  resetForm() {
    this.selectedReceptionist = this.getEmptyReceptionist();
    this.isEditMode = false;
    this.cdr.detectChanges();
  }
}
