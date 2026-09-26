import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate, useSearchParams } from 'react-router-dom';
import { fetchMovieById } from '../services/api';
import { Movie, STATUS_LABELS } from '../types';
import { bookingPath, formatDate, formatRuntime, formatShowDate, upcomingShowDates } from '../utils/format';
import { Calendar, Clock, Star, PlayCircle, Info, Ticket, ChevronLeft, Users, Clapperboard } from 'lucide-react';

const MovieDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [movie, setMovie] = useState<Movie | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedShowtime, setSelectedShowtime] = useState<string | null>(null);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    setLoading(true);
    setError(null);
    setSelectedShowtime(null);
    fetchMovieById(id)
      .then(data => {
        if (cancelled) return;
        setMovie(data);
        // Preselect the date passed from the home page (?date=), else the next show date
        const dates = upcomingShowDates(data);
        const requested = searchParams.get('date');
        setSelectedDate(requested && dates.includes(requested) ? requested : dates[0] ?? null);
      })
      .catch(e => {
        if (cancelled) return;
        setMovie(null);
        setError(e.status === 404 ? 'We couldn’t find that movie.' : e.message);
      })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
    // Only refetch when the movie changes; ?date= is just the initial selection
  }, [id]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!movie) {
    return (
      <div className="text-center py-24 px-4">
        <p className="text-white text-xl font-medium mb-2">Movie unavailable</p>
        <p className="text-muted mb-6">{error ?? 'We couldn’t find that movie.'}</p>
        <Link to="/" className="text-primary hover:underline">Back to all movies</Link>
      </div>
    );
  }

  const runtime = formatRuntime(movie.runtime);
  const releaseDate = formatDate(movie.releaseDate);
  const showtimes = movie.availableShowtimes ?? [];
  const showDates = upcomingShowDates(movie);
  const isNowPlaying = movie.status === 'CURRENTLY_RUNNING';

  return (
    <div className="min-h-screen bg-background">
      {/* Backdrop */}
      <div className="relative min-h-[60vh] w-full flex items-end">
        <div className="absolute inset-0 overflow-hidden">
          <img src={movie.posterUrl} alt="" className="w-full h-full object-cover opacity-20 blur-sm scale-105" />
          <div className="absolute inset-0 bg-gradient-to-t from-background via-background/80 to-transparent" />
        </div>

        <div className="relative max-w-7xl mx-auto w-full px-4 sm:px-6 lg:px-8 pt-8 pb-12">
          <Link to="/" className="inline-flex items-center gap-1 text-sm text-gray-300 hover:text-white mb-8">
            <ChevronLeft className="w-4 h-4" /> All movies
          </Link>
          <div className="flex flex-col md:flex-row gap-8 md:items-end">
            <img src={movie.posterUrl} alt={`${movie.title} poster`} className="w-40 md:w-64 rounded-xl shadow-2xl border border-white/10" />
            <div className="flex-1">
              <div className="flex gap-2 mb-3">
                <span className="bg-primary/20 text-primary px-2 py-1 rounded text-xs font-bold border border-primary/30">{STATUS_LABELS[movie.status]}</span>
                <span className="bg-white/10 text-white px-2 py-1 rounded text-xs font-medium border border-white/10">Rated {movie.rating}</span>
              </div>
              <h1 className="text-4xl md:text-6xl font-bold text-white mb-4">{movie.title}</h1>
              <div className="flex flex-wrap gap-x-5 gap-y-2 text-sm text-gray-300">
                {runtime && <span className="flex items-center gap-1"><Clock className="w-4 h-4" /> {runtime}</span>}
                {releaseDate && <span className="flex items-center gap-1"><Calendar className="w-4 h-4" /> {isNowPlaying ? 'Released' : 'Opens'} {releaseDate}</span>}
                {movie.imdbRating != null && <span className="flex items-center gap-1"><Star className="w-4 h-4 text-yellow-500" /> {movie.imdbRating.toFixed(1)}/10</span>}
                <span>{movie.genre.join(' · ')}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-12">

          {/* Main Content */}
          <div className="lg:col-span-2 space-y-12">
            <section>
              <h2 className="text-2xl font-bold mb-4 flex items-center gap-2"><Info className="text-primary w-6 h-6"/> Synopsis</h2>
              <p className="text-gray-300 text-lg leading-relaxed mb-6">{movie.description}</p>
              <dl className="grid sm:grid-cols-2 gap-4 text-sm">
                {movie.director && (
                  <div>
                    <dt className="text-muted flex items-center gap-1.5 mb-1"><Clapperboard className="w-4 h-4" /> Director</dt>
                    <dd className="text-white">{movie.director}</dd>
                  </div>
                )}
                {movie.cast && movie.cast.length > 0 && (
                  <div>
                    <dt className="text-muted flex items-center gap-1.5 mb-1"><Users className="w-4 h-4" /> Cast</dt>
                    <dd className="text-white">{movie.cast.join(', ')}</dd>
                  </div>
                )}
              </dl>
            </section>

            <section>
              <h2 className="text-2xl font-bold mb-4 flex items-center gap-2"><PlayCircle className="text-primary w-6 h-6"/> Trailer</h2>
              <div className="aspect-video rounded-xl overflow-hidden border border-white/10 shadow-lg bg-black">
                <iframe
                  className="w-full h-full"
                  src={movie.trailerUrl}
                  title={`${movie.title} Trailer`}
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                  referrerPolicy="strict-origin-when-cross-origin"
                  allowFullScreen
                ></iframe>
              </div>
            </section>
          </div>

          {/* Sidebar / Showtimes */}
          <aside>
            <div className="glass rounded-2xl p-6 sticky top-24">
              <h3 className="text-xl font-bold mb-2">Showtimes</h3>
              {isNowPlaying && showtimes.length > 0 && showDates.length > 0 ? (
                <>
                  <p className="text-sm text-muted mb-5 pb-4 border-b border-white/10">Pick a date and time to book your tickets.</p>
                  <p className="text-xs uppercase tracking-wider text-muted mb-2">Date</p>
                  <div className="flex gap-2 overflow-x-auto pb-2 mb-4">
                    {showDates.map(date => (
                      <button
                        key={date}
                        onClick={() => setSelectedDate(date)}
                        aria-pressed={selectedDate === date}
                        className={`px-3 py-2 rounded-lg border text-sm whitespace-nowrap transition-colors ${
                          selectedDate === date
                            ? 'bg-primary border-primary text-white'
                            : 'bg-surface border-white/10 text-white hover:border-primary hover:bg-primary/10'
                        }`}
                      >
                        {formatShowDate(date)}
                      </button>
                    ))}
                  </div>
                  <p className="text-xs uppercase tracking-wider text-muted mb-2">Time</p>
                  <div className="grid grid-cols-3 gap-3">
                    {showtimes.map(time => (
                      <button
                        key={time}
                        onClick={() => setSelectedShowtime(time)}
                        aria-pressed={selectedShowtime === time}
                        className={`py-3 rounded-lg border font-medium transition-colors ${
                          selectedShowtime === time
                            ? 'bg-primary border-primary text-white'
                            : 'bg-surface border-white/10 text-white hover:border-primary hover:bg-primary/10'
                        }`}
                      >
                        {time}
                      </button>
                    ))}
                  </div>
                  <button
                    onClick={() => selectedShowtime && selectedDate && navigate(bookingPath(movie.id, selectedShowtime, selectedDate))}
                    disabled={!selectedShowtime || !selectedDate}
                    className="mt-6 w-full flex items-center justify-center gap-2 bg-primary hover:bg-blue-600 disabled:bg-white/10 disabled:text-muted disabled:cursor-not-allowed text-white font-bold py-3 rounded-lg transition-colors"
                  >
                    <Ticket className="w-5 h-5" />
                    {selectedShowtime && selectedDate
                      ? `Book · ${formatShowDate(selectedDate)}, ${selectedShowtime}`
                      : 'Select a showtime'}
                  </button>
                </>
              ) : (
                <p className="text-sm text-muted pt-2">
                  {isNowPlaying
                    ? 'No showings scheduled in the next week.'
                    : `${releaseDate ? `Opens ${releaseDate}. ` : ''}Showtimes and tickets will be available closer to release.`}
                </p>
              )}
            </div>
          </aside>

        </div>
      </div>
    </div>
  );
};

export default MovieDetails;
