import { Movie } from '../types';

// 166 -> "2h 46m"
export const formatRuntime = (minutes?: number): string | null => {
  if (!minutes) return null;
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return h > 0 ? `${h}h ${m}m` : `${m}m`;
};

// "2026-10-16" -> Date at local midnight (new Date("2026-10-16") would be UTC and can shift a day)
export const parseISODate = (isoDate?: string | null): Date | null => {
  if (!isoDate) return null;
  const [y, mo, d] = isoDate.split('-').map(Number);
  if (!y || !mo || !d) return null;
  const date = new Date(y, mo - 1, d);
  return date.getMonth() === mo - 1 ? date : null;
};

// Date -> "2026-10-16" in local time
export const toISODate = (date: Date): string =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

// "2026-10-16" -> "Oct 16, 2026"
export const formatDate = (isoDate?: string): string | null => {
  const date = parseISODate(isoDate);
  if (!date) return isoDate ?? null;
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
};

export const today = (): Date => {
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
};

export const addDays = (date: Date, days: number): Date =>
  new Date(date.getFullYear(), date.getMonth(), date.getDate() + days);

// "2026-09-24" -> "Today", "Tomorrow", or "Sat, Sep 26"
export const formatShowDate = (isoDate: string): string => {
  const date = parseISODate(isoDate);
  if (!date) return isoDate;
  const diff = Math.round((date.getTime() - today().getTime()) / 86_400_000);
  if (diff === 0) return 'Today';
  if (diff === 1) return 'Tomorrow';
  return date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
};

const DAY_NAMES = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export const isShownOn = (movie: Movie, isoDate: string): boolean => {
  const date = parseISODate(isoDate);
  return !!date && movie.status === 'CURRENTLY_RUNNING' && !!movie.showDays?.includes(DAY_NAMES[date.getDay()]);
};

// Upcoming dates (from today) on which the movie is shown
export const upcomingShowDates = (movie: Movie, days = 7): string[] =>
  Array.from({ length: days }, (_, i) => toISODate(addDays(today(), i))).filter(d => isShownOn(movie, d));

// Show-date filter options on the home page
export type DateFilter = 'any' | 'today' | 'tomorrow' | 'weekend';

export const DATE_FILTER_LABELS: Record<DateFilter, string> = {
  any: 'Any date',
  today: 'Today',
  tomorrow: 'Tomorrow',
  weekend: 'This Weekend',
};

export const datesForFilter = (filter: DateFilter): string[] => {
  const start = today();
  switch (filter) {
    case 'today':
      return [toISODate(start)];
    case 'tomorrow':
      return [toISODate(addDays(start, 1))];
    case 'weekend': {
      const dow = start.getDay();
      if (dow === 0) return [toISODate(start)]; // It's Sunday: only today is left
      const saturday = addDays(start, 6 - dow);
      return [toISODate(saturday), toISODate(addDays(saturday, 1))];
    }
    default:
      return [];
  }
};

export const bookingPath = (movieId: string, showtime: string, date?: string) =>
  `/booking/${encodeURIComponent(movieId)}/${encodeURIComponent(showtime)}${date ? `?date=${date}` : ''}`;
