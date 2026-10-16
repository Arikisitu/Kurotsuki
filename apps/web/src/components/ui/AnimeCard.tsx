import React from 'react';
import { Link } from 'react-router-dom';
import { Anime } from '@sahasra/shared-types';
import { ScoreBadge } from './ScoreBadge';
import { Play, Bookmark, Check } from 'lucide-react';
import { useLibrary } from '../../hooks/useLibrary';

interface AnimeCardProps {
  anime: Anime;
  priority?: boolean;
}

export const AnimeCard: React.FC<AnimeCardProps> = ({ anime, priority = false }) => {
  const { getEntry, setStatus } = useLibrary();
  const libraryEntry = getEntry(anime.id);

  const displayTitle =
    anime.title.userPreferred || anime.title.english || anime.title.romaji || 'Untitled';
  const coverUrl =
    anime.artwork.cover.large || anime.artwork.cover.medium || '/logo.jpg';
  const studioName = anime.studios?.[0]?.name;

  const handleLibraryToggle = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (libraryEntry) {
      // Toggle to watching or completed
      setStatus(anime, libraryEntry.status === 'WATCHING' ? 'COMPLETED' : 'WATCHING');
    } else {
      setStatus(anime, 'PLANNING');
    }
  };

  return (
    <div className="group relative flex flex-col w-full transition-transform duration-300 ease-out hover:-translate-y-1.5">
      <Link
        to={`/anime/${anime.id}`}
        className="relative block aspect-[2/3] w-full overflow-hidden rounded-xl bg-[#111318] border border-[#292C35] transition-all duration-300 group-hover:border-[#8585F7]/60 group-hover:shadow-[0_8px_30px_rgba(133,133,247,0.15)]"
      >
        {/* Cover Artwork */}
        <img
          src={coverUrl}
          alt={displayTitle}
          loading={priority ? 'eager' : 'lazy'}
          className="h-full w-full object-cover object-center transition-transform duration-500 ease-out group-hover:scale-105"
        />

        {/* Subtle Dark Gradient Overlay for Badges */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#08090B]/90 via-[#08090B]/10 to-transparent opacity-80 group-hover:opacity-60 transition-opacity duration-300" />

        {/* Top Badges */}
        <div className="absolute top-2 left-2 right-2 flex items-center justify-between gap-1 pointer-events-none">
          {anime.format && (
            <span className="rounded-md bg-[#08090B]/80 backdrop-blur-md px-1.5 py-0.5 text-[10px] font-semibold tracking-wider text-[#A0A3AE] border border-[#292C35]">
              {anime.format.replace('_', ' ')}
            </span>
          )}
          {anime.averageScore && (
            <ScoreBadge score={anime.averageScore} size="sm" />
          )}
        </div>

        {/* Quick hover action bar */}
        <div className="absolute inset-0 flex items-center justify-center gap-3 opacity-0 group-hover:opacity-100 transition-opacity duration-200 bg-[#08090B]/40 backdrop-blur-[2px]">
          <div
            className="flex h-11 w-11 items-center justify-center rounded-full bg-[#8585F7] text-white shadow-lg transition-transform duration-200 hover:scale-110 active:scale-95"
            title="View Details"
          >
            <Play size={18} className="fill-current ml-0.5" />
          </div>

          <button
            onClick={handleLibraryToggle}
            type="button"
            className={`flex h-10 w-10 items-center justify-center rounded-full border transition-all duration-200 hover:scale-110 active:scale-95 ${
              libraryEntry
                ? 'bg-[#4ADE80] text-black border-[#4ADE80]'
                : 'bg-[#191C22]/90 text-white border-[#292C35] hover:border-[#8585F7]'
            }`}
            title={libraryEntry ? `Status: ${libraryEntry.status}` : 'Add to Planning'}
          >
            {libraryEntry ? <Check size={16} /> : <Bookmark size={16} />}
          </button>
        </div>

        {/* Bottom meta tag (e.g. Next episode or Episodes count) */}
        <div className="absolute bottom-2 left-2 right-2 pointer-events-none">
          {anime.nextAiringEpisode ? (
            <span className="inline-block rounded-md bg-[#8585F7]/20 border border-[#8585F7]/40 px-1.5 py-0.5 text-[10px] font-medium text-[#AAA7FF] backdrop-blur-md">
              Ep {anime.nextAiringEpisode.episode} airing soon
            </span>
          ) : anime.episodes ? (
            <span className="text-[11px] font-medium text-[#A0A3AE] drop-shadow-md">
              {anime.episodes} eps
            </span>
          ) : null}
        </div>
      </Link>

      {/* Meta Info */}
      <div className="mt-2.5 flex flex-col gap-0.5">
        <Link
          to={`/anime/${anime.id}`}
          className="line-clamp-1 text-sm font-semibold text-[#F5F5F7] hover:text-[#8585F7] transition-colors"
          title={displayTitle}
        >
          {displayTitle}
        </Link>
        <div className="flex items-center gap-2 text-xs text-[#A0A3AE]">
          {studioName && <span className="line-clamp-1">{studioName}</span>}
          {studioName && (anime.seasonYear || anime.genres[0]) && (
            <span className="text-[#292C35]">•</span>
          )}
          <span className="shrink-0">{anime.seasonYear || anime.genres[0] || ''}</span>
        </div>
      </div>
    </div>
  );
};
