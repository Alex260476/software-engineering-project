export type MovieStatus = 'Currently Running' | 'Coming Soon';

export interface Movie {
  id: string;
  title: string;
  description: string;
  rating: string;
  posterUrl: string;
  trailerUrl: string;
  genre: string[];
  status: MovieStatus;
  releaseDate?: string;
  duration?: string;
}

export type TicketCategoryType = 'Adult' | 'Child' | 'Senior';

export interface TicketCategory {
  type: TicketCategoryType;
  price: number;
}

export interface Seat {
  id: string;
  row: string;
  number: number;
  isAvailable: boolean;
  isSelected?: boolean;
}
