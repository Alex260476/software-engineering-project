import React, { useEffect, useState } from 'react';
import {
  Clock,
  Film,
  Percent,
  Plus,
  Search,
  Shield,
  Tag,
  Users,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { fetchAllUsers, fetchMovies } from '../services/api';
import { Movie, User } from '../types';
import { useNavigate } from 'react-router-dom';

const AdminPortal: React.FC = () => {
  const { user, isAdmin, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<'movies' | 'promotions' | 'users' | 'showtimes'>('movies');

  const [movies, setMovies] = useState<Movie[]>([]);
  const [usersList, setUsersList] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchUserQuery, setSearchUserQuery] = useState('');

  // Sample promotions data for prototype
  const [promotions] = useState([
    { code: 'SUMMER20', discount: 20, startDate: '2026-06-01', endDate: '2026-08-31', status: 'ACTIVE' },
    { code: 'STUDENT10', discount: 10, startDate: '2026-01-01', endDate: '2026-12-31', status: 'ACTIVE' },
    { code: 'EARLYBIRD15', discount: 15, startDate: '2026-09-01', endDate: '2026-11-30', status: 'ACTIVE' },
    { code: 'CINEMAFEST', discount: 25, startDate: '2026-10-01', endDate: '2026-10-31', status: 'ACTIVE' },
  ]);

  useEffect(() => {
    if (!isAuthenticated || !isAdmin) {
      navigate('/');
    }
  }, [isAuthenticated, isAdmin, navigate]);

  useEffect(() => {
    loadData();
  }, [activeTab]);

  const loadData = async () => {
    setLoading(true);
    try {
      if (activeTab === 'movies' || activeTab === 'showtimes') {
        const m = await fetchMovies();
        setMovies(m);
      } else if (activeTab === 'users' && user?.id) {
        const u = await fetchAllUsers(user.id);
        setUsersList(u);
      }
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  const filteredUsers = usersList.filter(u =>
    `${u.firstName} ${u.lastName} ${u.email}`.toLowerCase().includes(searchUserQuery.toLowerCase())
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      {/* Admin Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8 pb-6 border-b border-white/10">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-primary/20 text-primary border border-primary/30 flex items-center gap-1">
              <Shield className="w-3 h-3" /> Admin Portal
            </span>
            <span className="text-xs text-muted">Sprint 2 Prototype</span>
          </div>
          <h1 className="text-3xl font-bold text-white tracking-tight">System Administration</h1>
          <div className="flex items-center gap-3 mt-1">
            <p className="text-sm text-muted">Logged in as {user?.firstName} {user?.lastName} ({user?.email})</p>
            {loading && <span className="text-xs text-primary animate-pulse">● Loading...</span>}
          </div>
        </div>
      </div>

      {/* Required Admin Menu: Manage Movies, Promotions, Users, Showtimes */}
      <div className="flex flex-wrap gap-2 mb-8 bg-surface/80 p-1.5 rounded-2xl border border-white/10 w-fit">
        <button
          onClick={() => setActiveTab('movies')}
          className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold transition-all ${
            activeTab === 'movies'
              ? 'bg-primary text-white shadow-lg shadow-primary/20'
              : 'text-gray-300 hover:text-white hover:bg-white/5'
          }`}
        >
          <Film className="w-4 h-4" /> Manage Movies
        </button>
        <button
          onClick={() => setActiveTab('promotions')}
          className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold transition-all ${
            activeTab === 'promotions'
              ? 'bg-primary text-white shadow-lg shadow-primary/20'
              : 'text-gray-300 hover:text-white hover:bg-white/5'
          }`}
        >
          <Percent className="w-4 h-4" /> Promotions
        </button>
        <button
          onClick={() => setActiveTab('users')}
          className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold transition-all ${
            activeTab === 'users'
              ? 'bg-primary text-white shadow-lg shadow-primary/20'
              : 'text-gray-300 hover:text-white hover:bg-white/5'
          }`}
        >
          <Users className="w-4 h-4" /> Users
        </button>
        <button
          onClick={() => setActiveTab('showtimes')}
          className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold transition-all ${
            activeTab === 'showtimes'
              ? 'bg-primary text-white shadow-lg shadow-primary/20'
              : 'text-gray-300 hover:text-white hover:bg-white/5'
          }`}
        >
          <Clock className="w-4 h-4" /> Showtimes
        </button>
      </div>

      {/* TAB 1: MANAGE MOVIES */}
      {activeTab === 'movies' && (
        <div className="bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h2 className="text-xl font-bold text-white mb-1">Movie Inventory ({movies.length})</h2>
              <p className="text-xs text-muted">Currently loaded movies from MongoDB</p>
            </div>
            <button
              onClick={() => alert('Add Movie Modal Prototype: In Sprint 3 this will persist custom movie releases.')}
              className="py-2 px-4 bg-primary hover:bg-blue-600 text-white text-xs font-semibold rounded-xl flex items-center gap-2 shadow-lg shadow-primary/20"
            >
              <Plus className="w-4 h-4" /> Add New Movie
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-white/5 text-muted uppercase tracking-wider text-[11px] border-b border-white/10">
                <tr>
                  <th className="py-3 px-4">Poster</th>
                  <th className="py-3 px-4">Title</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Rating</th>
                  <th className="py-3 px-4">Runtime</th>
                  <th className="py-3 px-4">Genres</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {movies.map(movie => (
                  <tr key={movie.id} className="hover:bg-white/5 transition-colors">
                    <td className="py-3 px-4">
                      <img
                        src={movie.posterUrl}
                        alt={movie.title}
                        className="w-10 h-14 object-cover rounded shadow"
                      />
                    </td>
                    <td className="py-3 px-4 font-semibold text-white">{movie.title}</td>
                    <td className="py-3 px-4">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                          movie.status === 'CURRENTLY_RUNNING'
                            ? 'bg-green-500/10 text-green-400 border border-green-500/20'
                            : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                        }`}
                      >
                        {movie.status === 'CURRENTLY_RUNNING' ? 'Now Playing' : 'Coming Soon'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-gray-300">{movie.rating}</td>
                    <td className="py-3 px-4 text-gray-300">{movie.runtime ? `${movie.runtime}m` : 'N/A'}</td>
                    <td className="py-3 px-4 text-muted">{movie.genre?.join(', ')}</td>
                    <td className="py-3 px-4 text-right space-x-2">
                      <button
                        onClick={() => alert(`Edit Movie Prototype for "${movie.title}"`)}
                        className="px-2.5 py-1 bg-white/5 hover:bg-white/10 border border-white/10 rounded text-gray-300"
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: PROMOTIONS */}
      {activeTab === 'promotions' && (
        <div className="bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h2 className="text-xl font-bold text-white mb-1">Promotional Codes & Discounts</h2>
              <p className="text-xs text-muted">Manage active discount campaigns and coupon codes</p>
            </div>
            <button
              onClick={() => alert('Create Promotion Prototype: Add promo code and percentage.')}
              className="py-2 px-4 bg-primary hover:bg-blue-600 text-white text-xs font-semibold rounded-xl flex items-center gap-2 shadow-lg shadow-primary/20"
            >
              <Plus className="w-4 h-4" /> Create Promotion
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-white/5 text-muted uppercase tracking-wider text-[11px] border-b border-white/10">
                <tr>
                  <th className="py-3 px-4">Promo Code</th>
                  <th className="py-3 px-4">Discount</th>
                  <th className="py-3 px-4">Start Date</th>
                  <th className="py-3 px-4">End Date</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {promotions.map((promo, idx) => (
                  <tr key={idx} className="hover:bg-white/5 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-primary flex items-center gap-2">
                      <Tag className="w-3.5 h-3.5" /> {promo.code}
                    </td>
                    <td className="py-3 px-4 text-white font-semibold">{promo.discount}% OFF</td>
                    <td className="py-3 px-4 text-gray-300">{promo.startDate}</td>
                    <td className="py-3 px-4 text-gray-300">{promo.endDate}</td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-green-500/10 text-green-400 border border-green-500/20">
                        {promo.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => alert(`Edit promotion ${promo.code}`)}
                        className="px-2.5 py-1 bg-white/5 hover:bg-white/10 border border-white/10 rounded text-gray-300"
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 3: USERS */}
      {activeTab === 'users' && (
        <div className="bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h2 className="text-xl font-bold text-white mb-1">Registered User Directory ({usersList.length})</h2>
              <p className="text-xs text-muted">All active, inactive, and admin accounts stored in MongoDB</p>
            </div>
            <div className="relative w-full sm:w-64">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted" />
              <input
                type="text"
                placeholder="Search users..."
                value={searchUserQuery}
                onChange={e => setSearchUserQuery(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 bg-background border border-white/10 rounded-xl text-xs text-white placeholder-muted focus:outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-white/5 text-muted uppercase tracking-wider text-[11px] border-b border-white/10">
                <tr>
                  <th className="py-3 px-4">Name</th>
                  <th className="py-3 px-4">Email</th>
                  <th className="py-3 px-4">Role</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Verified</th>
                  <th className="py-3 px-4">Stored Cards</th>
                  <th className="py-3 px-4">Favorites</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {filteredUsers.map(u => (
                  <tr key={u.id} className="hover:bg-white/5 transition-colors">
                    <td className="py-3 px-4 font-semibold text-white">
                      {u.firstName} {u.lastName}
                    </td>
                    <td className="py-3 px-4 text-gray-300 font-mono text-[11px]">{u.email}</td>
                    <td className="py-3 px-4">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                          u.userType === 'ADMIN'
                            ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                            : 'bg-blue-500/10 text-blue-400 border border-blue-500/20'
                        }`}
                      >
                        {u.userType}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                          u.status === 'ACTIVE'
                            ? 'bg-green-500/10 text-green-400 border border-green-500/20'
                            : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                        }`}
                      >
                        {u.status}
                      </span>
                    </td>
                    <td className="py-3 px-4">
                      {u.emailVerified ? (
                        <span className="text-green-400 font-medium">✓ Yes</span>
                      ) : (
                        <span className="text-amber-400 font-medium">No (Inactive)</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-muted">{u.paymentCards?.length || 0} / 3</td>
                    <td className="py-3 px-4 text-muted">{u.favoriteMovieIds?.length || 0}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 4: SHOWTIMES */}
      {activeTab === 'showtimes' && (
        <div className="bg-surface border border-white/10 rounded-2xl p-6 sm:p-8 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h2 className="text-xl font-bold text-white mb-1">Showtime Schedule Management</h2>
              <p className="text-xs text-muted">Auditorium screen assignments and scheduled movie showtimes</p>
            </div>
            <button
              onClick={() => alert('Schedule Showtime Prototype: Add movie to auditorium timetable.')}
              className="py-2 px-4 bg-primary hover:bg-blue-600 text-white text-xs font-semibold rounded-xl flex items-center gap-2 shadow-lg shadow-primary/20"
            >
              <Plus className="w-4 h-4" /> Schedule Showtime
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div className="p-4 bg-background/60 border border-white/10 rounded-xl">
              <p className="text-xs text-muted">Auditorium 1 (Standard)</p>
              <p className="text-lg font-bold text-white mt-1">70 Seats</p>
              <p className="text-xs text-green-400 mt-1">● 3 Showings scheduled</p>
            </div>
            <div className="p-4 bg-background/60 border border-white/10 rounded-xl">
              <p className="text-xs text-muted">Auditorium 2 (IMAX Laser)</p>
              <p className="text-lg font-bold text-white mt-1">120 Seats</p>
              <p className="text-xs text-green-400 mt-1">● 3 Showings scheduled</p>
            </div>
            <div className="p-4 bg-background/60 border border-white/10 rounded-xl">
              <p className="text-xs text-muted">Auditorium 3 (Dolby Cinema)</p>
              <p className="text-lg font-bold text-white mt-1">90 Seats</p>
              <p className="text-xs text-green-400 mt-1">● 3 Showings scheduled</p>
            </div>
          </div>

          <div className="space-y-3">
            <h3 className="text-sm font-semibold text-white">Daily Showings Matrix</h3>
            {movies
              .filter(m => m.status === 'CURRENTLY_RUNNING')
              .map(movie => (
                <div
                  key={movie.id}
                  className="p-4 bg-background/40 border border-white/10 rounded-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                >
                  <div className="flex items-center gap-3">
                    <img src={movie.posterUrl} alt="" className="w-10 h-14 object-cover rounded" />
                    <div>
                      <p className="font-semibold text-white text-sm">{movie.title}</p>
                      <p className="text-xs text-muted">
                        Auditorium 1 · Scheduled Days: {movie.showDays?.join(', ') || 'All Week'}
                      </p>
                    </div>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {movie.availableShowtimes?.map((time, idx) => (
                      <span
                        key={idx}
                        className="px-3 py-1 bg-white/5 border border-white/10 rounded-lg text-xs font-mono text-gray-200"
                      >
                        {time}
                      </span>
                    ))}
                  </div>
                </div>
              ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminPortal;
