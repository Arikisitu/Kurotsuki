import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { SearchBar } from '../ui/SearchBar';
import { getCurrentSeason } from '../../hooks/useAnime';
import { Bookmark, Bell, Tv, BookOpen } from 'lucide-react';
import { useLibrary } from '../../hooks/useLibrary';

export const Header: React.FC = () => {
  const { season, year } = getCurrentSeason();
  const { listEntries } = useLibrary();
  const location = useLocation();

  const isMangaMode =
    location.pathname.startsWith('/manga') ||
    location.pathname.startsWith('/read') ||
    (location.pathname === '/explore' && location.search.includes('type=manga'));

  return (
    <header className="sticky top-0 z-30 flex h-16 w-full items-center justify-between gap-4 border-b border-[#292C35]/80 bg-[#08090B]/80 px-4 md:px-8 backdrop-blur-xl">
      {/* Mobile Brand Link */}
      <Link to="/" className="flex items-center gap-2 md:hidden shrink-0">
        <img
          src="/logo.jpg"
          alt="SAHASRA"
          className="h-8 w-8 rounded-lg object-cover border border-[#8585F7]/30"
        />
        <span className="font-extrabold text-sm tracking-wider text-[#F5F5F7] font-display">
          SAHASRA
        </span>
      </Link>

      {/* Center Segmented Switcher (Anime | Manga) & Search Bar */}
      <div className="flex items-center gap-3 md:gap-5 flex-1 max-w-2xl mx-auto">
        <div className="flex items-center gap-1 p-1 rounded-2xl bg-[#111318] border border-[#292C35] shrink-0 shadow-inner">
          <Link
            to="/"
            className={`flex items-center gap-1.5 px-3 sm:px-4 py-1.5 rounded-xl text-xs font-bold transition-all ${
              !isMangaMode
                ? 'bg-[#8585F7] text-black shadow'
                : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
            }`}
          >
            <Tv size={13} />
            <span>Anime</span>
          </Link>
          <Link
            to="/manga"
            className={`flex items-center gap-1.5 px-3 sm:px-4 py-1.5 rounded-xl text-xs font-bold transition-all ${
              isMangaMode
                ? 'bg-[#8585F7] text-black shadow'
                : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
            }`}
          >
            <BookOpen size={13} />
            <span>Manga</span>
          </Link>
        </div>

        <div className="flex-1 min-w-0">
          <SearchBar />
        </div>
      </div>

      {/* Right Controls */}
      <div className="flex items-center gap-3 shrink-0">
        {/* Season indicator pill */}
        <div className="hidden lg:flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#111318] border border-[#292C35] text-xs font-medium text-[#A0A3AE]">
          <span className="h-1.5 w-1.5 rounded-full bg-[#4ADE80] animate-pulse" />
          <span>{season} {year}</span>
        </div>

        {/* Quick Library Button */}
        <Link
          to="/library"
          className="relative flex h-9 w-9 items-center justify-center rounded-xl bg-[#111318] text-[#A0A3AE] hover:text-[#F5F5F7] hover:bg-[#191C22] border border-[#292C35] transition-colors"
          title="My Library"
        >
          <Bookmark size={16} />
          {listEntries.length > 0 && (
            <span className="absolute -top-1 -right-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-[#8585F7] px-1 text-[10px] font-bold text-white">
              {listEntries.length}
            </span>
          )}
        </Link>

        {/* User Settings */}
        <Link
          to="/settings"
          className="h-9 w-9 rounded-xl overflow-hidden border border-[#292C35] hover:border-[#8585F7] transition-colors bg-[#191C22] flex items-center justify-center"
          title="Account Settings"
        >
          <span className="text-xs font-bold text-[#8585F7]">S</span>
        </Link>
      </div>
    </header>
  );
};

export default Header;
