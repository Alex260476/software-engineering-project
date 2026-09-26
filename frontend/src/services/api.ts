import { ApiResponse, Movie } from '../types';

// In dev, Vite proxies /api to the Spring Boot server (see vite.config.ts).
// Set VITE_API_BASE_URL to call a backend on another host instead.
const API_BASE = `${import.meta.env.VITE_API_BASE_URL ?? ''}/api/v1/movies`;

export class ApiError extends Error {
  constructor(message: string, public status?: number) {
    super(message);
  }
}

const request = async <T>(path: string): Promise<T> => {
  let response: Response;
  try {
    response = await fetch(`${API_BASE}${path}`);
  } catch {
    throw new ApiError('Unable to reach the server. Make sure the backend is running on port 8080.');
  }

  let body: ApiResponse<T> | null = null;
  try {
    body = await response.json();
  } catch {
    // Non-JSON response (e.g. proxy error page)
  }

  if (!response.ok || !body?.success) {
    throw new ApiError(body?.message ?? `Request failed (${response.status})`, response.status);
  }
  return body.data as T;
};

export const fetchMovies = () => request<Movie[]>('');

export const fetchMovieById = (id: string) => request<Movie>(`/${encodeURIComponent(id)}`);

export const searchMovies = (title: string) =>
  request<Movie[]>(`/search?title=${encodeURIComponent(title)}`);

export const filterMovies = ({ genre, showDates }: { genre?: string; showDates?: string[] }) => {
  const params = new URLSearchParams();
  if (genre) params.set('genre', genre);
  showDates?.forEach(date => params.append('showDate', date));
  return request<Movie[]>(`/filter?${params}`);
};

export const fetchGenres = () => request<string[]>('/genres');
