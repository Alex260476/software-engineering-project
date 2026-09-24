import { Movie } from '../types';

export const mockMovies: Movie[] = [
  {
    id: '1',
    title: 'Dune: Part Two',
    description: 'Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family.',
    rating: 'PG-13',
    posterUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/Way9Dexny3w',
    genre: ['Sci-Fi', 'Adventure', 'Action'],
    status: 'Currently Running',
    releaseDate: '2024-03-01',
    duration: '2h 46m',
  },
  {
    id: '2',
    title: 'Furiosa: A Mad Max Saga',
    description: 'The origin story of renegade warrior Furiosa before her encounter and teamup with Mad Max.',
    rating: 'R',
    posterUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/XJMuhwVlca4',
    genre: ['Action', 'Sci-Fi', 'Adventure'],
    status: 'Coming Soon',
    releaseDate: '2024-05-24',
    duration: '2h 28m',
  },
  {
    id: '3',
    title: 'Kingdom of the Planet of the Apes',
    description: 'Many years after the reign of Caesar, a young ape goes on a journey that will lead him to question everything he\'s been taught about the past.',
    rating: 'PG-13',
    posterUrl: 'https://images.unsplash.com/photo-1542204165-65bf26472b9b?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/KUr0y20E4uE',
    genre: ['Sci-Fi', 'Action'],
    status: 'Currently Running',
    releaseDate: '2024-05-10',
    duration: '2h 25m',
  },
  {
    id: '4',
    title: 'Deadpool & Wolverine',
    description: 'Wolverine is recovering from his injuries when he crosses paths with the loudmouth, Deadpool. They team up to defeat a common enemy.',
    rating: 'R',
    posterUrl: 'https://images.unsplash.com/photo-1612036782180-6f0b6cd846fe?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/73_1biulkYk',
    genre: ['Action', 'Comedy'],
    status: 'Coming Soon',
    releaseDate: '2024-07-26',
    duration: '2h 7m',
  },
  {
    id: '5',
    title: 'Civil War',
    description: 'A journey across a dystopian future America, following a team of military-embedded journalists as they race against time to reach DC before rebel factions descend upon the White House.',
    rating: 'R',
    posterUrl: 'https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/aDyQZIGlsXG',
    genre: ['Action', 'Thriller'],
    status: 'Currently Running',
    releaseDate: '2024-04-12',
    duration: '1h 49m',
  },
  {
    id: '6',
    title: 'Inside Out 2',
    description: 'Follows Riley, in her teenage years, encountering new emotions.',
    rating: 'PG',
    posterUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/LEjhY15eCx0',
    genre: ['Animation', 'Comedy', 'Family'],
    status: 'Coming Soon',
    releaseDate: '2024-06-14',
    duration: '1h 36m',
  },
  {
    id: '7',
    title: 'A Quiet Place: Day One',
    description: 'Experience the day the world went quiet.',
    rating: 'PG-13',
    posterUrl: 'https://images.unsplash.com/photo-1440407876336-62333a6f010c?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/YPY7J-flzE8',
    genre: ['Horror', 'Sci-Fi'],
    status: 'Coming Soon',
    releaseDate: '2024-06-28',
    duration: '1h 40m',
  },
  {
    id: '8',
    title: 'Challengers',
    description: 'Tashi, a tennis player-turned-coach, has taken her husband, Art, and transformed him from a mediocre player into a world-famous grand slam champion.',
    rating: 'R',
    posterUrl: 'https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/VObntwt-G20',
    genre: ['Drama', 'Romance'],
    status: 'Currently Running',
    releaseDate: '2024-04-26',
    duration: '2h 11m',
  },
  {
    id: '9',
    title: 'The Fall Guy',
    description: 'A down-and-out stuntman must find the missing star of his ex-girlfriend\'s blockbuster film.',
    rating: 'PG-13',
    posterUrl: 'https://images.unsplash.com/photo-1533038590840-1cde6e668a8f?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/j7jPnwVGdCQ',
    genre: ['Action', 'Comedy'],
    status: 'Currently Running',
    releaseDate: '2024-05-03',
    duration: '2h 6m',
  },
  {
    id: '10',
    title: 'Alien: Romulus',
    description: 'Young people from a distant world must face the most terrifying life form in the universe.',
    rating: 'R',
    posterUrl: 'https://images.unsplash.com/photo-1605806616949-1e87b487cb2a?auto=format&fit=crop&w=800&q=80',
    trailerUrl: 'https://www.youtube.com/embed/x0XDEhP4MQs',
    genre: ['Horror', 'Sci-Fi'],
    status: 'Coming Soon',
    releaseDate: '2024-08-16',
    duration: '1h 59m',
  }
];

export const fetchMovies = async (): Promise<Movie[]> => {
  // Simulate network delay
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(mockMovies);
    }, 800);
  });
};

export const fetchMovieById = async (id: string): Promise<Movie | undefined> => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(mockMovies.find(m => m.id === id));
    }, 500);
  });
};
