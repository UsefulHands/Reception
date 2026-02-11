import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RoomService } from './room.service';
import { AuthService } from '../../core/services/auth.service';
import { ReservationService } from '../reservation/reservation.service';
import { RoomModel } from './room.model';
import { ROOM_CONSTANTS } from './room.constants';
import { ApiResponse } from '../../core/models/api/ApiResponse';

declare var bootstrap: any;

@Component({
  selector: 'app-room',
  templateUrl: './room.component.html',
  standalone: true,
  imports: [FormsModule, CommonModule],
  styleUrls: ['./room.component.css']
})
export class RoomComponent implements OnInit {
  rooms: RoomModel[] = [];
  selectedRoom: RoomModel = this.getEmptyRoom();
  detailRoom: RoomModel | null = null;

  reservationDates = { checkIn: '', checkOut: '' };
  isEditMode = false;
  showAvailableOnly = false;

  roomTypes = ROOM_CONSTANTS.types;
  roomStatuses = ROOM_CONSTANTS.statuses;
  viewTypes = ROOM_CONSTANTS.views;
  bedTypeOptions = ROOM_CONSTANTS.bedTypes;
  amenitiesList = ROOM_CONSTANTS.amenities;

  constructor(
    private roomService: RoomService,
    public authService: AuthService,
    private reservationService: ReservationService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRooms();
  }

  get isAdmin() { return this.authService.hasRole('ADMIN'); }
  get canModify() { return this.authService.hasAnyRole(['ADMIN', 'RECEPTIONIST']); }
  get isGuest() { return this.authService.hasRole('GUEST'); }
  get canReserveRoom() { return this.isGuest || !this.authService.isLoggedIn(); }

  loadRooms(): void {
    const call = this.showAvailableOnly ? this.roomService.getAvailableRooms() : this.roomService.getAllRooms();
    call.subscribe((res: ApiResponse<RoomModel[]>) => {
      this.rooms = res.data;
      this.cdr.detectChanges();
    });
  }

  confirmQuickReservation(): void {
    if (!this.reservationDates.checkIn || !this.reservationDates.checkOut) {
      alert("Please select dates.");
      return;
    }

    const reservationRequest = {
      roomId: this.detailRoom?.id,
      roomNumber: this.detailRoom?.roomNumber,
      checkInDate: this.reservationDates.checkIn,
      checkOutDate: this.reservationDates.checkOut,
      totalPrice: this.calculateTotalPrice()
    };

    this.reservationService.setTemporaryBooking(reservationRequest);
    this.closeModal('quickReserveModal');

    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/payment' } });
    } else {
      this.router.navigate(['/payments']);
    }
  }

  calculateTotalPrice(): number {
    const nights = this.getNightCount();
    const basePrice = this.detailRoom?.price || 0;
    return nights > 0 ? nights * basePrice : basePrice;
  }

  getNightCount(): number {
    if (!this.reservationDates.checkIn || !this.reservationDates.checkOut) return 0;
    const start = new Date(this.reservationDates.checkIn);
    const end = new Date(this.reservationDates.checkOut);
    const diff = end.getTime() - start.getTime();
    const nights = Math.ceil(diff / (1000 * 60 * 60 * 24));
    return nights > 0 ? nights : 0;
  }

  saveRoom(): void {
    if (!this.selectedRoom.bedTypes?.length) {
      alert("Select at least one bed type.");
      return;
    }

    const confirmMsg = this.isEditMode ? "Update this room?" : "Create this room?";
    if (!confirm(confirmMsg)) return;

    const request = this.isEditMode && this.selectedRoom.id
      ? this.roomService.editRoom(this.selectedRoom.id, this.selectedRoom)
      : this.roomService.createRoom(this.selectedRoom);

    request.subscribe({
      next: (res) => {
        alert(res.message);
        this.resetForm();
        this.loadRooms();
      },
      error: (err) => alert(err.error?.message || "Operation failed.")
    });
  }

  editRoom(room: RoomModel): void {
    this.selectedRoom = JSON.parse(JSON.stringify(room));
    this.isEditMode = true;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  deleteRoom(id: number): void {
    if (confirm("Are you sure?")) {
      this.roomService.deleteRoom(id).subscribe(() => this.loadRooms());
    }
  }

  viewDetails(room: RoomModel): void {
    this.detailRoom = room;
    this.openModal('roomDetailModal');
  }

  toggleAvailableFilter(): void {
    this.showAvailableOnly = !this.showAvailableOnly;
    this.loadRooms();
  }

  toggleBedType(type: string): void {
    const idx = this.selectedRoom.bedTypes.indexOf(type);
    if (idx > -1) this.selectedRoom.bedTypes.splice(idx, 1);
    else this.selectedRoom.bedTypes.push(type);
  }

  toggleAmenity(type: string): void {
    const idx = this.selectedRoom.amenities.indexOf(type);
    if (idx > -1) this.selectedRoom.amenities.splice(idx, 1);
    else this.selectedRoom.amenities.push(type);
  }

  updateImages(csv: string): void {
    this.selectedRoom.images = csv ? csv.split(',').map(s => s.trim()).filter(s => s !== '') : [];
  }

  resetForm(): void {
    this.selectedRoom = this.getEmptyRoom();
    this.isEditMode = false;
    this.cdr.detectChanges();
  }

  private getEmptyRoom(): RoomModel {
    return {
      roomNumber: '', type: 'SINGLE', status: 'CLEAN', bedTypes: [],
      beds: 1, maxGuests: 2, areaSqm: 25, view: 'CITY',
      description: '', price: 100, available: true,
      smokingAllowed: false, floor: 1, amenities: [], images: ['https://images.unsplash.com/photo-1568495248636-6432b97bd949?q=80&w=2574&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D']
    };
  }

  openReservationModal(): void {
    this.closeModal('roomDetailModal');
    this.openModal('quickReserveModal');
  }

  private openModal(id: string): void {
    const el = document.getElementById(id);
    if (el) new bootstrap.Modal(el).show();
  }

  private closeModal(id: string): void {
    const el = document.getElementById(id);
    if (el) {
      const modal = bootstrap.Modal.getInstance(el);
      if (modal) modal.hide();
    }
  }

  currentImageIndex: number = 0;

  prevImage() {
    if (!this.detailRoom?.images) return;

    if (this.currentImageIndex > 0) {
      this.currentImageIndex--;
    } else {
      this.currentImageIndex = this.detailRoom.images.length - 1;
    }
  }

  nextImage() {
    if (!this.detailRoom?.images) return;

    if (this.currentImageIndex < this.detailRoom.images.length - 1) {
      this.currentImageIndex++;
    } else {
      this.currentImageIndex = 0;
    }
  }

  isPreviewOpen: boolean = false;

  openPreview() {
    this.isPreviewOpen = true;
  }

  closePreview() {
    this.isPreviewOpen = false;
  }
}
