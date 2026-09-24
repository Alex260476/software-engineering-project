import os

# Update index.css
index_css = '''@tailwind base;
@tailwind components;
@tailwind utilities;

@layer utilities {
  .glass {
    @apply bg-surface/80 backdrop-blur-md border border-white/10;
  }
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
}
'''
with open('src/index.css', 'w') as f: f.write(index_css)

# Update tailwind.config.js
tailwind_config = '''/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: '#0a0a0a',
        surface: '#1a1a1a',
        primary: '#3b82f6',
        text: '#ffffff',
        muted: '#a3a3a3'
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
'''
with open('tailwind.config.js', 'w') as f: f.write(tailwind_config)

# Update App.tsx
app_tsx = '''import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
import MovieDetails from './pages/MovieDetails';
import Booking from './pages/Booking';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/movie/:id" element={<MovieDetails />} />
          <Route path="/booking/:movieId/:showtime" element={<Booking />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;
'''
with open('src/App.tsx', 'w') as f: f.write(app_tsx)

# Update Layout.tsx
layout_tsx = '''import React from 'react';
import Header from './Header';

interface LayoutProps {
  children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  return (
    <div className="min-h-screen flex flex-col bg-background text-text">
      <Header />
      <main className="flex-grow">
        {children}
      </main>
      <footer className="bg-surface border-t border-white/10 mt-auto py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-muted text-sm">
          <p>&copy; {new Date().getFullYear()} CinemaFlex. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default Layout;
'''
with open('src/components/Layout.tsx', 'w') as f: f.write(layout_tsx)

# Update Header.tsx
header_tsx = '''import { Film, Menu, Search, User } from 'lucide-react';
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
'''
with open('src/components/Header.tsx', 'w') as f: f.write(header_tsx)

# Revert specific utility classes in Home, MovieDetails, Booking
files_to_patch = ['src/pages/Home.tsx', 'src/pages/MovieDetails.tsx', 'src/pages/Booking.tsx']
for p in files_to_patch:
    with open(p, 'r') as f:
        content = f.read()
    
    # Revert replacements
    content = content.replace('border-border', 'border-white/10')
    content = content.replace('bg-border', 'bg-white/10')
    content = content.replace('bg-white/5 hover:bg-surface', 'bg-white/5 hover:bg-white/10')
    content = content.replace('hover:bg-border', 'hover:bg-white/20')
    content = content.replace('text-text', 'text-white')
    
    # Let's do some specific targeted fixups if needed, like the search input
    content = content.replace('bg-surface hover:bg-white/10 text-gray-300', 'bg-white/5 hover:bg-white/10 text-gray-300')

    with open(p, 'w') as f:
        f.write(content)
