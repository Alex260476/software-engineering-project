import { useState } from 'react';
import { Film, Menu, Search, X } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router-dom';

const NAV_ITEMS = [
  { label: 'Now Playing', sectionId: 'now-playing' },
  { label: 'Coming Soon', sectionId: 'coming-soon' },
];

const Header = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  // Go to the home page (if needed) and then act on an element there
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

  const navButtonClass = 'text-sm font-medium text-white hover:text-primary transition-colors';

  return (
    <header className="sticky top-0 z-50 glass border-b border-white/10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link to="/" className="flex items-center gap-2 hover:opacity-80 transition-opacity">
              <Film className="h-8 w-8 text-primary" />
              <span className="font-bold text-xl tracking-tight text-white">Cinema<span className="text-primary">Flex</span></span>
            </Link>
          </div>

          <nav className="hidden md:flex space-x-8">
            {NAV_ITEMS.map(item => (
              <button key={item.sectionId} onClick={() => scrollTo(item.sectionId)} className={navButtonClass}>
                {item.label}
              </button>
            ))}
          </nav>

          <div className="flex items-center space-x-2">
            <button onClick={focusSearch} aria-label="Search movies" className="p-2 text-white hover:text-primary transition-colors">
              <Search className="h-5 w-5" />
            </button>
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

        {menuOpen && (
          <nav className="md:hidden flex flex-col gap-4 pb-4">
            {NAV_ITEMS.map(item => (
              <button key={item.sectionId} onClick={() => scrollTo(item.sectionId)} className={`${navButtonClass} text-left`}>
                {item.label}
              </button>
            ))}
          </nav>
        )}
      </div>
    </header>
  );
};

export default Header;
