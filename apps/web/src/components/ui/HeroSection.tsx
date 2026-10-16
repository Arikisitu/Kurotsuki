import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Anime } from '@sahasra/shared-types';
import { ScoreBadge } from './ScoreBadge';
import { Play, Info, Bookmark, Check, ChevronLeft, ChevronRight } from 'lucide-react';
import { useLibrary } from '../../hooks/useLibrary';

interface HeroSectionProps {
  items: Anime[];
}

export const HeroSection: React.FC<HeroSectionProps> = ({ items }) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const { getEntry, setStatus } = useLibrary();

  if (!items || items.length === 0) return null;

  // Use top 5 items for hero rotation
  const featured = items.slice(0, 5);
  const current = featured[currentIndex] || featured[0];

  const libraryEntry = getEntry(current.id);
  const displayTitle =
    current.title.userPreferred || current.title.english || current.title.romaji || 'Untitled';
  const bannerUrl = current.artwork.banner || current.artwork.cover.extraLarge || current.artwork.cover.large;
  const studio = current.studios?.[0]?.name;

  const nextSlide = () => {
    setCurrentIndex((prev) => (prev + 1) % featured.length);
  };

  const prevSlide = () => {
    setCurrentIndex((prev) => (prev - 1 + featured.length) % featured.length);
  };

  const handleLibraryToggle = () => {
    if (libraryEntry) {
      setStatus(current, libraryEntry.status === 'WATCHING' ? 'COMPLETED' : 'WATCHING');
    } else {
      setStatus(current, 'PLANNING');
    }
  };

  return (
    <div className="relative w-full overflow-hidden rounded-2xl md:rounded-3xl border border-[#292C35] bg-[#111318] shadow-2xl">
      {/* Background Banner with Layered Gradients */}
      <div className="relative h-[440px] sm:h-[500px] md:h-[560px] w-full">
        {bannerUrl ? (
          <img
            src={bannerUrl}
            alt={displayTitle}
            className="h-full w-full object-cover object-top filter brightness-[0.7] contrast-[1.05] transition-all duration-700 ease-out"
          />
        ) : (
          <div className="h-full w-full bg-[#111318]" />
        )}

        {/* Multi-directional Gradients for AMOLED integration */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#08090B] via-[#08090B]/60 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-r from-[#08090B] via-[#08090B]/75 to-transparent" />
        <div className="absolute inset-0 bg-radial-at-c from-transparent via-[#08090B]/30 to-[#08090B]/90" />

        {/* Content Container */}
        <div className="absolute inset-0 flex flex-col justify-end p-6 sm:p-8 md:p-12 lg:p-14">
          <div className="max-w-3xl space-y-3.5">
            {/* Meta Row */}
            <div className="flex flex-wrap items-center gap-2 text-xs font-semibold">
              <span className="rounded-full bg-[#E64B55] px-2.5 py-0.5 text-white tracking-wider uppercase text-[10px]">
                Featured #0{currentIndex + 1}
              </span>
              {current.format && (
                <span className="rounded-full bg-[#191C22]/90 border border-[#292C35] px-2.5 py-0.5 text-[#AAA7FF]">
                  {current.format}
                </span>
              )}
              {current.averageScore && (
                <ScoreBadge score={current.averageScore} size="md" />
              )}
              {current.seasonYear && (
                <span className="text-[#A0A3AE]">
                  {current.season ? `${current.season} ` : ''}{current.seasonYear}
                </span>
              )}
              {studio && (
                <>
                  <span className="text-[#292C35]">•</span>
                  <span className="text-[#A0A3AE]">{studio}</span>
                </>
              )}
            </div>

            {/* Title */}
            <h1 className="text-2xl sm:text-4xl md:text-5xl font-extrabold tracking-tight text-[#F5F5F7] line-clamp-2 leading-tight">
              {displayTitle}
            </h1>

            {/* Synopsis */}
            {current.description && (
              <p className="line-clamp-2 md:line-clamp-3 text-xs sm:text-sm text-[#A0A3AE] max-w-2xl leading-relaxed">
                {current.description}
              </p>
            )}

            {/* Genres */}
            <div className="hidden sm:flex flex-wrap gap-1.5 pt-1">
              {current.genres.slice(0, 4).map((genre) => (
                <span
                  key={genre}
                  className="rounded-md bg-[#191C22]/80 border border-[#292C35]/80 px-2 py-0.5 text-xs text-[#A0A3AE]"
                >
                  {genre}
                </span>
              ))}
            </div>

            {/* Action Buttons */}
            <div className="flex flex-wrap items-center gap-3 pt-2">
              <Link
                to={`/anime/${current.id}`}
                className="flex items-center gap-2 rounded-xl bg-[#8585F7] hover:bg-[#7272E5] text-white px-5 py-3 text-sm font-semibold shadow-lg shadow-[#8585F7]/25 transition-all duration-200 active:scale-95"
              >
                <Play size={16} className="fill-current" />
                <span>Explore Title</span>
              </Link>

              <button
                onClick={handleLibraryToggle}
                className={`flex items-center gap-2 rounded-xl border px-4 py-3 text-sm font-semibold transition-all duration-200 active:scale-95 ${
                  libraryEntry
                    ? 'bg-[#4ADE80]/10 border-[#4ADE80] text-[#4ADE80]'
                    : 'bg-[#191C22]/90 border-[#292C35] text-[#F5F5F7] hover:border-[#8585F7]'
                }`}
              >
                {libraryEntry ? (
                  <>
                    <Check size={16} />
                    <span>In Library ({libraryEntry.status})</span>
                  </>
                ) : (
                  <>
                    <Bookmark size={16} />
                    <span>Track to Watch</span>
                  </>
                )}
              </button>

              <Link
                to={`/anime/${current.id}`}
                className="hidden sm:flex items-center gap-1.5 rounded-xl bg-[#191C22]/80 hover:bg-[#292C35] border border-[#292C35] text-[#A0A3AE] hover:text-[#F5F5F7] px-3.5 py-3 text-sm font-medium transition-colors"
                title="Full Overview"
              >
                <Info size={16} />
              </Link>
            </div>
          </div>

          {/* Carousel Dot Indicators & Navigation Controls */}
          <div className="absolute bottom-6 right-6 sm:bottom-8 sm:right-8 flex items-center gap-3">
            <div className="flex items-center gap-1.5">
              {featured.map((_, idx) => (
                <button
                  key={idx}
                  onClick={() => setCurrentIndex(idx)}
                  className={`h-1.5 rounded-full transition-all duration-300 ${
                    idx === currentIndex
                      ? 'w-6 bg-[#8585F7]'
                      : 'w-2 bg-[#292C35] hover:bg-[#A0A3AE]'
                  }`}
                  aria-label={`Go to slide ${idx + 1}`}
                />
              ))}
            </div>

            <div className="flex items-center gap-1">
              <button
                onClick={prevSlide}
                className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#191C22]/80 hover:bg-[#292C35] border border-[#292C35] text-[#A0A3AE] hover:text-white transition-colors"
                aria-label="Previous featured title"
              >
                <ChevronLeft size={16} />
              </button>
              <button
                onClick={nextSlide}
                className="flex h-8 w-8 items-center justify-center rounded-lg bg-[#191C22]/80 hover:bg-[#292C35] border border-[#292C35] text-[#A0A3AE] hover:text-white transition-colors"
                aria-label="Next featured title"
              >
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
