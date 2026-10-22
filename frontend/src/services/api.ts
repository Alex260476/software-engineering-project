import {
  ApiResponse,
  AuthResponse,
  ChangePasswordData,
  LoginData,
  Movie,
  PaymentCardData,
  RegistrationData,
  SentEmail,
  UpdateProfileData,
  User,
} from '../types';

// In dev, Vite proxies /api to the Spring Boot server (see vite.config.ts).
const API_BASE = `${import.meta.env.VITE_API_BASE_URL ?? ''}/api/v1`;

export class ApiError extends Error {
  constructor(message: string, public status?: number) {
    super(message);
  }
}

// Token helper
const getAuthToken = (): string | null => {
  return localStorage.getItem('cinema_token');
};

const getUserId = (): string | null => {
  try {
    const raw = localStorage.getItem('cinema_user');
    if (raw) {
      const u = JSON.parse(raw);
      return u?.id ?? null;
    }
  } catch {
    // ignore
  }
  return null;
};

const request = async <T>(
  path: string,
  options: RequestInit = {}
): Promise<T> => {
  const token = getAuthToken();
  const userId = getUserId();

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  } else if (userId) {
    headers['Authorization'] = `Bearer ${userId}`;
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers,
    });
  } catch {
    throw new ApiError('Unable to reach the server. Make sure the backend is running on port 8080.');
  }

  let body: ApiResponse<T> | null = null;
  try {
    body = await response.json();
  } catch {
    // Non-JSON response
  }

  if (!response.ok || !body?.success) {
    const message = body?.message ?? body?.error ?? `Request failed (${response.status})`;
    throw new ApiError(message, response.status);
  }

  return body.data as T;
};

// ==================== MOVIES ====================
export const fetchMovies = () => request<Movie[]>('/movies');

export const fetchMovieById = (id: string) => request<Movie>(`/movies/${encodeURIComponent(id)}`);

export const searchMovies = (title: string) =>
  request<Movie[]>(`/movies/search?title=${encodeURIComponent(title)}`);

export const filterMovies = ({ genre, showDates }: { genre?: string; showDates?: string[] }) => {
  const params = new URLSearchParams();
  if (genre) params.set('genre', genre);
  showDates?.forEach(date => params.append('showDate', date));
  return request<Movie[]>(`/movies/filter?${params}`);
};

export const fetchGenres = () => request<string[]>('/movies/genres');

// ==================== AUTH ====================
export const registerUser = (data: RegistrationData) =>
  request<AuthResponse>('/auth/register', {
    method: 'POST',
    body: JSON.stringify(data),
  });

export const loginUser = (data: LoginData) =>
  request<AuthResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify(data),
  });

export const verifyEmail = (token: string) =>
  request<AuthResponse>(`/auth/verify-email?token=${encodeURIComponent(token)}`, {
    method: 'POST',
  });

export const resendVerificationEmail = (email: string) =>
  request<string>(`/auth/resend-verification?email=${encodeURIComponent(email)}`, {
    method: 'POST',
  });

export const forgotPassword = (email: string) =>
  request<string>(`/auth/forgot-password?email=${encodeURIComponent(email)}`, {
    method: 'POST',
  });

export const resetPassword = (token: string, newPassword: string) =>
  request<AuthResponse>('/auth/reset-password', {
    method: 'POST',
    body: JSON.stringify({ token, newPassword }),
  });

export const logoutUser = () =>
  request<{ message: string }>('/auth/logout', {
    method: 'POST',
  });

// ==================== USER PROFILE ====================
export const fetchProfile = (userId?: string) =>
  request<User>(userId ? `/users/me?userId=${encodeURIComponent(userId)}` : '/users/me');

export const updateProfile = (data: UpdateProfileData, userId?: string) =>
  request<User>(userId ? `/users/profile?userId=${encodeURIComponent(userId)}` : '/users/profile', {
    method: 'PUT',
    body: JSON.stringify(data),
  });

export const changePassword = (data: ChangePasswordData, userId?: string) =>
  request<{ message: string }>(userId ? `/users/change-password?userId=${encodeURIComponent(userId)}` : '/users/change-password', {
    method: 'PUT',
    body: JSON.stringify(data),
  });

// ==================== PAYMENT CARDS ====================
export const addPaymentCard = (data: PaymentCardData, userId?: string) =>
  request<User>(userId ? `/users/cards?userId=${encodeURIComponent(userId)}` : '/users/cards', {
    method: 'POST',
    body: JSON.stringify(data),
  });

export const updatePaymentCard = (cardId: string, data: PaymentCardData, userId?: string) =>
  request<User>(userId ? `/users/cards/${cardId}?userId=${encodeURIComponent(userId)}` : `/users/cards/${cardId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });

export const deletePaymentCard = (cardId: string, userId?: string) =>
  request<User>(userId ? `/users/cards/${cardId}?userId=${encodeURIComponent(userId)}` : `/users/cards/${cardId}`, {
    method: 'DELETE',
  });

// ==================== FAVORITES ====================
export const fetchFavoriteMovies = (userId?: string) =>
  request<Movie[]>(userId ? `/users/favorites?userId=${encodeURIComponent(userId)}` : '/users/favorites');

export const addFavoriteMovie = (movieId: string, userId?: string) =>
  request<User>(userId ? `/users/favorites/${movieId}?userId=${encodeURIComponent(userId)}` : `/users/favorites/${movieId}`, {
    method: 'POST',
  });

export const removeFavoriteMovie = (movieId: string, userId?: string) =>
  request<User>(userId ? `/users/favorites/${movieId}?userId=${encodeURIComponent(userId)}` : `/users/favorites/${movieId}`, {
    method: 'DELETE',
  });

// ==================== ADMIN ====================
export const fetchAllUsers = (userId?: string) =>
  request<User[]>(userId ? `/users/admin/all?userId=${encodeURIComponent(userId)}` : '/users/admin/all');

// ==================== DEV / DEMO ====================
export const fetchDevEmails = () => request<SentEmail[]>('/dev/emails');

export const fetchLatestVerification = (email?: string) =>
  request<{ actionUrl: string }>(email ? `/dev/latest-verification?email=${encodeURIComponent(email)}` : '/dev/latest-verification');

export const resetDemoUsers = () =>
  request<string>('/dev/reset-demo-users', {
    method: 'POST',
  });
