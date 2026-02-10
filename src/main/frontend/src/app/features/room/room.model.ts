export interface RoomModel {
  id?: number;
  roomNumber: string;
  type: string;
  status: string;
  bedTypes: string[];
  beds: number;
  maxGuests: number;
  areaSqm?: number;
  view?: string;
  description: string;
  price: number;
  available: boolean;
  smokingAllowed: boolean;
  floor: number;
  amenities: string[];
  images: string[];
}
