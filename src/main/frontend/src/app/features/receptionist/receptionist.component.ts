import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {ReceptionistModel, SelectedReceptionist, SHIFT_TYPES, ReceptionistRegistrationRequest} from './models/receptionist.model';
import { ReceptionistService } from './receptionist.service';
import {ApiResponse} from '../../core/models/api/ApiResponse';

@Component({
  selector: 'app-receptionist',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './receptionist.component.html',
  styleUrls: ['./receptionist.component.css']
})
export class ReceptionistComponent implements OnInit {
  receptionists: ReceptionistModel[] = [];
  shiftTypes = SHIFT_TYPES;
  isEditMode = false;
  selectedReceptionist: SelectedReceptionist = this.getEmptyReceptionist();

  constructor(
    private receptionistService: ReceptionistService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadReceptionists();
  }

  getEmptyReceptionist(): SelectedReceptionist {
    return {
      firstName: '',
      lastName: '',
      shiftType: '',
      username: '',
      password: ''
    };
  }

  loadReceptionists(): void {
    this.receptionistService.getAll().subscribe({
      next: (res: ApiResponse<ReceptionistModel[]>) => {
        if (res.success) {
          this.receptionists = res.data ? [...res.data] : [];
          this.cdr.detectChanges();
        }
      },
      error: (err) => console.error(err)
    });
  }

  saveReceptionist(): void {
    if (this.isEditMode && this.selectedReceptionist.id) {
      this.handleUpdate();
    } else {
      this.handleCreate();
    }
    this.cdr.detectChanges();
  }

  private handleUpdate(): void {
    const updatePayload: ReceptionistModel = {
      id: this.selectedReceptionist.id,
      firstName: this.selectedReceptionist.firstName,
      lastName: this.selectedReceptionist.lastName,
      shiftType: this.selectedReceptionist.shiftType,
      userId: this.selectedReceptionist.userId
    };

    if (confirm("Update this receptionist?")) {
      this.receptionistService.update(updatePayload.id!, updatePayload).subscribe({
        next: (res) => {
          alert(res.message);
          this.resetForm();
          this.loadReceptionists();
        },
        error: (err) => alert(err.error?.message)
      });
    }
  }

  private handleCreate(): void {
    const registrationPayload: ReceptionistRegistrationRequest = {
      receptionistDetails: {
        firstName: this.selectedReceptionist.firstName,
        lastName: this.selectedReceptionist.lastName,
        shiftType: this.selectedReceptionist.shiftType
      },
      username: this.selectedReceptionist.username || '',
      password: this.selectedReceptionist.password || ''
    };

    if (confirm("Add this receptionist?")) {
      this.receptionistService.create(registrationPayload).subscribe({
        next: (res) => {
          alert(res.message);
          this.loadReceptionists();
          this.resetForm();
        },
        error: (err) => alert(err.error?.message)
      });
    }
  }

  editReceptionist(receptionist: ReceptionistModel): void {
    this.selectedReceptionist = { ...receptionist };
    this.isEditMode = true;
  }

  deleteReceptionist(id: number): void {
    if (confirm('Are you sure?')) {
      this.receptionistService.delete(id).subscribe({
        next: (res) => {
          alert(res.message);
          this.loadReceptionists();
        },
        error: (err) => alert(err.error?.message)
      });
    }
  }

  resetForm(): void {
    this.selectedReceptionist = this.getEmptyReceptionist();
    this.isEditMode = false;
    this.cdr.detectChanges();
  }
}
