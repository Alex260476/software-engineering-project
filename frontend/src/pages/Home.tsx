import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Calendar, Filter, Clock, AlertTriangle, X } from 'lucide-react';
import { fetchGenres, fetchMovies, filterMovies, searchMovies } from '../services/api';
import { Movie } from '../types';
import {
  DATE_FILTER_LABELS,
  DateFilter,
  bookingPath,
  datesForFilter,
  formatDate,
  formatShowDate,
  isShownOn,
  upcomingShowDates,
} from '../utils/format';

const ALL = 'All';

const Home = () => {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [genres, setGenres] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [selectedGenre, setSelectedGenre] = useState(ALL);
  const [dateFilter, setDateFilter] = useState<DateFilter>('any');
  const [reloadKey, setReloadKey] = useState(0);

  // Genre options come from the backend so they always match what the filter endpoint accepts
  useEffect(() => {
    fetchGenres().then(setGenres).catch(() => setGenres([]));
  }, []);

  // Wait until the user stops typing before hitting the search endpoint
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(searchQuery.trim()), 300);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const genre = selectedGenre !== ALL ? selectedGenre : undefined;
        const showDates = datesForFilter(dateFilter);
        let data: Movie[];
        if (showDates.length > 0) {
          // Show date (+ genre) is filtered in the DB; a title search narrows those results
          data = await filterMovies({ genre, showDates });
          if (debouncedQuery) {
            const query = debouncedQuery.toLowerCase();
            data = data.filter(m => m.title.toLowerCase().includes(query));
          }
        } else if (debouncedQuery && genre) {
          // No combined endpoint: search by title in the DB, then narrow by genre
          const results = await searchMovies(debouncedQuery);
          data = results.filter(m => m.genre.some(g => g.toLowerCase() === genre.toLowerCase()));
        } else if (debouncedQuery) {
          data = await searchMovies(debouncedQuery);
        } else if (genre) {
          data = await filterMovies({ genre });
        } else {
          data = await fetchMovies();
        }
        if (!cancelled) setMovies(data);
      } catch (e) {
        if (!cancelled) {
          setMovies([]);
          setError(e instanceof Error ? e.message : 'Something went wrong loading movies.');
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [debouncedQuery, selectedGenre, dateFilter, reloadKey]);

  const currentlyRunning = movies.filter(m => m.status === 'CURRENTLY_RUNNING');
  const comingSoon = movies.filter(m => m.status === 'COMING_SOON');
  const selectedDates = datesForFilter(dateFilter);
  const isFiltering = debouncedQuery !== '' || selectedGenre !== ALL || dateFilter !== 'any';

  const describeDates = () => {
    const labels = selectedDates.map(formatShowDate);
    if (dateFilter === 'weekend') return `this weekend (${labels.join(' & ')})`;
    return labels.join('').replace(/^(Today|Tomorrow)$/, l => l.toLowerCase());
  };

  const describeCriteria = () => {
    const parts: string[] = [];
    if (debouncedQuery) parts.push(`titles matching “${debouncedQuery}”`);
    if (selectedGenre !== ALL) parts.push(`the ${selectedGenre} genre`);
    let text = parts.join(' in ');
    if (dateFilter !== 'any') text = `${text ? `${text} ` : 'movies '}playing ${describeDates()}`;
    return text;
  };

  const clearFilters = () => {
    setSearchQuery('');
    setDebouncedQuery('');
    setSelectedGenre(ALL);
    setDateFilter('any');
  };

  // Date that a card's showtime buttons book: the first selected date the movie plays,
  // or its next upcoming show date when no date filter is set
  const cardShowDate = (movie: Movie) =>
    selectedDates.length > 0
      ? selectedDates.find(d => isShownOn(movie, d))
      : upcomingShowDates(movie)[0];

  const renderMovieCard = (movie: Movie) => (
    <div key={movie.id} className="group rounded-xl overflow-hidden glass hover:ring-2 hover:ring-primary transition-all duration-300 flex flex-col">
      <Link to={`/movie/${movie.id}${dateFilter !== 'any' && cardShowDate(movie) ? `?date=${cardShowDate(movie)}` : ''}`} className="relative block">
        <div className="aspect-[2/3] w-full overflow-hidden bg-surface">
          <img
            src={movie.posterUrl}
            alt={`${movie.title} poster`}
            loading="lazy"
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
        </div>
        <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/95 via-black/60 to-transparent p-4 pt-12">
          <h3 className="font-semibold text-lg text-white mb-1 line-clamp-2">{movie.title}</h3>
          <div className="flex items-center text-sm text-gray-300 gap-2">
            <span className="border border-white/30 rounded px-1.5 text-xs font-medium whitespace-nowrap flex-shrink-0">{movie.rating}</span>
            <span className="line-clamp-1">{movie.genre.join(' · ')}</span>
          </div>
        </div>
      </Link>

      <div className="p-3 border-t border-white/10 mt-auto">
        {movie.status === 'CURRENTLY_RUNNING' && movie.availableShowtimes?.length ? (
          (() => {
            const showDate = cardShowDate(movie);
            if (!showDate) {
              return <p className="text-xs text-muted">No showings this week</p>;
            }
            return (
              <>
                <p className="text-xs text-muted mb-1.5">{formatShowDate(showDate)}</p>
                <div className="flex flex-wrap gap-1.5">
                  {movie.availableShowtimes.map(time => (
                    <Link
                      key={time}
                      to={bookingPath(movie.id, time, showDate)}
                      title={`Book ${movie.title} · ${formatShowDate(showDate)} at ${time}`}
                      className="text-xs px-2 py-1 rounded-md bg-white/5 border border-white/10 text-white hover:bg-primary hover:border-primary transition-colors"
                    >
                      {time}
                    </Link>
                  ))}
                </div>
              </>
            );
          })()
        ) : (
          <p className="text-xs text-muted flex items-center gap-1.5">
            <Calendar className="w-3.5 h-3.5" />
            {movie.releaseDate ? `Opens ${formatDate(movie.releaseDate)}` : 'Release date TBA'}
          </p>
        )}
      </div>
    </div>
  );

  const renderMovieGrid = (movieList: Movie[], title: string, emptyText: string, id: string) => (
    <section id={id} className="mb-16 scroll-mt-24">
      <h2 className="text-2xl font-bold mb-6 text-white border-l-4 border-primary pl-3 flex items-baseline gap-3">
        {title}
        <span className="text-sm font-normal text-muted">{movieList.length} {movieList.length === 1 ? 'movie' : 'movies'}</span>
      </h2>
      {movieList.length === 0 ? (
        <p className="text-muted italic">{emptyText}</p>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
          {movieList.map(renderMovieCard)}
        </div>
      )}
    </section>
  );

  const renderResults = () => {
    if (loading && movies.length === 0) {
      return (
        <div className="flex items-center justify-center py-24">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
        </div>
      );
    }

    if (error) {
      return (
        <div className="glass rounded-xl p-8 text-center max-w-xl mx-auto mb-16">
          <AlertTriangle className="w-10 h-10 text-yellow-500 mx-auto mb-3" />
          <p className="text-white font-medium mb-1">Couldn't load movies</p>
          <p className="text-muted text-sm mb-5">{error}</p>
          <button
            onClick={() => setReloadKey(k => k + 1)}
            className="bg-primary hover:bg-blue-600 text-white text-sm font-medium px-5 py-2 rounded-lg transition-colors"
          >
            Try again
          </button>
        </div>
      );
    }

    if (isFiltering && movies.length === 0) {
      return (
        <div className="glass rounded-xl p-8 text-center max-w-xl mx-auto mb-16">
          <Search className="w-10 h-10 text-muted mx-auto mb-3" />
          <p className="text-white font-medium mb-1">No movies found</p>
          <p className="text-muted text-sm mb-5">We couldn't find any movies for {describeCriteria()}.</p>
          <button
            onClick={clearFilters}
            className="bg-white/10 hover:bg-white/20 text-white text-sm font-medium px-5 py-2 rounded-lg transition-colors"
          >
            Clear search and filters
          </button>
        </div>
      );
    }

    return (
      <div className={loading ? 'opacity-60 transition-opacity' : 'transition-opacity'}>
        {isFiltering && (
          <p className="text-muted text-sm mb-8">
            Showing {movies.length} {movies.length === 1 ? 'result' : 'results'} for {describeCriteria()}.
          </p>
        )}
        {renderMovieGrid(currentlyRunning, 'Currently Running', 'No currently running movies match.', 'now-playing')}
        {dateFilter === 'any' ? (
          renderMovieGrid(comingSoon, 'Coming Soon', 'No upcoming movies match.', 'coming-soon')
        ) : (
          <section id="coming-soon" className="mb-16 scroll-mt-24">
            <p className="text-muted text-sm">
              Coming Soon movies don't have showings yet.{' '}
              <button onClick={() => setDateFilter('any')} className="text-primary hover:underline">
                Show all dates
              </button>{' '}
              to see what's coming.
            </p>
          </section>
        )}
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Hero Section */}
      <div className="relative h-[50vh] md:h-[60vh] overflow-hidden">
        <div className="absolute inset-0">
          <img
            src="https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=1920&q=80"
            alt=""
            className="w-full h-full object-cover opacity-30"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-background via-background/50 to-transparent" />
        </div>
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="text-center px-4 max-w-3xl w-full">
            <h1 className="text-4xl md:text-6xl font-bold text-white mb-4 tracking-tight">Experience the <span className="text-primary">Magic</span> of Cinema</h1>
            <p className="text-lg md:text-xl text-white/80 mb-8">Book tickets for the latest blockbusters in stunning immersive quality.</p>

            {/* Search Bar */}
            <div className="relative max-w-xl mx-auto">
              <label htmlFor="movie-search" className="sr-only">Search movies by title</label>
              <input
                id="movie-search"
                type="search"
                placeholder="Search movies by title..."
                className="w-full bg-surface/80 backdrop-blur border border-white/10 text-white rounded-full py-4 pl-12 pr-12 focus:outline-none focus:ring-2 focus:ring-primary shadow-2xl [&::-webkit-search-cancel-button]:hidden"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
              {searchQuery && (
                <button
                  onClick={() => setSearchQuery('')}
                  aria-label="Clear search"
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-white"
                >
                  <X className="w-5 h-5" />
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-8 relative z-10">

        {/* Filters */}
        <div className="glass rounded-xl p-4 mb-10 flex flex-col md:flex-row gap-4 items-center justify-between">
          <div className="flex items-center gap-3 overflow-x-auto w-full pb-2 md:pb-0">
            <Filter className="text-primary w-5 h-5 flex-shrink-0" aria-label="Filter by genre" />
            {[ALL, ...genres].map(genre => (
              <button
                key={genre}
                onClick={() => setSelectedGenre(genre)}
                aria-pressed={selectedGenre === genre}
                className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${selectedGenre === genre ? 'bg-primary text-white' : 'bg-white/5 hover:bg-white/10 text-gray-300'}`}
              >
                {genre}
              </button>
            ))}
          </div>
          <div className="flex items-center gap-2 w-full md:w-auto">
            <Clock className="text-gray-400 w-5 h-5 flex-shrink-0" />
            <select
              aria-label="Filter by show date"
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value as DateFilter)}
              className={`bg-surface border text-sm rounded-lg px-3 py-2 text-white outline-none focus:ring-2 focus:ring-primary w-full md:w-44 ${dateFilter !== 'any' ? 'border-primary' : 'border-white/10'}`}
            >
              {(Object.keys(DATE_FILTER_LABELS) as DateFilter[]).map(option => (
                <option key={option} value={option}>{DATE_FILTER_LABELS[option]}</option>
              ))}
            </select>
          </div>
        </div>

        {renderResults()}

      </div>
    </div>
  );
};

export default Home;
