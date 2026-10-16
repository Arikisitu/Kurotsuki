import React, { useRef } from 'react';
import { Link } from 'react-router-dom';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Anime, Manga } from '@sahasra/shared-types';
import { AnimeCard } from './AnimeCard';
import { MangaCard } from './MangaCard';

interface MediaCarouselProps {
  title: string;
  subtitle?: string;
  items: (Anime | Manga)[];
  type?: 'ANIME' | 'MANGA';
  viewAllLink?: string;
  badge?: string;
}

export const MediaCarousel: React.FC<MediaCarouselProps> = ({
  title,
  subtitle,
  items,
  type = 'ANIME',
  viewAllLink,
  badge,
}) => {
  const scrollContainerRef = useRef<HTMLDivElement>(null);

  const scroll = (direction: 'left' | 'right') => {
    if (scrollContainerRef.current) {
      const container = scrollContainerRef.current;
      const scrollAmount = container.clientWidth * 0.75;
      container.scrollBy({
        left: direction === 'left' ? -scrollAmount : scrollAmount,
        behavior: 'smooth',
      });
    }
  };

  if (!items || items.length === 0) return null;

  return (
    <section className="relative space-y-3.5 my-6">
      {/* Header with Title and Controls */}
      <div className="flex items-end justify-between px-1">
        <div className="space-y-0.5">
          <div className="flex items-center gap-2.5">
            <h2 className="text-xl md:text-2xl font-bold tracking-tight text-[#F5F5F7]">
              {title}
            </h2>
            {badge && (
              <span className="rounded-full bg-[#8585F7]/10 border border-[#8585F7]/30 px-2 py-0.5 text-[11px] font-semibold text-[#8585F7]">
                {badge}
              </span>
            )}
          </div>
          {subtitle && <p className="text-xs md:text-sm text-[#A0A3AE]">{subtitle}</p>}
        </div>

        <div className="flex items-center gap-2">
          {viewAllLink && (
            <Link
              to={viewAllLink}
              className="text-xs md:text-sm font-semibold text-[#8585F7] hover:text-[#AAA7FF] transition-colors pr-2"
            >
              View all
            </Link>
          )}

          {/* Navigation Arrows */}
          <div className="hidden sm:flex items-center gap-1.5">
            <button
              onClick={() => scroll('left')}
              className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#191C22] text-[#A0A3AE] hover:text-[#F5F5F7] hover:bg-[#292C35] border border-[#292C35] transition-colors"
              aria-label="Scroll left"
            >
              <ChevronLeft size={16} />
            </button>
            <button
              onClick={() => scroll('right')}
              className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#191C22] text-[#A0A3AE] hover:text-[#F5F5F7] hover:bg-[#292C35] border border-[#292C35] transition-colors"
              aria-label="Scroll right"
            >
              <ChevronRight size={16} />
            </button>
          </div>
        </div>
      </div>

      {/* Carousel Track */}
      <div
        ref={scrollContainerRef}
        className="flex gap-4 overflow-x-auto pb-3 pt-1 no-scrollbar scroll-smooth snap-x snap-mandatory"
        style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
      >
        {items.map((item, index) => (
          <div
            key={item.id}
            className="w-[140px] sm:w-[170px] md:w-[190px] lg:w-[210px] shrink-0 snap-start"
          >
            {type === 'MANGA' || item.type === 'MANGA' ? (
              <MangaCard manga={item as Manga} priority={index < 4} />
            ) : (
              <AnimeCard anime={item as Anime} priority={index < 4} />
            )}
          </div>
        ))}
      </div>
    </section>
  );
};
