import React from 'react';
import { NavLink, Link } from 'react-router-dom';
import {
  Home,
  Compass,
  BookOpen,
  Calendar,
  Bookmark,
  Settings,
  Sparkles,
} from 'lucide-react';

interface SidebarProps {
  className?: string;
}

export const Sidebar: React.FC<SidebarProps> = ({ className = '' }) => {
  const navItems = [
    { name: 'Home', path: '/', icon: Home },
    { name: 'Explore Anime', path: '/explore', icon: Compass },
    { name: 'Manga & Novels', path: '/explore?type=manga', icon: BookOpen },
    { name: 'Schedule', path: '/schedule', icon: Calendar },
    { name: 'My Library', path: '/library', icon: Bookmark },
  ];

  return (
    <aside
      className={`hidden md:flex flex-col w-64 shrink-0 bg-[#08090B] border-r border-[#292C35] h-screen sticky top-0 z-40 select-none ${className}`}
    >
      {/* Brand Header */}
      <div className="h-16 flex items-center gap-3 px-6 border-b border-[#292C35]/60">
        <Link to="/" className="flex items-center gap-3 group">
          <div className="relative h-9 w-9 rounded-xl overflow-hidden border border-[#8585F7]/30 shadow-md shadow-[#8585F7]/10 group-hover:border-[#8585F7] transition-all">
            <img
              src="/logo.jpg"
              alt="SAHASRA"
              className="h-full w-full object-cover"
            />
          </div>
          <div className="flex flex-col">
            <span className="font-extrabold text-base tracking-wider text-[#F5F5F7] group-hover:text-[#8585F7] transition-colors font-display">
              SAHASRA
            </span>
            <span className="text-[10px] uppercase font-bold tracking-widest text-[#8585F7] -mt-1">
              Anime & Manga
            </span>
          </div>
        </Link>
      </div>

      {/* Main Navigation */}
      <div className="flex-1 overflow-y-auto px-4 py-6 space-y-6 no-scrollbar">
        <div>
          <div className="px-3 pb-2 text-[11px] font-semibold uppercase tracking-wider text-[#A0A3AE]/70">
            Discover
          </div>
          <nav className="space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.name}
                  to={item.path}
                  end={item.path === '/'}
                  className={({ isActive }) =>
                    `flex items-center gap-3.5 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 group relative ${
                      isActive
                        ? 'bg-[#191C22] text-[#F5F5F7] font-semibold shadow-inner'
                        : 'text-[#A0A3AE] hover:text-[#F5F5F7] hover:bg-[#111318]'
                    }`
                  }
                >
                  {({ isActive }) => (
                    <>
                      {isActive && (
                        <span className="absolute left-0 top-2 bottom-2 w-1 rounded-r-md bg-[#8585F7]" />
                      )}
                      <Icon
                        size={18}
                        className={`transition-colors ${
                          isActive
                            ? 'text-[#8585F7]'
                            : 'text-[#A0A3AE] group-hover:text-[#F5F5F7]'
                        }`}
                      />
                      <span>{item.name}</span>
                    </>
                  )}
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* Quick Collections Card */}
        <div className="p-4 rounded-2xl bg-gradient-to-br from-[#191C22] to-[#111318] border border-[#292C35] space-y-2">
          <div className="flex items-center gap-2 text-xs font-bold text-[#8585F7]">
            <Sparkles size={14} />
            <span>Seasonal Radar</span>
          </div>
          <p className="text-xs text-[#A0A3AE] leading-relaxed">
            Catch up on the highest trending releases of this current broadcast season.
          </p>
          <Link
            to="/explore?season=current"
            className="inline-block pt-1 text-xs font-semibold text-[#F5F5F7] hover:text-[#AAA7FF] underline underline-offset-4"
          >
            Browse Now →
          </Link>
        </div>
      </div>

      {/* Footer Navigation */}
      <div className="p-4 border-t border-[#292C35]/60 space-y-1">
        <NavLink
          to="/settings"
          className={({ isActive }) =>
            `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors ${
              isActive
                ? 'bg-[#191C22] text-[#F5F5F7]'
                : 'text-[#A0A3AE] hover:text-[#F5F5F7] hover:bg-[#111318]'
            }`
          }
        >
          <Settings size={18} />
          <span>Preferences</span>
        </NavLink>
      </div>
    </aside>
  );
};
