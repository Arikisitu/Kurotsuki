import React from 'react';
import { NavLink } from 'react-router-dom';
import { Home, Compass, BookOpen, Calendar, Bookmark } from 'lucide-react';

export const MobileNav: React.FC = () => {
  const navItems = [
    { name: 'Anime', path: '/', icon: Home },
    { name: 'Manga', path: '/manga', icon: BookOpen },
    { name: 'Explore', path: '/explore', icon: Compass },
    { name: 'Schedule', path: '/schedule', icon: Calendar },
    { name: 'Library', path: '/library', icon: Bookmark },
  ];

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-[#08090B]/95 backdrop-blur-xl border-t border-[#292C35] px-2 py-1 safe-area-pb">
      <div className="flex items-center justify-around h-14">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.name}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) =>
                `flex flex-col items-center justify-center w-full h-full gap-1 transition-colors ${
                  isActive ? 'text-[#8585F7]' : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
                }`
              }
            >
              <Icon size={18} />
              <span className="text-[10px] font-medium tracking-tight">
                {item.name}
              </span>
            </NavLink>
          );
        })}
      </div>
    </nav>
  );
};
