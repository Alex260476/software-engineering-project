import os

# Update index.css
index_css = '''@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --color-background: #ffffff;
    --color-surface: #f3f4f6;
    --color-primary: #3b82f6;
    --color-text: #111827;
    --color-muted: #6b7280;
    --color-border: rgba(0, 0, 0, 0.1);
  }

  .dark {
    --color-background: #0a0a0a;
    --color-surface: #1a1a1a;
    --color-primary: #3b82f6;
    --color-text: #ffffff;
    --color-muted: #a3a3a3;
    --color-border: rgba(255, 255, 255, 0.1);
  }
}

@layer utilities {
  .glass {
    @apply bg-surface/80 backdrop-blur-md border border-[var(--color-border)];
  }
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  @apply bg-background text-text transition-colors duration-300;
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
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        background: 'var(--color-background)',
        surface: 'var(--color-surface)',
        primary: 'var(--color-primary)',
        text: 'var(--color-text)',
        muted: 'var(--color-muted)',
        border: 'var(--color-border)'
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
import { useState, useEffect } from 'react';

function App() {
  const [theme, setTheme] = useState<'light'|'dark'>(() => {
    return (localStorage.getItem('theme') as 'light'|'dark') || 'dark';
  });

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => setTheme(prev => prev === 'light' ? 'dark' : 'light');

  return (
    <Router>
      <Layout theme={theme} toggleTheme={toggleTheme}>
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
  theme: 'light'|'dark';
  toggleTheme: () => void;
}

const Layout: React.FC<LayoutProps> = ({ children, theme, toggleTheme }) => {
  return (
    <div className="min-h-screen flex flex-col bg-background text-text transition-colors duration-300">
      <Header theme={theme} toggleTheme={toggleTheme} />
      <main className="flex-grow">
        {children}
      </main>
      <footer className="bg-surface border-t border-border mt-auto py-8 transition-colors duration-300">
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
header_tsx = '''import { Film, Menu, Search, User, Sun, Moon } from 'lucide-react';
import { Link } from 'react-router-dom';

const Header = ({ theme, toggleTheme }: { theme?: 'light'|'dark', toggleTheme?: () => void }) => {
  return (
    <header className="sticky top-0 z-50 glass border-b border-border transition-colors duration-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link to="/" className="flex items-center gap-2 hover:opacity-80 transition-opacity">
              <Film className="h-8 w-8 text-primary" />
              <span className="font-bold text-xl tracking-tight text-text">Cinema<span className="text-primary">Flex</span></span>
            </Link>
          </div>
          
          <nav className="hidden md:flex space-x-8">
            <a href="/#now-playing" className="text-sm font-medium text-text hover:text-primary transition-colors">Now Playing</a>
            <a href="/#coming-soon" className="text-sm font-medium text-text hover:text-primary transition-colors">Coming Soon</a>
            <Link to="/" className="text-sm font-medium text-text hover:text-primary transition-colors">Cinemas</Link>
          </nav>

          <div className="flex items-center space-x-4">
            <button onClick={toggleTheme} className="p-2 text-text hover:text-primary transition-colors">
              {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </button>
            <button className="p-2 text-text hover:text-primary transition-colors">
              <Search className="h-5 w-5" />
            </button>
            <button className="p-2 text-text hover:text-primary transition-colors hidden md:block">
              <User className="h-5 w-5" />
            </button>
            <button className="p-2 text-text hover:text-primary transition-colors md:hidden">
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

# Replace specific utility classes in Home, MovieDetails, Booking
files_to_patch = ['src/pages/Home.tsx', 'src/pages/MovieDetails.tsx', 'src/pages/Booking.tsx']
for p in files_to_patch:
    with open(p, 'r') as f:
        content = f.read()
    
    # Simple replacements
    content = content.replace('border-white/10', 'border-border')
    content = content.replace('border-white/20', 'border-border')
    content = content.replace('bg-white/10', 'bg-border')
    content = content.replace('bg-white/5', 'bg-surface')
    content = content.replace('hover:bg-white/10', 'hover:bg-surface')
    content = content.replace('hover:bg-white/20', 'hover:bg-border')
    content = content.replace('text-white', 'text-text')
    
    # Fix back things that should absolutely be text-white
    content = content.replace('bg-primary text-text', 'bg-primary text-white')
    content = content.replace('<h1 className="text-4xl md:text-6xl font-bold text-text mb-4 tracking-tight">Experience the <span className="text-primary">Magic</span> of Cinema</h1>', '<h1 className="text-4xl md:text-6xl font-bold text-white mb-4 tracking-tight">Experience the <span className="text-primary">Magic</span> of Cinema</h1>')
    content = content.replace('<p className="text-lg md:text-xl text-gray-300 mb-8">', '<p className="text-lg md:text-xl text-white/80 mb-8">')
    
    with open(p, 'w') as f:
        f.write(content)
