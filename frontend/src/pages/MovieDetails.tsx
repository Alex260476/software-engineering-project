import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { fetchMovieById } from '../services/mockData';
import { Movie } from '../types';
import { Calendar, Clock, Star, PlayCircle, Info } from 'lucide-react';

const SHOWTIMES = ['2:00 PM', '5:00 PM', '8:00 PM'];

const MovieDetails = () => {
  const { id } = useParams();
  const [movie, setMovie] = useState<Movie | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadMovie = async () => {
      if (id) {
        const data = await fetchMovieById(id);
        setMovie(data || null);
      }
      setLoading(false);
    };
    loadMovie();
  }, [id]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!movie) {
    return <div className="text-center p-12 text-white">Movie not found.</div>;
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Backdrop */}
      <div className="relative h-[60vh] w-full">
        <div className="absolute inset-0">
          <img src={movie.posterUrl} alt={movie.title} className="w-full h-full object-cover opacity-20" />
          <div className="absolute inset-0 bg-gradient-to-t from-background via-background/80 to-transparent" />
        </div>
        
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 absolute inset-0 flex items-end pb-12">
          <div className="flex flex-col md:flex-row gap-8 items-end">
            <img src={movie.posterUrl} alt={movie.title} className="w-48 md:w-64 rounded-xl shadow-2xl border border-white/10 hidden md:block" />
            <div className="flex-1">
              <div className="flex gap-2 mb-3">
                <span className="bg-primary/20 text-primary px-2 py-1 rounded text-xs font-bold border border-primary/30">{movie.status}</span>
                <span className="bg-white/10 text-white px-2 py-1 rounded text-xs font-medium border border-white/10">{movie.rating}</span>
              </div>
              <h1 className="text-4xl md:text-6xl font-bold text-white mb-4">{movie.title}</h1>
              <div className="flex flex-wrap gap-4 text-sm text-gray-300 mb-6">
                <span className="flex items-center gap-1"><Clock className="w-4 h-4" /> {movie.duration || 'N/A'}</span>
                <span className="flex items-center gap-1"><Calendar className="w-4 h-4" /> {movie.releaseDate || 'N/A'}</span>
                <span className="flex items-center gap-1"><Star className="w-4 h-4 text-yellow-500" /> {movie.genre.join(', ')}</span>
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
              <p className="text-gray-300 text-lg leading-relaxed">{movie.description}</p>
            </section>

            <section>
              <h2 className="text-2xl font-bold mb-4 flex items-center gap-2"><PlayCircle className="text-primary w-6 h-6"/> Trailer</h2>
              <div className="aspect-video rounded-xl overflow-hidden border border-white/10 shadow-lg">
                <iframe 
                  className="w-full h-full"
                  src={movie.trailerUrl} 
                  title={`${movie.title} Trailer`}
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                  allowFullScreen
                ></iframe>
              </div>
            </section>
          </div>

          {/* Sidebar / Showtimes */}
          <div>
            <div className="glass rounded-2xl p-6 sticky top-24">
              <h3 className="text-xl font-bold mb-6 border-b border-white/10 pb-4">Select Showtime</h3>
              <p className="text-sm text-muted mb-4">Choose a time below to book your tickets.</p>
              <div className="space-y-3">
                {SHOWTIMES.map(time => (
                  <Link 
                    key={time}
                    to={`/booking/${movie.id}/${time}`}
                    className="block w-full text-center py-3 rounded-lg bg-surface border border-white/10 hover:border-primary hover:bg-primary/10 transition-colors font-medium text-white"
                  >
                    {time}
                  </Link>
                ))}
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};

export default MovieDetails;
