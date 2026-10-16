import React from 'react';
import { Link } from 'react-router-dom';
import { Manga } from '@sahasra/shared-types';
import { ScoreBadge } from './ScoreBadge';
import { BookOpen, Bookmark, Check } from 'lucide-react';
import { useLibrary } from '../../hooks/useLibrary';

interface MangaCardProps {
  manga: Manga;
  priority?: boolean;
}

export const MangaCard: React.FC<MangaCardProps> = ({ manga, priority = false }) => {
  const { getEntry, setStatus } = useLibrary();
  const libraryEntry = getEntry(manga.id);

  const displayTitle =
    manga.title.userPreferred || manga.title.english || manga.title.romaji || 'Untitled';
  const coverUrl =
    manga.artwork.cover.large || manga.artwork.cover.medium || '/logo.jpg';
  const authorName = manga.authors?.[0]?.name?.full || manga.staff?.[0]?.name?.full;

  const handleLibraryToggle = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (libraryEntry) {
      setStatus(manga, libraryEntry.status === 'WATCHING' ? 'COMPLETED' : 'WATCHING');
    } else {
      setStatus(manga, 'PLANNING');
    }
  };

  return (
    <div className="group relative flex flex-col w-full transition-transform duration-300 ease-out hover:-translate-y-1.5">
      <Link
        to={`/manga/${manga.id}`}
        className="relative block aspect-[2/3] w-full overflow-hidden rounded-xl bg-[#111318] border border-[#292C35] transition-all duration-300 group-hover:border-[#AAA7FF]/60 group-hover:shadow-[0_8px_30px_rgba(170,167,255,0.15)]"
      >
        {/* Cover Artwork */}
        <img
          src={coverUrl}
          alt={displayTitle}
          loading={priority ? 'eager' : 'lazy'}
          className="h-full w-full object-cover object-center transition-transform duration-500 ease-out group-hover:scale-105"
        />

        {/* Gradient Overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#08090B]/90 via-[#08090B]/10 to-transparent opacity-80 group-hover:opacity-60 transition-opacity duration-300" />

        {/* Top Badges */}
        <div className="absolute top-2 left-2 right-2 flex items-center justify-between gap-1 pointer-events-none">
          {manga.format && (
            <span className="rounded-md bg-[#08090B]/80 backdrop-blur-md px-1.5 py-0.5 text-[10px] font-semibold tracking-wider text-[#AAA7FF] border border-[#292C35]">
              {manga.format.replace('_', ' ')}
            </span>
          )}
          {manga.averageScore && (
            <ScoreBadge score={manga.averageScore} size="sm" />
          )}
        </div>

        {/* Quick action buttons on hover */}
        <div className="absolute inset-0 flex items-center justify-center gap-3 opacity-0 group-hover:opacity-100 transition-opacity duration-200 bg-[#08090B]/40 backdrop-blur-[2px]">
          <div
            className="flex h-11 w-11 items-center justify-center rounded-full bg-[#8585F7] text-white shadow-lg transition-transform duration-200 hover:scale-110 active:scale-95"
            title="Read Synopsis"
          >
            <BookOpen size={18} />
          </div>

          <button
            onClick={handleLibraryToggle}
            type="button"
            className={`flex h-10 w-10 items-center justify-center rounded-full border transition-all duration-200 hover:scale-110 active:scale-95 ${
              libraryEntry
                ? 'bg-[#4ADE80] text-black border-[#4ADE80]'
                : 'bg-[#191C22]/90 text-white border-[#292C35] hover:border-[#8585F7]'
            }`}
            title={libraryEntry ? `Status: ${libraryEntry.status}` : 'Add to Reading List'}
          >
            {libraryEntry ? <Check size={16} /> : <Bookmark size={16} />}
          </button>
        </div>

        {/* Bottom meta */}
        <div className="absolute bottom-2 left-2 right-2 pointer-events-none">
          {manga.chapters ? (
            <span className="text-[11px] font-medium text-[#A0A3AE] drop-shadow-md">
              {manga.chapters} chapters
            </span>
          ) : manga.status ? (
            <span className="text-[11px] font-medium text-[#A0A3AE] drop-shadow-md capitalize">
              {manga.status.toLowerCase().replace('_', ' ')}
            </span>
          ) : null}
        </div>
      </Link>

      {/* Meta Info */}
      <div className="mt-2.5 flex flex-col gap-0.5">
        <Link
          to={`/manga/${manga.id}`}
          className="line-clamp-1 text-sm font-semibold text-[#F5F5F7] hover:text-[#AAA7FF] transition-colors"
          title={displayTitle}
        >
          {displayTitle}
        </Link>
        <div className="flex items-center gap-2 text-xs text-[#A0A3AE]">
          {authorName && <span className="line-clamp-1">{authorName}</span>}
          {authorName && manga.genres[0] && (
            <span className="text-[#292C35]">•</span>
          )}
          <span className="shrink-0">{manga.genres[0] || ''}</span>
        </div>
      </div>
    </div>
  );
};
