import {Component, OnInit, ChangeDetectorRef, ViewChild, ElementRef, AfterViewInit, HostListener} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ReservationService } from './reservation.service';
import {ReservationGridResponse, ReservationModel, ReservationStatus} from './models/reservation.model';

declare var bootstrap: any;

interface DayInfo {
  day: number;
  monthShort: string;
  fullDate: string;
}

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
  selectedStartDate: string = new Date().toISOString().substring(0, 10);
  daysInMonth: DayInfo[] = [];
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
      guestFirstName: ['', Validators.required],
      guestLastName: ['', Validators.required],
      phoneNumber: [''],
      identityNumber: [''],
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

  shiftWeek(direction: number): void {
    const currentDate = new Date(this.selectedStartDate);
    currentDate.setDate(currentDate.getDate() + (direction * 7));
    this.selectedStartDate = currentDate.toISOString().substring(0, 10);
    this.updateCalendar();
  }

  updateCalendarFromDate(): void {
    this.updateCalendar();
  }

  updateCalendar(): void {
    const startDate = new Date(this.selectedStartDate);
    this.daysInMonth = [];

    const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

    const endDate = new Date(startDate);
    endDate.setMonth(endDate.getMonth() + 1);

    const totalDays = Math.round((endDate.getTime() - startDate.getTime()) / 86400000);

    for (let i = 0; i < totalDays; i++) {
      const currentDate = new Date(startDate);
      currentDate.setDate(startDate.getDate() + i);

      const year = currentDate.getFullYear();
      const month = (currentDate.getMonth() + 1).toString().padStart(2, '0');
      const day = currentDate.getDate();

      this.daysInMonth.push({
        day: day,
        monthShort: monthNames[currentDate.getMonth()],
        fullDate: `${year}-${month}-${day.toString().padStart(2, '0')}`
      });
    }

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
    if (this.daysInMonth.length === 0) return;

    const startDate = new Date(this.daysInMonth[0].fullDate);
    startDate.setDate(startDate.getDate() - 7);
    const start = startDate.toISOString().substring(0, 10);

    const endDate = new Date(this.daysInMonth[this.daysInMonth.length - 1].fullDate);
    endDate.setDate(endDate.getDate() + 7);
    const end = endDate.toISOString().substring(0, 10);

    this.reservationService.getGridData(start, end).subscribe({
      next: (res) => {
        this.gridData = res.data || {};
        this.cdr.detectChanges();
      }
    });
  }

  getReservationStart(roomId: number, dateStr: string): ReservationModel | undefined {
    const reservations = this.gridData[roomId] || [];

    for (const res of reservations) {
      if (res.checkInDate === dateStr) {
        return res;
      }

      if (res.checkInDate < dateStr && dateStr < res.checkOutDate) {
        const isFirstVisibleDay = !this.daysInMonth.some(d => d.fullDate === res.checkInDate);

        if (isFirstVisibleDay) {
          const firstDay = this.daysInMonth[0]?.fullDate;
          if (dateStr === firstDay) {
            return res;
          }
        }
      }
    }

    return undefined;
  }

  isDayOccupied(roomId: number, dateStr: string): boolean {
    return (this.gridData[roomId] || []).some(res =>
      dateStr >= res.checkInDate && dateStr < res.checkOutDate
    );
  }

  private getCellWidth(): number {
    const cell = document.querySelector('.cell-square') as HTMLElement;
    return cell ? cell.getBoundingClientRect().width : 45;
  }

  calculateResStyle(checkIn: string, checkOut: string): any {
    const d1 = new Date(checkIn);
    const d2 = new Date(checkOut);
    d1.setHours(0,0,0,0);
    d2.setHours(0,0,0,0);

    const firstVisibleDate = new Date(this.daysInMonth[0]?.fullDate || checkIn);
    firstVisibleDate.setHours(0,0,0,0);

    const lastVisibleDate = new Date(this.daysInMonth[this.daysInMonth.length - 1]?.fullDate || checkOut);
    lastVisibleDate.setHours(0,0,0,0);

    const effectiveStart = d1 < firstVisibleDate ? firstVisibleDate : d1;
    const effectiveEnd = d2 > lastVisibleDate ? lastVisibleDate : d2;

    const diffDays = Math.max(
      Math.round((effectiveEnd.getTime() - effectiveStart.getTime()) / 86400000),
      1
    );

    const cellWidth = this.getCellWidth();

    const startsBeforeGrid = d1 < firstVisibleDate;
    const endsAfterGrid = d2 > lastVisibleDate;

    const startOffset = startsBeforeGrid ? 0 : cellWidth * 0.6;
    const endOffset = endsAfterGrid ? cellWidth : cellWidth * 0.4;

    const width = (diffDays * cellWidth) - startOffset + endOffset;

    return {
      width: `${width}px`,
      left: `${startOffset}px`,
      position: 'absolute'
    };
  }

  @HostListener('window:resize')
  onResize() {
    this.cdr.detectChanges();
  }

  openNewReservation(room: any, dateStr: string): void {
    this.isEditMode = false;

    const checkInDate = new Date(dateStr);
    const nextDay = new Date(checkInDate);
    nextDay.setDate(checkInDate.getDate() + 1);

    this.resForm.reset({
      roomId: room.id,
      roomNumber: room.roomNumber,
      checkInDate: dateStr,
      checkOutDate: this.formatDate(nextDay),
      status: ReservationStatus.CONFIRMED,
      guestFirstName: '',
      guestLastName: ''
    });
    this.cdr.detectChanges();
    this.modalInstance.show();
  }

  formatDate(date: Date): string {
    return date.toISOString().substring(0, 10);
  }

  openEditReservation(reservationId: number): void {
    if (!reservationId) return;
    this.isEditMode = true;
    this.reservationService.getById(reservationId).subscribe({
      next: (res) => {
        const data = res.data;
        this.resForm.patchValue({
          id: data.id,
          roomId: data.roomId,
          roomNumber: data.roomNumber,
          checkInDate: data.checkInDate,
          checkOutDate: data.checkOutDate,
          guestFirstName: data.guestFirstName,
          guestLastName: data.guestLastName,
          phoneNumber: data.phoneNumber,
          identityNumber: data.identityNumber,
          status: data.status
        });
        this.cdr.detectChanges();
        this.modalInstance.show();
      }
    });
  }

  onSave(): void {
    if (this.resForm.invalid) return;
    this.isEditMode ? this.handleUpdate() : this.handleCreate();
  }

  private handleUpdate(): void {
    const checkIn = this.resForm.get('checkInDate')?.value;
    const checkOut = this.resForm.get('checkOutDate')?.value;

    if (!checkIn || !checkOut) {
      alert('Please select both check-in and check-out dates');
      return;
    }

    if (checkIn >= checkOut) {
      alert('Check-out date must be at least 1 day after check-in date');
      return;
    }

    const payload = { ...this.resForm.value };
    if (confirm("Update reservation?")) {
      this.reservationService.update(payload.id, payload).subscribe({
        next: (res) => this.handleSuccess(res.message),
        error: (err) => alert(err.error?.message || 'Failed to update reservation. The room may not be available for selected dates.')
      });
    }
  }

  private handleCreate(): void {
    const checkIn = this.resForm.get('checkInDate')?.value;
    const checkOut = this.resForm.get('checkOutDate')?.value;

    if (!checkIn || !checkOut) {
      alert('Please select both check-in and check-out dates');
      return;
    }

    if (checkIn >= checkOut) {
      alert('Check-out date must be at least 1 day after check-in date');
      return;
    }

    const d1 = new Date(checkIn);
    const d2 = new Date(checkOut);
    const diffDays = Math.ceil((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays < 1) {
      alert('Minimum stay is 1 night');
      return;
    }

    if (confirm("Create new reservation?")) {
      this.reservationService.create(this.resForm.value).subscribe({
        next: (res) => this.handleSuccess(res.message),
        error: (err) => alert(err.error?.message || 'Failed to create reservation. The room may not be available for selected dates.')
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
