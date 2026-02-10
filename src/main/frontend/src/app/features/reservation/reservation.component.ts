import { Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ReservationService } from './reservation.service';
import {ReservationGridResponse} from './reservation.grid.response';
import {ReservationStatus} from './reservation.status';
import {ReservationModel} from './reservation.model';

declare var bootstrap: any;

@Component({
  selector: 'app-reservation',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './reservation.component.html',
  styleUrls: ['./reservation.component.css']
})
export class ReservationComponent implements OnInit, AfterViewInit {
  rooms: any[] = [];
  gridData: ReservationGridResponse = {};
  selectedDate: string = new Date().toISOString().substring(0, 7);
  daysInMonth: number[] = [];
  resForm: FormGroup;
  modalInstance: any;
  isEditMode = false;

  @ViewChild('reservationModal') modalElement!: ElementRef;

  constructor(
    private reservationService: ReservationService,
    private cdr: ChangeDetectorRef,
    private fb: FormBuilder,
    public authService: AuthService
  ) {
    this.resForm = this.fb.group({
      id: [null],
      roomId: [null, Validators.required],
      roomNumber: [''],
      checkInDate: ['', Validators.required],
      checkOutDate: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      identityNumber: ['', Validators.required],
      status: [ReservationStatus.CONFIRMED]
    });
  }

  ngOnInit(): void {
    this.updateCalendar();
    this.loadRooms();
  }

  ngAfterViewInit(): void {
    if (this.modalElement) {
      this.modalInstance = new bootstrap.Modal(this.modalElement.nativeElement);
    }
  }

  isAdmin(): boolean {
    return this.authService.hasRole('ADMIN');
  }

  updateCalendar(): void {
    const [year, month] = this.selectedDate.split('-').map(Number);
    const days = new Date(year, month, 0).getDate();
    this.daysInMonth = Array.from({ length: days }, (_, i) => i + 1);
    this.loadGridData();
  }

  loadRooms(): void {
    this.reservationService.getRooms().subscribe({
      next: (res) => {
        this.rooms = res.data || [];
        this.cdr.detectChanges();
      }
    });
  }

  loadGridData(): void {
    const start = `${this.selectedDate}-01`;
    const lastDay = this.daysInMonth[this.daysInMonth.length - 1];
    const end = `${this.selectedDate}-${lastDay.toString().padStart(2, '0')}`;

    this.reservationService.getGridData(start, end).subscribe({
      next: (res) => {
        this.gridData = res.data || {};
        this.cdr.detectChanges();
      }
    });
  }

  getReservationStart(roomId: number, day: number): ReservationModel | undefined {
    const dateStr = `${this.selectedDate}-${day.toString().padStart(2, '0')}`;
    return (this.gridData[roomId] || []).find(res => res.checkInDate === dateStr);
  }

  isDayOccupied(roomId: number, day: number): boolean {
    const dateStr = `${this.selectedDate}-${day.toString().padStart(2, '0')}`;
    return (this.gridData[roomId] || []).some(res =>
      dateStr >= res.checkInDate && dateStr < res.checkOutDate
    );
  }

  calculateWidth(checkIn: string, checkOut: string): string {
    const start = new Date(checkIn);
    const end = new Date(checkOut);
    const diffDays = Math.ceil(Math.abs(end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
    return `${((diffDays + 1) * 45) - 8}px`;
  }

  openNewReservation(room: any, day: number): void {
    this.isEditMode = false;
    const dateStr = `${this.selectedDate}-${day.toString().padStart(2, '0')}`;
    this.resForm.reset({
      roomId: room.id,
      roomNumber: room.roomNumber,
      checkInDate: dateStr,
      checkOutDate: dateStr,
      status: ReservationStatus.CONFIRMED
    });
    this.modalInstance.show();
  }

  openEditReservation(reservationId: number): void {
    if (reservationId === undefined) return;

    this.isEditMode = true;
    this.reservationService.getById(reservationId).subscribe({
      next: (res) => {
        const data = res.data;
        const nameParts = data.guestFullName ? data.guestFullName.split(' ') : ['', ''];
        this.resForm.patchValue({
          id: data.id,
          roomId: data.roomId,
          roomNumber: data.roomNumber,
          checkInDate: data.checkInDate,
          checkOutDate: data.checkOutDate,
          firstName: nameParts[0],
          lastName: nameParts.slice(1).join(' '),
          phoneNumber: data.phoneNumber || 'PROTECTED',
          identityNumber: data.identityNumber || 'PROTECTED',
          status: data.status
        });
        this.modalInstance.show();
      }
    });
  }

  onSave(): void {
    if (this.resForm.invalid) return;

    if (this.isEditMode) {
      this.handleUpdate();
    } else {
      this.handleCreate();
    }
  }

  private handleUpdate(): void {
    const payload = {
      roomId: this.resForm.value.roomId,
      checkInDate: this.resForm.value.checkInDate,
      checkOutDate: this.resForm.value.checkOutDate,
      status: this.resForm.value.status
    };
    if (confirm("Update reservation?")) {
      this.reservationService.update(this.resForm.value.id, payload).subscribe({
        next: (res) => this.handleSuccess(res.message)
      });
    }
  }

  private handleCreate(): void {
    if (confirm("Create new reservation?")) {
      this.reservationService.create(this.resForm.value).subscribe({
        next: (res) => this.handleSuccess(res.message)
      });
    }
  }

  cancelReservation(): void {
    const id = this.resForm.value.id;
    if (id && confirm('Cancel this reservation?')) {
      this.reservationService.cancel(id).subscribe({
        next: (res) => this.handleSuccess(res.message)
      });
    }
  }

  deleteReservation(): void {
    const id = this.resForm.value.id;
    if (id && confirm('Delete permanently?')) {
      this.reservationService.delete(id).subscribe({
        next: (res) => this.handleSuccess(res.message)
      });
    }
  }

  private handleSuccess(msg: string): void {
    alert(msg);
    this.modalInstance.hide();
    this.loadGridData();
  }
}
