import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { RoomService } from './room.service';
import { AuthService } from '../../core/services/auth.service';
import { RoomModel } from '../../core/models/room/RoomModel';
import { ApiResponse } from '../../core/models/api/ApiResponse';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

declare var bootstrap: any;

@Component({
  selector: 'app-room',
  templateUrl: './room.component.html',
  imports: [
    FormsModule,
    CommonModule
  ],
  styleUrls: ['./room.component.css']
})
export class RoomComponent implements OnInit {
  rooms: RoomModel[] = [];
  selectedRoom: RoomModel = this.getEmptyRoom();
  detailRoom: RoomModel | null = null;

  isEditMode = false;
  showAvailableOnly = false;

  roomTypes = ['SINGLE', 'DOUBLE', 'SUITE', 'DELUXE', 'FAMILY'];
  viewTypes = ['SEA', 'GARDEN', 'CITY', 'MOUNTAIN', 'NONE'];
  bedTypeOptions = ['SINGLE', 'DOUBLE', 'QUEEN', 'KING', 'SOFA'];
  amenitiesList = ['WIFI', 'TV', 'MINIBAR', 'AIR_CONDITIONING', 'SAFE', 'BALCONY', 'JACUZZI'];

  constructor(private roomService: RoomService, public authService: AuthService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadRooms();
  }

  get isAdmin() { return this.authService.hasRole('ADMIN'); }
  get canModify() { return this.authService.hasAnyRole(['ADMIN', 'RECEPTIONIST']); }

  loadRooms() {
    const call = this.showAvailableOnly ? this.roomService.getAvailableRooms() : this.roomService.getAllRooms();
    call.subscribe((res: ApiResponse<RoomModel[]>) => {
      this.rooms = res.data;
      this.cdr.detectChanges();
    });
  }

  saveRoom() {
    if (!this.selectedRoom.beds || this.selectedRoom.beds < 1) this.selectedRoom.beds = 1;
    if (!this.selectedRoom.maxGuests || this.selectedRoom.maxGuests < 1) this.selectedRoom.maxGuests = 1;

    if (!this.selectedRoom.bedTypes || this.selectedRoom.bedTypes.length === 0) {
      alert("Please select at least one bed type.");
      return;
    }

    const request = this.isEditMode && this.selectedRoom.id
      ? this.roomService.editRoom(this.selectedRoom.id, this.selectedRoom)
      : this.roomService.createRoom(this.selectedRoom);

    request.subscribe({
      next: () => {
        this.resetForm();
        this.loadRooms();
      },
      error: (err) => {
        console.error("Backend hatası:", err);
        alert("Error saving room.");
      }
    });
  }

  editRoom(room: RoomModel) {
    this.selectedRoom = JSON.parse(JSON.stringify(room));
    this.isEditMode = true;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  deleteRoom(id: number) {
    if (confirm('Are you sure you want to delete this room?')) {
      this.roomService.deleteRoom(id).subscribe(() => this.loadRooms());
    }
  }

  viewDetails(room: RoomModel) {
    this.detailRoom = room;
    const modalElement = document.getElementById('roomDetailModal');
    if (modalElement) {
      const modal = new bootstrap.Modal(modalElement);
      modal.show();
    }
  }

  toggleAvailableFilter() {
    this.showAvailableOnly = !this.showAvailableOnly;
    this.loadRooms();
  }

  toggleBedType(type: string) {
    this.toggleSelection(this.selectedRoom.bedTypes, type);
  }

  toggleSelection(list: any[], item: any) {
    const idx = list.indexOf(item);
    if (idx > -1) {
      list.splice(idx, 1);
    } else {
      list.push(item);
    }
  }

  updateImages(csv: string) {
    this.selectedRoom.images = csv ? csv.split(',').map(s => s.trim()).filter(s => s !== '') : [];
  }

  resetForm() {
    this.selectedRoom = this.getEmptyRoom();
    this.isEditMode = false;
    this.cdr.detectChanges();
  }

  private getEmptyRoom(): RoomModel {
    return {
      roomNumber: '',
      type: 'SINGLE',
      bedTypes: [],
      beds: 1,
      maxGuests: 2,
      areaSqm: 25,
      view: 'CITY',
      description: '',
      price: 100,
      available: true,
      smokingAllowed: false,
      floor: 1,
      amenities: [],
      images: []
    };
  }
}
