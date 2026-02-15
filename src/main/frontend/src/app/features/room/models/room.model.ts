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

export const ROOM_CONSTANTS = {
  types: ['SINGLE', 'DOUBLE', 'SUITE', 'DELUXE', 'FAMILY'],
  statuses: ['CLEAN', 'DIRTY', 'MAINTENANCE', 'OCCUPIED', 'RESERVED'],
  views: ['SEA', 'GARDEN', 'CITY', 'MOUNTAIN', 'NONE'],
  bedTypes: ['SINGLE', 'DOUBLE', 'QUEEN', 'KING', 'SOFA'],
  amenities: ['WIFI', 'TV', 'MINIBAR', 'AIR_CONDITIONING', 'SAFE', 'BALCONY', 'JACUZZI']
};
