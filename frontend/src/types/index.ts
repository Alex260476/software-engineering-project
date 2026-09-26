// Values stored in the Movie table's status attribute
export type MovieStatus = 'CURRENTLY_RUNNING' | 'COMING_SOON';

export const STATUS_LABELS: Record<MovieStatus, string> = {
  CURRENTLY_RUNNING: 'Now Playing',
  COMING_SOON: 'Coming Soon',
};

// Mirrors the backend MovieDTO
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
  runtime?: number;
  director?: string;
  cast?: string[];
  imdbRating?: number;
  availableShowtimes?: string[];
  // Days of the week the movie is shown, e.g. ["MONDAY", "SATURDAY"]
  showDays?: string[];
}

// Wrapper every backend endpoint responds with
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
  total?: number;
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
