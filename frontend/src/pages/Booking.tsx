import { useState, useEffect, useMemo } from 'react';
import { useParams, Link, useSearchParams } from 'react-router-dom';
import { TicketCategory, TicketCategoryType, Seat, Movie } from '../types';
import { fetchMovieById } from '../services/api';
import { formatShowDate, isShownOn, parseISODate, today, upcomingShowDates } from '../utils/format';
import { ChevronLeft, Monitor, CheckCircle, Info, Minus, Plus } from 'lucide-react';

const TICKET_CATEGORIES: TicketCategory[] = [
  { type: 'Adult', price: 15.00 },
  { type: 'Child', price: 10.00 },
  { type: 'Senior', price: 12.00 },
];

const ROWS = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];
const SEATS_PER_ROW = 10;
const MAX_TICKETS = 10;

// Small seeded PRNG so a given movie + date + showtime always shows the same "taken" seats
const seededRandom = (seedText: string) => {
  let seed = 0;
  for (const ch of seedText) seed = (seed * 31 + ch.charCodeAt(0)) >>> 0;
  return () => {
    seed = (seed * 1664525 + 1013904223) >>> 0;
    return seed / 2 ** 32;
  };
};

// Prototype seat map; real availability comes from the backend in a later sprint
const generateSeats = (seedText: string): Seat[] => {
  const random = seededRandom(seedText);
  return ROWS.flatMap(row =>
    Array.from({ length: SEATS_PER_ROW }, (_, i) => ({
      id: `${row}${i + 1}`,
      row,
      number: i + 1,
      isAvailable: random() > 0.25,
      isSelected: false,
    }))
  );
};

const Booking = () => {
  const { movieId, showtime } = useParams();
  const [searchParams] = useSearchParams();
  const [movie, setMovie] = useState<Movie | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tickets, setTickets] = useState<Record<TicketCategoryType, number>>({ Adult: 0, Child: 0, Senior: 0 });
  const [seats, setSeats] = useState<Seat[]>([]);
  const [notice, setNotice] = useState<string | null>(null);

  useEffect(() => {
    if (!movieId) return;
    let cancelled = false;
    setLoading(true);
    fetchMovieById(movieId)
      .then(m => { if (!cancelled) setMovie(m); })
      .catch(e => { if (!cancelled) setError(e.status === 404 ? 'We couldn’t find that movie.' : e.message); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [movieId]);

  // Links without ?date= (e.g. older bookmarks) book the movie's next show date
  const showDate = searchParams.get('date') ?? (movie ? upcomingShowDates(movie)[0] : undefined);

  useEffect(() => {
    setSeats(generateSeats(`${movieId}|${showDate}|${showtime}`));
    setTickets({ Adult: 0, Child: 0, Senior: 0 });
    setNotice(null);
  }, [movieId, showDate, showtime]);

  const totalTickets = Object.values(tickets).reduce((sum, count) => sum + count, 0);
  const selectedSeats = useMemo(() => seats.filter(s => s.isSelected), [seats]);
  const total = TICKET_CATEGORIES.reduce((sum, cat) => sum + tickets[cat.type] * cat.price, 0);

  const updateTicketCount = (type: TicketCategoryType, delta: number) => {
    const newCount = tickets[type] + delta;
    if (newCount < 0 || totalTickets + delta > MAX_TICKETS) return;
    setTickets({ ...tickets, [type]: newCount });
    setNotice(null);

    // Fewer tickets than selected seats: release the most recently listed extra seats
    const newTotal = totalTickets + delta;
    if (selectedSeats.length > newTotal) {
      const keep = new Set(selectedSeats.slice(0, newTotal).map(s => s.id));
      setSeats(prev => prev.map(s => (s.isSelected && !keep.has(s.id) ? { ...s, isSelected: false } : s)));
    }
  };

  const toggleSeat = (seat: Seat) => {
    if (!seat.isAvailable) return;
    if (!seat.isSelected) {
      if (totalTickets === 0) {
        setNotice('Choose how many tickets you need first.');
        return;
      }
      if (selectedSeats.length >= totalTickets) {
        setNotice(`You've already selected ${totalTickets} ${totalTickets === 1 ? 'seat' : 'seats'}. Deselect one or add another ticket.`);
        return;
      }
    }
    setNotice(null);
    setSeats(prev => prev.map(s => (s.id === seat.id ? { ...s, isSelected: !s.isSelected } : s)));
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  const showDateValue = parseISODate(showDate);
  const validShowtime =
    movie?.status === 'CURRENTLY_RUNNING' &&
    !!showtime &&
    !!movie.availableShowtimes?.includes(showtime) &&
    !!showDate &&
    !!showDateValue &&
    showDateValue >= today() &&
    isShownOn(movie, showDate);
  if (!movie || !validShowtime || !showDate) {
    return (
      <div className="text-center py-24 px-4">
        <p className="text-white text-xl font-medium mb-2">Booking unavailable</p>
        <p className="text-muted mb-6">{error ?? 'That showtime isn’t available for booking.'}</p>
        <Link to={movie ? `/movie/${movie.id}` : '/'} className="text-primary hover:underline">
          {movie ? `Back to ${movie.title}` : 'Back to all movies'}
        </Link>
      </div>
    );
  }

  const seatsReady = totalTickets > 0 && selectedSeats.length === totalTickets;

  return (
    <div className="min-h-screen bg-background pb-20">
      <div className="bg-surface/95 backdrop-blur border-b border-white/10 sticky top-16 z-40">
        <div className="max-w-6xl mx-auto px-4 py-4 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3 min-w-0">
            <Link to={`/movie/${movie.id}`} aria-label="Back to movie details" className="p-2 hover:bg-white/10 rounded-full transition-colors text-white">
              <ChevronLeft className="w-5 h-5" />
            </Link>
            <img src={movie.posterUrl} alt="" className="w-10 h-14 object-cover rounded hidden sm:block" />
            <div className="min-w-0">
              <h1 className="text-xl font-bold text-white leading-tight truncate">{movie.title}</h1>
              <p className="text-sm text-primary font-medium">{formatShowDate(showDate)} · {showtime} · Rated {movie.rating}</p>
            </div>
          </div>
          <div className="text-right flex-shrink-0">
            <p className="text-sm text-muted">Total</p>
            <p className="text-2xl font-bold text-white">${total.toFixed(2)}</p>
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

          {/* Ticket Selection */}
          <div className="space-y-6">
            <div className="glass p-6 rounded-2xl">
              <h2 className="text-lg font-bold mb-1 text-white">1. Select Tickets</h2>
              <p className="text-xs text-muted mb-5">Up to {MAX_TICKETS} tickets per booking.</p>
              <div className="space-y-4">
                {TICKET_CATEGORIES.map(category => (
                  <div key={category.type} className="flex items-center justify-between">
                    <div>
                      <p className="font-medium text-white">{category.type}</p>
                      <p className="text-sm text-muted">${category.price.toFixed(2)} each</p>
                    </div>
                    <div className="flex items-center gap-3">
                      <button
                        onClick={() => updateTicketCount(category.type, -1)}
                        aria-label={`Remove ${category.type} ticket`}
                        className="w-8 h-8 rounded-full border border-white/10 flex items-center justify-center text-white hover:border-primary disabled:opacity-30 disabled:hover:border-white/10 transition-colors"
                        disabled={tickets[category.type] === 0}
                      ><Minus className="w-4 h-4" /></button>
                      <span className="w-5 text-center text-white tabular-nums" aria-live="polite">{tickets[category.type]}</span>
                      <button
                        onClick={() => updateTicketCount(category.type, 1)}
                        aria-label={`Add ${category.type} ticket`}
                        className="w-8 h-8 rounded-full border border-white/10 flex items-center justify-center text-white hover:border-primary disabled:opacity-30 disabled:hover:border-white/10 transition-colors"
                        disabled={totalTickets >= MAX_TICKETS}
                      ><Plus className="w-4 h-4" /></button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Order Summary */}
            <div className="glass p-6 rounded-2xl">
              <h2 className="text-lg font-bold mb-4 text-white">Order Summary</h2>
              {totalTickets === 0 ? (
                <p className="text-sm text-muted">No tickets selected yet.</p>
              ) : (
                <dl className="space-y-2 text-sm">
                  {TICKET_CATEGORIES.filter(c => tickets[c.type] > 0).map(c => (
                    <div key={c.type} className="flex justify-between text-gray-300">
                      <dt>{tickets[c.type]} × {c.type}</dt>
                      <dd>${(tickets[c.type] * c.price).toFixed(2)}</dd>
                    </div>
                  ))}
                  <div className="flex justify-between text-gray-300">
                    <dt>Seats</dt>
                    <dd>{selectedSeats.length ? selectedSeats.map(s => s.id).join(', ') : '—'}</dd>
                  </div>
                  <div className="flex justify-between text-white font-bold pt-3 mt-3 border-t border-white/10">
                    <dt>Total</dt>
                    <dd>${total.toFixed(2)}</dd>
                  </div>
                </dl>
              )}
            </div>
          </div>

          {/* Seating Layout */}
          <div className="lg:col-span-2 glass p-6 rounded-2xl">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-lg font-bold text-white">2. Choose Seats</h2>
              <p className="text-sm text-muted">
                Selected <span className="text-white font-medium">{selectedSeats.length} / {totalTickets}</span>
              </p>
            </div>

            <div className="overflow-x-auto">
              <div className="min-w-[420px]">
                <p className="text-xs uppercase tracking-widest text-muted text-center flex items-center justify-center gap-2 mb-2">
                  <Monitor className="w-4 h-4" /> Screen
                </p>
                <div className="w-full bg-gradient-to-b from-primary/60 to-transparent h-1.5 mb-10 rounded-full mx-auto max-w-md shadow-[0_0_24px_rgba(59,130,246,0.35)]"></div>

                <div className="grid gap-3 justify-center mb-8">
                  {ROWS.map(row => (
                    <div key={row} className="flex gap-2 items-center justify-center">
                      <span className="w-6 text-center text-xs font-bold text-muted">{row}</span>
                      <div className="flex gap-2">
                        {seats.filter(s => s.row === row).map(seat => (
                          <button
                            key={seat.id}
                            onClick={() => toggleSeat(seat)}
                            disabled={!seat.isAvailable}
                            aria-label={`Seat ${seat.id}${!seat.isAvailable ? ', taken' : seat.isSelected ? ', selected' : ''}`}
                            aria-pressed={seat.isSelected}
                            title={seat.id}
                            className={`w-7 h-7 sm:w-8 sm:h-8 rounded-t-lg rounded-b-sm text-[10px] font-medium flex items-center justify-center transition-colors ${
                              !seat.isAvailable ? 'bg-neutral-800/60 text-neutral-600 cursor-not-allowed' :
                              seat.isSelected ? 'bg-primary text-white shadow-[0_0_10px_rgba(59,130,246,0.6)]' :
                              'bg-white/15 hover:bg-primary/50 text-transparent hover:text-white'
                            }`}
                          >
                            {seat.isAvailable ? seat.number : '×'}
                          </button>
                        ))}
                      </div>
                      <span className="w-6 text-center text-xs font-bold text-muted">{row}</span>
                    </div>
                  ))}
                </div>

                <div className="flex justify-center gap-6 text-sm text-muted border-t border-white/10 pt-6">
                  <div className="flex items-center gap-2"><div className="w-4 h-4 bg-white/15 rounded"></div> Available</div>
                  <div className="flex items-center gap-2"><div className="w-4 h-4 bg-primary rounded"></div> Selected</div>
                  <div className="flex items-center gap-2"><div className="w-4 h-4 bg-neutral-800/60 rounded text-neutral-600 text-[10px] leading-4 text-center">×</div> Taken</div>
                </div>
              </div>
            </div>

            {notice && (
              <p role="status" className="mt-6 text-sm text-yellow-400 flex items-center gap-2">
                <Info className="w-4 h-4 flex-shrink-0" /> {notice}
              </p>
            )}

            <div className="mt-8 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <p className="text-sm text-muted">
                {totalTickets === 0
                  ? 'Add tickets to start choosing seats.'
                  : seatsReady
                    ? 'All set! Review your order and continue.'
                    : `Select ${totalTickets - selectedSeats.length} more ${totalTickets - selectedSeats.length === 1 ? 'seat' : 'seats'}.`}
              </p>
              <button
                disabled={!seatsReady}
                onClick={() => setNotice('Checkout isn’t available yet. Payment will be added in a later sprint.')}
                className="bg-primary hover:bg-blue-600 disabled:bg-white/10 disabled:text-muted disabled:cursor-not-allowed text-white font-bold py-3 px-8 rounded-lg transition-colors flex items-center justify-center gap-2"
              >
                <CheckCircle className="w-5 h-5" />
                Proceed to Payment
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Booking;
