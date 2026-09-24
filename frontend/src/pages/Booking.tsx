import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { TicketCategory, Seat, Movie } from '../types';
import { fetchMovieById } from '../services/mockData';
import { ChevronLeft, Monitor, CheckCircle } from 'lucide-react';

const TICKET_CATEGORIES: TicketCategory[] = [
  { type: 'Adult', price: 15.00 },
  { type: 'Child', price: 10.00 },
  { type: 'Senior', price: 12.00 },
];

const generateSeats = (): Seat[] => {
  const seats: Seat[] = [];
  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];
  const seatsPerRow = 10;

  rows.forEach(row => {
    for (let i = 1; i <= seatsPerRow; i++) {
      seats.push({
        id: `${row}${i}`,
        row,
        number: i,
        // Randomly make some seats unavailable for demonstration
        isAvailable: Math.random() > 0.3,
        isSelected: false
      });
    }
  });
  return seats;
};

const Booking = () => {
  const { movieId, showtime } = useParams();
  const [movie, setMovie] = useState<Movie | null>(null);
  const [tickets, setTickets] = useState<Record<string, number>>({ Adult: 0, Child: 0, Senior: 0 });
  const [seats, setSeats] = useState<Seat[]>([]);

  useEffect(() => {
    const load = async () => {
      if (movieId) {
        const m = await fetchMovieById(movieId);
        setMovie(m || null);
      }
      setSeats(generateSeats());
    };
    load();
  }, [movieId]);

  const updateTicketCount = (type: string, delta: number) => {
    setTickets(prev => {
      const newCount = prev[type] + delta;
      if (newCount < 0) return prev;
      return { ...prev, [type]: newCount };
    });
  };

  const totalTickets = Object.values(tickets).reduce((sum, count) => sum + count, 0);
  const selectedSeatsCount = seats.filter(s => s.isSelected).length;

  const toggleSeat = (id: string) => {
    setSeats(prev => prev.map(seat => {
      if (seat.id === id) {
        if (!seat.isAvailable) return seat;
        // Check if they have reached the ticket limit
        if (!seat.isSelected && selectedSeatsCount >= totalTickets && totalTickets > 0) {
          alert('You have selected all your seats based on the ticket quantity.');
          return seat;
        }
        return { ...seat, isSelected: !seat.isSelected };
      }
      return seat;
    }));
  };

  const calculateTotal = () => {
    return TICKET_CATEGORIES.reduce((total, cat) => total + (tickets[cat.type] * cat.price), 0);
  };

  if (!movie) return <div className="text-center p-12 text-white">Loading...</div>;

  return (
    <div className="min-h-screen bg-background pb-20">
      <div className="bg-surface border-b border-white/10 sticky top-16 z-40">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Link to={`/movie/${movieId}`} className="p-2 hover:bg-white/10 rounded-full transition-colors text-white">
              <ChevronLeft className="w-5 h-5" />
            </Link>
            <div>
              <h1 className="text-xl font-bold text-white leading-tight">{movie.title}</h1>
              <p className="text-sm text-primary font-medium">{showtime}</p>
            </div>
          </div>
          <div className="text-right">
            <p className="text-sm text-muted">Total Amount</p>
            <p className="text-2xl font-bold text-white">${calculateTotal().toFixed(2)}</p>
          </div>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          
          {/* Ticket Selection */}
          <div className="glass p-6 rounded-2xl h-fit">
            <h2 className="text-lg font-bold mb-4 text-white">Select Tickets</h2>
            <div className="space-y-4">
              {TICKET_CATEGORIES.map(category => (
                <div key={category.type} className="flex items-center justify-between">
                  <div>
                    <p className="font-medium text-white">{category.type}</p>
                    <p className="text-sm text-muted">${category.price.toFixed(2)}</p>
                  </div>
                  <div className="flex items-center gap-3">
                    <button 
                      onClick={() => updateTicketCount(category.type, -1)}
                      className="w-8 h-8 rounded-full border border-white/10 flex items-center justify-center text-white hover:border-primary transition-colors"
                      disabled={tickets[category.type] === 0}
                    >-</button>
                    <span className="w-4 text-center text-white">{tickets[category.type]}</span>
                    <button 
                      onClick={() => updateTicketCount(category.type, 1)}
                      className="w-8 h-8 rounded-full border border-white/10 flex items-center justify-center text-white hover:border-primary transition-colors"
                    >+</button>
                  </div>
                </div>
              ))}
            </div>

            {totalTickets > 0 && (
              <div className="mt-6 pt-4 border-t border-white/10">
                <p className="text-sm text-muted">Seats selected: <span className="text-white font-medium">{selectedSeatsCount} / {totalTickets}</span></p>
              </div>
            )}
          </div>

          {/* Seating Layout */}
          <div className="md:col-span-2 glass p-6 rounded-2xl overflow-x-auto">
            <h2 className="text-lg font-bold mb-6 text-white text-center flex items-center justify-center gap-2">
              <Monitor className="w-5 h-5 text-gray-500" /> Screen This Way
            </h2>
            
            <div className="w-full bg-gradient-to-b from-white/20 to-transparent h-1 mb-12 rounded-full mx-auto max-w-sm shadow-[0_0_20px_rgba(255,255,255,0.1)]"></div>

            <div className="min-w-[400px]">
              <div className="grid gap-3 justify-center mb-8">
                {['A', 'B', 'C', 'D', 'E', 'F', 'G'].map(row => (
                  <div key={row} className="flex gap-2 items-center justify-center">
                    <span className="w-6 text-center text-xs font-bold text-muted">{row}</span>
                    <div className="flex gap-2">
                      {seats.filter(s => s.row === row).map(seat => (
                        <button
                          key={seat.id}
                          onClick={() => toggleSeat(seat.id)}
                          disabled={!seat.isAvailable}
                          className={`w-6 h-6 sm:w-8 sm:h-8 rounded-t-lg rounded-b-sm text-xs flex items-center justify-center transition-colors ${
                            !seat.isAvailable ? 'bg-gray-800 cursor-not-allowed' :
                            seat.isSelected ? 'bg-primary text-white shadow-[0_0_10px_rgba(229,9,20,0.5)]' :
                            'bg-white/10 hover:bg-white/10 text-transparent'
                          }`}
                        >
                          {seat.isSelected && <span className="block w-2 h-2 bg-white rounded-full"></span>}
                        </button>
                      ))}
                    </div>
                  </div>
                ))}
              </div>

              <div className="flex justify-center gap-6 text-sm text-muted border-t border-white/10 pt-6">
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 bg-white/10 rounded"></div> Available
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 bg-primary rounded"></div> Selected
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 bg-gray-800 rounded"></div> Taken
                </div>
              </div>
            </div>

            <div className="mt-8 flex justify-end">
              <button 
                disabled={totalTickets === 0 || selectedSeatsCount !== totalTickets}
                className="bg-primary hover:bg-red-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-bold py-3 px-8 rounded-lg transition-colors flex items-center gap-2"
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
