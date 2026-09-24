import { Film, Menu, Search, User } from 'lucide-react';
import { Link } from 'react-router-dom';

const Header = () => {
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
            <a href="/#now-playing" className="text-sm font-medium text-white hover:text-primary transition-colors">Now Playing</a>
            <a href="/#coming-soon" className="text-sm font-medium text-white hover:text-primary transition-colors">Coming Soon</a>
            <Link to="/" className="text-sm font-medium text-white hover:text-primary transition-colors">Cinemas</Link>
          </nav>

          <div className="flex items-center space-x-4">
            <button className="p-2 text-white hover:text-primary transition-colors">
              <Search className="h-5 w-5" />
            </button>
            <button className="p-2 text-white hover:text-primary transition-colors hidden md:block">
              <User className="h-5 w-5" />
            </button>
            <button className="p-2 text-white hover:text-primary transition-colors md:hidden">
              <Menu className="h-5 w-5" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
