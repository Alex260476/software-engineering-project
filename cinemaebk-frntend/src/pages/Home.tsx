import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Calendar, Filter, Star } from 'lucide-react';
import { fetchMovies } from '../services/mockData';
import { Movie } from '../types';

const GENRES = ['All', 'Action', 'Sci-Fi', 'Drama', 'Comedy', 'Horror', 'Animation', 'Adventure'];

const Home = () => {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedGenre, setSelectedGenre] = useState('All');

  useEffect(() => {
    const loadMovies = async () => {
      const data = await fetchMovies();
      setMovies(data);
      setLoading(false);
    };
    loadMovies();
  }, []);

  const filteredMovies = movies.filter((movie) => {
    const matchesSearch = movie.title.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesGenre = selectedGenre === 'All' || movie.genre.includes(selectedGenre);
    return matchesSearch && matchesGenre;
  });

  const currentlyRunning = filteredMovies.filter(m => m.status === 'Currently Running');
  const comingSoon = filteredMovies.filter(m => m.status === 'Coming Soon');

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
      </div>
    );
  }

  const renderMovieGrid = (movieList: Movie[], title: string, id?: string) => (
    <div id={id} className="mb-16">
      <h2 className="text-2xl font-bold mb-6 text-white border-l-4 border-primary pl-3">{title}</h2>
      {movieList.length === 0 ? (
        <p className="text-muted italic">No matches found.</p>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
          {movieList.map(movie => (
            <Link key={movie.id} to={`/movie/${movie.id}`} className="group relative rounded-xl overflow-hidden glass hover:ring-2 hover:ring-primary transition-all duration-300">
              <div className="aspect-[2/3] w-full overflow-hidden">
                <img src={movie.posterUrl} alt={movie.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
              </div>
              <div className="absolute inset-0 bg-gradient-to-t from-black/90 via-black/40 to-transparent opacity-100 flex flex-col justify-end p-4">
                <h3 className="font-semibold text-lg text-white mb-1 line-clamp-1">{movie.title}</h3>
                <div className="flex items-center text-sm text-muted gap-2 mb-2">
                  <span className="flex items-center gap-1"><Star className="w-3 h-3 text-yellow-500" /> {movie.rating}</span>
                  <span>•</span>
                  <span>{movie.genre[0]}</span>
                </div>
                <div className="flex justify-between items-center mt-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300 translate-y-2 group-hover:translate-y-0">
                  <span className="text-xs bg-primary text-white px-2 py-1 rounded font-medium">Book Now</span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );

  return (
    <div className="min-h-screen bg-background">
      {/* Hero Section */}
      <div className="relative h-[50vh] md:h-[60vh] overflow-hidden">
        <div className="absolute inset-0">
          <img 
            src="https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=1920&q=80" 
            alt="Hero Cinema" 
            className="w-full h-full object-cover opacity-30"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-background via-background/50 to-transparent" />
        </div>
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="text-center px-4 max-w-3xl">
            <h1 className="text-4xl md:text-6xl font-bold text-white mb-4 tracking-tight">Experience the <span className="text-primary">Magic</span> of Cinema</h1>
            <p className="text-lg md:text-xl text-white/80 mb-8">Book tickets for the latest blockbusters in stunning immersive quality.</p>
            
            {/* Search Bar */}
            <div className="relative max-w-xl mx-auto">
              <input 
                type="text" 
                placeholder="Search movies by title..." 
                className="w-full bg-surface/80 backdrop-blur border border-white/10 text-white rounded-full py-4 pl-12 pr-6 focus:outline-none focus:ring-2 focus:ring-primary shadow-2xl"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-8 relative z-10">
        
        {/* Filters */}
        <div className="glass rounded-xl p-4 mb-12 flex flex-col md:flex-row gap-4 items-center justify-between">
          <div className="flex items-center gap-3 overflow-x-auto w-full pb-2 md:pb-0 hide-scrollbar">
            <Filter className="text-primary w-5 h-5 flex-shrink-0" />
            {GENRES.map(genre => (
              <button 
                key={genre}
                onClick={() => setSelectedGenre(genre)}
                className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${selectedGenre === genre ? 'bg-primary text-white' : 'bg-white/5 hover:bg-white/10 text-gray-300'}`}
              >
                {genre}
              </button>
            ))}
          </div>
          <div className="flex items-center gap-2 w-full md:w-auto">
            <Calendar className="text-gray-400 w-5 h-5" />
            <select className="bg-surface border border-white/10 text-sm rounded-lg px-3 py-2 text-white outline-none w-full md:w-40">
              <option>Today</option>
              <option>Tomorrow</option>
              <option>This Weekend</option>
            </select>
          </div>
        </div>

        {/* Movie Sections */}
        {renderMovieGrid(currentlyRunning, "Currently Running", "now-playing")}
        {renderMovieGrid(comingSoon, "Coming Soon", "coming-soon")}

      </div>
    </div>
  );
};

export default Home;
