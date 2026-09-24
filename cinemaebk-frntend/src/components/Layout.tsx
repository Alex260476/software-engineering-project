import React from 'react';
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
