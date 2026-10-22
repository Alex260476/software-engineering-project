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
  timestamp?: string;
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

// User & Auth Types for Sprint 2
export type UserType = 'CUSTOMER' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export interface Address {
  street?: string;
  city?: string;
  state?: string;
  zipCode?: string;
}

export interface PaymentCard {
  id: string;
  cardholderName: string;
  cardType: string;
  cardLastFour: string;
  expirationDate: string;
  billingAddress?: Address;
  addedAt?: string;
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  userType: UserType;
  status: UserStatus;
  promotionSubscription: boolean;
  emailVerified: boolean;
  address?: Address;
  paymentCards: PaymentCard[];
  favoriteMovieIds: string[];
}

export interface AuthResponse {
  token?: string;
  user?: User;
  message: string;
  emailVerified: boolean;
}

export interface RegistrationData {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  promotionSubscription: boolean;
  address?: Address;
  paymentCard?: {
    cardholderName: string;
    cardNumber: string;
    cardType: string;
    expirationDate: string;
    billingAddress?: Address;
  };
}

export interface LoginData {
  email: string;
  password: string;
}

export interface UpdateProfileData {
  firstName: string;
  lastName: string;
  phone?: string;
  promotionSubscription: boolean;
  address?: Address;
}

export interface ChangePasswordData {
  currentPassword: string;
  newPassword: string;
}

export interface PaymentCardData {
  cardholderName: string;
  cardNumber: string;
  cardType: string;
  expirationDate: string;
  billingAddress?: Address;
}

export interface SentEmail {
  to: string;
  subject: string;
  body: string;
  actionUrl?: string;
  type: string;
  sentAt: string;
}
