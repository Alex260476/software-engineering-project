import React, { useState } from 'react';
import {
  Film,
  Heart,
  LogOut,
  Menu,
  Search,
  Shield,
  Sparkles,
  User,
  X,
} from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LoginModal } from './auth/LoginModal';
import { RegisterModal } from './auth/RegisterModal';
import { ForgotPasswordModal } from './auth/ForgotPasswordModal';
import { DemoHelperModal } from './demo/DemoHelperModal';

const NAV_ITEMS = [
  { label: 'Now Playing', sectionId: 'now-playing' },
  { label: 'Coming Soon', sectionId: 'coming-soon' },
];

const Header: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isAuthenticated, isAdmin, logout } = useAuth();

  const [menuOpen, setMenuOpen] = useState(false);
  const [userDropdownOpen, setUserDropdownOpen] = useState(false);

  // Modals state
  const [loginOpen, setLoginOpen] = useState(false);
  const [registerOpen, setRegisterOpen] = useState(false);
  const [forgotPasswordOpen, setForgotPasswordOpen] = useState(false);
  const [demoHelperOpen, setDemoHelperOpen] = useState(false);

  // Quick fill state from demo companion
  const [loginPrefill, setLoginPrefill] = useState<{ email: string; pass: string }>({ email: '', pass: '' });
  const [registerPrefill, setRegisterPrefill] = useState<any>(null);

  const onHome = (action: () => void) => {
    setMenuOpen(false);
    if (location.pathname === '/') {
      action();
    } else {
      navigate('/');
      setTimeout(action, 400);
    }
  };

  const scrollTo = (sectionId: string) =>
    onHome(() => document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth' }));

  const focusSearch = () =>
    onHome(() => {
      const input = document.getElementById('movie-search');
      input?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      input?.focus({ preventScroll: true });
    });

  const handleOpenLoginWithData = (email: string, pass: string) => {
    setLoginPrefill({ email, pass });
    setLoginOpen(true);
  };

  const handleOpenRegisterWithData = (data: any) => {
    setRegisterPrefill(data);
    setRegisterOpen(true);
  };

  const navButtonClass = 'text-sm font-medium text-white hover:text-primary transition-colors';

  return (
    <>
      <header className="sticky top-0 z-40 glass border-b border-white/10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Brand Logo */}
            <div className="flex items-center gap-6">
              <Link to="/" className="flex items-center gap-2 hover:opacity-80 transition-opacity">
                <Film className="h-8 w-8 text-primary" />
                <span className="font-bold text-xl tracking-tight text-white">
                  Cinema<span className="text-primary">Flex</span>
                </span>
              </Link>

              <nav className="hidden md:flex space-x-6">
                {NAV_ITEMS.map(item => (
                  <button key={item.sectionId} onClick={() => scrollTo(item.sectionId)} className={navButtonClass}>
                    {item.label}
                  </button>
                ))}
                {isAdmin && (
                  <Link
                    to="/admin"
                    className="text-sm font-medium text-purple-400 hover:text-purple-300 flex items-center gap-1 transition-colors"
                  >
                    <Shield className="w-3.5 h-3.5" /> Admin Portal
                  </Link>
                )}
              </nav>
            </div>

            {/* Right Side Actions */}
            <div className="flex items-center space-x-3">
              {/* Demo Helper Button */}
              <button
                onClick={() => setDemoHelperOpen(true)}
                className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-gradient-to-r from-blue-600/30 to-purple-600/30 hover:from-blue-600/40 hover:to-purple-600/40 border border-primary/40 text-xs font-semibold text-white shadow-lg transition-all"
                title="Open Test Case Quick-Fill Helper"
              >
                <Sparkles className="w-3.5 h-3.5 text-primary" />
                <span>Demo Helper (TC1–TC10)</span>
              </button>

              <button
                onClick={focusSearch}
                aria-label="Search movies"
                className="p-2 text-white hover:text-primary transition-colors"
              >
                <Search className="h-5 w-5" />
              </button>

              {/* User Authentication Menu */}
              {isAuthenticated ? (
                <div className="relative">
                  <button
                    onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                    className="flex items-center gap-2 py-1.5 px-3 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-white text-xs font-medium transition-all"
                  >
                    <div className="w-6 h-6 rounded-full bg-primary/20 text-primary flex items-center justify-center font-bold text-[11px]">
                      {user?.firstName?.[0]?.toUpperCase() || 'U'}
                    </div>
                    <span className="hidden sm:inline-block max-w-[120px] truncate">
                      {user?.firstName}
                    </span>
                  </button>

                  {userDropdownOpen && (
                    <div
                      className="absolute right-0 mt-2 w-56 bg-surface border border-white/10 rounded-2xl p-2 shadow-2xl z-50 animate-fadeIn"
                      onClick={() => setUserDropdownOpen(false)}
                    >
                      <div className="px-3 py-2 border-b border-white/10 mb-1">
                        <p className="text-xs font-semibold text-white truncate">
                          {user?.firstName} {user?.lastName}
                        </p>
                        <p className="text-[11px] text-muted truncate">{user?.email}</p>
                        {isAdmin && (
                          <span className="inline-block mt-1 text-[10px] font-bold px-2 py-0.5 rounded bg-purple-500/20 text-purple-300 border border-purple-500/30">
                            ADMINISTRATOR
                          </span>
                        )}
                      </div>

                      <Link
                        to="/profile"
                        className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs text-gray-200 hover:bg-white/5 transition-colors"
                      >
                        <User className="w-4 h-4 text-muted" /> My Profile
                      </Link>

                      <Link
                        to="/profile"
                        onClick={() => {
                          // profile tab handles favorites
                        }}
                        className="flex items-center justify-between px-3 py-2 rounded-xl text-xs text-gray-200 hover:bg-white/5 transition-colors"
                      >
                        <span className="flex items-center gap-2.5">
                          <Heart className="w-4 h-4 text-pink-400" /> Favorite Movies
                        </span>
                        <span className="text-[10px] px-1.5 py-0.5 rounded bg-white/10 text-muted">
                          {user?.favoriteMovieIds?.length || 0}
                        </span>
                      </Link>

                      {isAdmin && (
                        <Link
                          to="/admin"
                          className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs text-purple-300 hover:bg-purple-500/10 transition-colors font-medium"
                        >
                          <Shield className="w-4 h-4 text-purple-400" /> Admin Portal
                        </Link>
                      )}

                      <div className="pt-1 mt-1 border-t border-white/10">
                        <button
                          onClick={logout}
                          className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl text-xs text-red-400 hover:bg-red-500/10 transition-colors text-left font-medium"
                        >
                          <LogOut className="w-4 h-4" /> Sign Out
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              ) : (
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => {
                      setLoginPrefill({ email: '', pass: '' });
                      setLoginOpen(true);
                    }}
                    className="px-3.5 py-1.5 text-xs font-semibold text-white hover:text-primary transition-colors"
                  >
                    Sign In
                  </button>
                  <button
                    onClick={() => {
                      setRegisterPrefill(null);
                      setRegisterOpen(true);
                    }}
                    className="px-3.5 py-1.5 rounded-xl bg-primary hover:bg-blue-600 text-white text-xs font-semibold transition-all shadow-lg shadow-primary/20"
                  >
                    Register
                  </button>
                </div>
              )}

              {/* Mobile menu toggle */}
              <button
                onClick={() => setMenuOpen(open => !open)}
                aria-label={menuOpen ? 'Close menu' : 'Open menu'}
                aria-expanded={menuOpen}
                className="p-2 text-white hover:text-primary transition-colors md:hidden"
              >
                {menuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
              </button>
            </div>
          </div>

          {/* Mobile nav */}
          {menuOpen && (
            <nav className="md:hidden flex flex-col gap-3 pb-4 pt-2 border-t border-white/10">
              {NAV_ITEMS.map(item => (
                <button
                  key={item.sectionId}
                  onClick={() => scrollTo(item.sectionId)}
                  className={`${navButtonClass} text-left`}
                >
                  {item.label}
                </button>
              ))}
              {isAdmin && (
                <Link to="/admin" className="text-sm font-medium text-purple-400 text-left">
                  Admin Portal
                </Link>
              )}
              <button
                onClick={() => {
                  setMenuOpen(false);
                  setDemoHelperOpen(true);
                }}
                className="text-left text-xs text-primary font-semibold flex items-center gap-1.5 pt-2"
              >
                <Sparkles className="w-4 h-4" /> Demo Helper (TC1–TC10)
              </button>
            </nav>
          )}
        </div>
      </header>

      {/* Floating Demo Launcher for quick access on any screen */}
      <div className="fixed bottom-6 right-6 z-40">
        <button
          onClick={() => setDemoHelperOpen(true)}
          className="flex items-center gap-2 px-4 py-3 rounded-full bg-primary hover:bg-blue-600 text-white shadow-2xl shadow-primary/40 font-bold text-xs border border-white/20 transition-all hover:scale-105 active:scale-95"
        >
          <Sparkles className="w-4 h-4 text-yellow-300" />
          <span>Demo Companion</span>
        </button>
      </div>

      {/* Modals */}
      <LoginModal
        isOpen={loginOpen}
        onClose={() => setLoginOpen(false)}
        onOpenRegister={() => {
          setLoginOpen(false);
          setRegisterOpen(true);
        }}
        onOpenForgotPassword={() => {
          setLoginOpen(false);
          setForgotPasswordOpen(true);
        }}
        defaultEmail={loginPrefill.email}
        defaultPassword={loginPrefill.pass}
      />

      <RegisterModal
        isOpen={registerOpen}
        onClose={() => setRegisterOpen(false)}
        onOpenLogin={() => {
          setRegisterOpen(false);
          setLoginOpen(true);
        }}
        initialData={registerPrefill}
      />

      <ForgotPasswordModal
        isOpen={forgotPasswordOpen}
        onClose={() => setForgotPasswordOpen(false)}
        onOpenLogin={() => {
          setForgotPasswordOpen(false);
          setLoginOpen(true);
        }}
      />

      <DemoHelperModal
        isOpen={demoHelperOpen}
        onClose={() => setDemoHelperOpen(false)}
        onFillRegister={handleOpenRegisterWithData}
        onFillLogin={handleOpenLoginWithData}
      />
    </>
  );
};

export default Header;
