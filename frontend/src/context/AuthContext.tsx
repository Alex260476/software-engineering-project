import React, { createContext, useContext, useEffect, useState } from 'react';
import {
  AuthResponse,
  LoginData,
  RegistrationData,
  User,
} from '../types';
import {
  addFavoriteMovie,
  fetchProfile,
  loginUser,
  logoutUser,
  registerUser,
  removeFavoriteMovie,
} from '../services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  favorites: string[];
  login: (data: LoginData) => Promise<User>;
  register: (data: RegistrationData) => Promise<AuthResponse>;
  logout: () => void;
  refreshUser: () => Promise<User | null>;
  toggleFavorite: (movieId: string) => Promise<boolean>;
  isFavorite: (movieId: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    try {
      const stored = localStorage.getItem('cinema_user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('cinema_token');
  });

  const [favorites, setFavorites] = useState<string[]>(() => {
    return user?.favoriteMovieIds || [];
  });

  useEffect(() => {
    if (user?.favoriteMovieIds) {
      setFavorites(user.favoriteMovieIds);
    }
  }, [user]);

  // Load latest profile on mount if token or user exists
  useEffect(() => {
    if (token || user?.id) {
      refreshUser().catch(() => {
        // If token expired/invalid, clear session
        logout();
      });
    }
  }, []);

  const refreshUser = async (): Promise<User | null> => {
    try {
      const updated = await fetchProfile(user?.id);
      setUser(updated);
      setFavorites(updated.favoriteMovieIds || []);
      localStorage.setItem('cinema_user', JSON.stringify(updated));
      return updated;
    } catch (err) {
      return null;
    }
  };

  const login = async (data: LoginData): Promise<User> => {
    const res = await loginUser(data);
    if (res.token) {
      setToken(res.token);
      localStorage.setItem('cinema_token', res.token);
    }
    if (res.user) {
      setUser(res.user);
      setFavorites(res.user.favoriteMovieIds || []);
      localStorage.setItem('cinema_user', JSON.stringify(res.user));
    }
    return res.user as User;
  };

  const register = async (data: RegistrationData): Promise<AuthResponse> => {
    return await registerUser(data);
  };

  const logout = () => {
    logoutUser().catch(() => {});
    setUser(null);
    setToken(null);
    setFavorites([]);
    localStorage.removeItem('cinema_token');
    localStorage.removeItem('cinema_user');
  };

  const toggleFavorite = async (movieId: string): Promise<boolean> => {
    if (!user) {
      throw new Error('Please log in to add movies to your favorites.');
    }

    const currentlyFav = favorites.includes(movieId);
    try {
      if (currentlyFav) {
        setFavorites(prev => prev.filter(id => id !== movieId));
        const updated = await removeFavoriteMovie(movieId, user.id);
        setUser(updated);
        localStorage.setItem('cinema_user', JSON.stringify(updated));
        return false;
      } else {
        setFavorites(prev => [...prev, movieId]);
        const updated = await addFavoriteMovie(movieId, user.id);
        setUser(updated);
        localStorage.setItem('cinema_user', JSON.stringify(updated));
        return true;
      }
    } catch (err) {
      // Revert on error
      if (currentlyFav) {
        setFavorites(prev => [...prev, movieId]);
      } else {
        setFavorites(prev => prev.filter(id => id !== movieId));
      }
      throw err;
    }
  };

  const isFavorite = (movieId: string): boolean => {
    return favorites.includes(movieId);
  };

  const isAuthenticated = !!user;
  const isAdmin = user?.userType === 'ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        isAdmin,
        favorites,
        login,
        register,
        logout,
        refreshUser,
        toggleFavorite,
        isFavorite,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
