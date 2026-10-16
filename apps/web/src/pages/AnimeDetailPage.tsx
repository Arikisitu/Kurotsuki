import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAnimeDetails } from '../hooks/useAnime';
import { ScoreBadge } from '../components/ui/ScoreBadge';
import { GenreBadge } from '../components/ui/GenreBadge';
import { DetailSkeleton } from '../components/ui/Skeleton';
import { ErrorState } from '../components/ui/ErrorState';
import { AnimeCard } from '../components/ui/AnimeCard';
import { useLibrary, MediaStatusType } from '../hooks/useLibrary';
import {
  Bookmark,
  Heart,
  ExternalLink as LinkIcon,
  Clock,
  Tv,
  Calendar,
  Building2,
  Share2,
  Check,
  Play,
  X,
} from 'lucide-react';

export const AnimeDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const animeId = id ? parseInt(id, 10) : undefined;
  const { data: anime, isLoading, isError, refetch } = useAnimeDetails(animeId);
  const { getEntry, setStatus, toggleFavorite } = useLibrary();
  const [showTrailer, setShowTrailer] = useState(false);
  const [copied, setCopied] = useState(false);

  if (isLoading) {
    return <DetailSkeleton />;
  }

  if (isError || !anime) {
    return (
      <ErrorState
        title="Anime not found"
        message="Could not retrieve details for this anime ID from AniList."
        onRetry={() => refetch()}
      />
    );
  }

  const libraryEntry = getEntry(anime.id);
  const displayTitle =
    anime.title.userPreferred || anime.title.english || anime.title.romaji || 'Untitled';
  const bannerUrl = anime.artwork.banner || anime.artwork.cover.extraLarge || anime.artwork.cover.large;
  const coverUrl = anime.artwork.cover.large || anime.artwork.cover.medium || '/logo.jpg';
  const studioName = anime.studios?.[0]?.name;

  const handleShare = () => {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(window.location.href);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const statusOptions: MediaStatusType[] = [
    'WATCHING',
    'PLANNING',
    'COMPLETED',
    'PAUSED',
    'DROPPED',
  ];

  return (
    <div className="space-y-8 pb-12">
      {/* Hero Banner Header */}
      <div className="relative w-full h-[280px] sm:h-[360px] md:h-[420px] rounded-3xl overflow-hidden border border-[#292C35] bg-[#111318]">
        {bannerUrl && (
          <img
            src={bannerUrl}
            alt={displayTitle}
            className="w-full h-full object-cover object-center filter brightness-[0.65]"
          />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-[#08090B] via-[#08090B]/50 to-transparent" />
      </div>

      {/* Main Grid Content */}
      <div className="relative -mt-24 sm:-mt-32 md:-mt-40 z-10 grid grid-cols-1 md:grid-cols-12 gap-8 px-2 sm:px-4">
        {/* Left Column: Poster & Quick Metadata */}
        <div className="md:col-span-4 lg:col-span-3 space-y-5">
          <div className="relative aspect-[2/3] w-full max-w-[280px] mx-auto md:max-w-none rounded-2xl overflow-hidden border-2 border-[#292C35] shadow-2xl bg-[#111318]">
            <img
              src={coverUrl}
              alt={displayTitle}
              className="w-full h-full object-cover"
            />
          </div>

          {/* Action Row */}
          <div className="space-y-2.5">
            {/* Primary Watch Action Button */}
            <Link
              to={`/watch/${anime.id}`}
              className="flex w-full items-center justify-center gap-2 h-11 px-4 rounded-xl bg-[#8585F7] hover:bg-[#7272E5] text-white text-xs font-bold shadow-lg shadow-[#8585F7]/25 transition-all"
            >
              <Play size={16} className="fill-current" />
              <span>Watch in SAHASRA Player</span>
            </Link>

            {/* Library Status Dropdown */}
            <div className="relative">
              <select
                value={libraryEntry?.status || ''}
                onChange={(e) => {
                  if (e.target.value) {
                    setStatus(anime, e.target.value as MediaStatusType);
                  }
                }}
                className={`w-full h-11 px-4 rounded-xl text-xs font-semibold border appearance-none transition-colors ${
                  libraryEntry
                    ? 'bg-[#8585F7] text-white border-[#8585F7]'
                    : 'bg-[#191C22] text-[#F5F5F7] border-[#292C35] hover:border-[#8585F7]'
                }`}
              >
                <option value="" disabled>
                  {libraryEntry ? `Status: ${libraryEntry.status}` : '+ Add to Watchlist'}
                </option>
                {statusOptions.map((st) => (
                  <option key={st} value={st} className="bg-[#191C22] text-white">
                    {st === 'WATCHING'
                      ? 'Watching'
                      : st === 'PLANNING'
                      ? 'Planning to Watch'
                      : st === 'COMPLETED'
                      ? 'Completed'
                      : st === 'PAUSED'
                      ? 'On Hold / Paused'
                      : 'Dropped'}
                  </option>
                ))}
              </select>
            </div>

            <div className="flex gap-2">
              {/* Favorite Toggle */}
              <button
                type="button"
                onClick={() => toggleFavorite(anime)}
                className={`flex-1 flex items-center justify-center gap-2 h-10 rounded-xl border text-xs font-semibold transition-colors ${
                  libraryEntry?.favorite
                    ? 'bg-[#E64B55]/15 border-[#E64B55] text-[#E64B55]'
                    : 'bg-[#111318] border-[#292C35] text-[#A0A3AE] hover:text-[#F5F5F7]'
                }`}
              >
                <Heart
                  size={15}
                  className={libraryEntry?.favorite ? 'fill-current' : ''}
                />
                <span>Favorite</span>
              </button>

              {/* Share link button */}
              <button
                type="button"
                onClick={handleShare}
                className="flex items-center justify-center h-10 w-10 rounded-xl bg-[#111318] border border-[#292C35] text-[#A0A3AE] hover:text-[#F5F5F7] transition-colors"
                title="Copy share link"
              >
                {copied ? <Check size={16} className="text-[#4ADE80]" /> : <Share2 size={16} />}
              </button>

              {/* Trailer button if available */}
              {anime.trailer?.id && (
                <button
                  type="button"
                  onClick={() => setShowTrailer(true)}
                  className="flex items-center justify-center h-10 w-10 rounded-xl bg-[#111318] border border-[#292C35] text-[#8585F7] hover:bg-[#8585F7]/10 transition-colors"
                  title="Watch Official Trailer"
                >
                  <Play size={16} className="fill-current" />
                </button>
              )}
            </div>
          </div>

          {/* Quick Specifications Box */}
          <div className="p-4 rounded-2xl bg-[#111318] border border-[#292C35] space-y-3 text-xs">
            <h3 className="font-bold text-[#F5F5F7] uppercase tracking-wider text-[11px] pb-1 border-b border-[#292C35]">
              Information
            </h3>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Format</span>
              <span className="font-medium text-[#F5F5F7]">{anime.format || 'TV'}</span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Episodes</span>
              <span className="font-medium text-[#F5F5F7]">{anime.episodes ?? 'TBA'}</span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Duration</span>
              <span className="font-medium text-[#F5F5F7]">
                {anime.duration ? `${anime.duration} mins` : 'Unknown'}
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Status</span>
              <span className="font-medium text-[#F5F5F7] capitalize">
                {anime.status.toLowerCase().replace('_', ' ')}
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Season</span>
              <span className="font-medium text-[#F5F5F7]">
                {anime.season ? `${anime.season} ` : ''}{anime.seasonYear || ''}
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Studio</span>
              <span className="font-medium text-[#8585F7]">{studioName || 'Unknown'}</span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Source</span>
              <span className="font-medium text-[#F5F5F7] capitalize">
                {anime.source ? anime.source.toLowerCase().replace('_', ' ') : 'Original'}
              </span>
            </div>
          </div>

          {/* External Links */}
          {anime.externalLinks && anime.externalLinks.length > 0 && (
            <div className="p-4 rounded-2xl bg-[#111318] border border-[#292C35] space-y-2.5 text-xs">
              <h3 className="font-bold text-[#F5F5F7] uppercase tracking-wider text-[11px] pb-1 border-b border-[#292C35]">
                External Streaming & Links
              </h3>
              <div className="flex flex-wrap gap-2 pt-1">
                {anime.externalLinks.map((link) => (
                  <a
                    key={link.id}
                    href={link.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#191C22] hover:bg-[#292C35] border border-[#292C35] text-[#A0A3AE] hover:text-[#F5F5F7] transition-colors"
                  >
                    <span>{link.site}</span>
                    <LinkIcon size={12} />
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Title, Synopsis, Next Airing, Cast, Relations */}
        <div className="md:col-span-8 lg:col-span-9 space-y-8">
          {/* Header Title & Ratings */}
          <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-2">
              {anime.averageScore && (
                <ScoreBadge score={anime.averageScore} size="lg" />
              )}
              {anime.meanScore && (
                <span className="text-xs text-[#A0A3AE]">
                  Mean score: {anime.meanScore}%
                </span>
              )}
              {anime.popularity && (
                <span className="text-xs text-[#8585F7] font-medium">
                  • {anime.popularity.toLocaleString()} community members
                </span>
              )}
            </div>

            <h1 className="text-3xl sm:text-4xl md:text-5xl font-extrabold tracking-tight text-[#F5F5F7]">
              {displayTitle}
            </h1>

            {anime.title.romaji && anime.title.romaji !== displayTitle && (
              <p className="text-sm text-[#A0A3AE] italic">{anime.title.romaji}</p>
            )}

            {/* Genre Chips */}
            <div className="flex flex-wrap gap-2 pt-1">
              {anime.genres.map((g) => (
                <GenreBadge key={g} genre={g} clickable size="md" />
              ))}
            </div>
          </div>

          {/* Next Airing Countdown Banner */}
          {anime.nextAiringEpisode && (
            <div className="flex items-center gap-4 p-4 rounded-2xl bg-gradient-to-r from-[#8585F7]/15 to-[#191C22] border border-[#8585F7]/30">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#8585F7]/20 text-[#8585F7]">
                <Clock size={20} />
              </div>
              <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-[#AAA7FF]">
                  Upcoming Broadcast
                </p>
                <p className="text-sm font-bold text-[#F5F5F7]">
                  Episode {anime.nextAiringEpisode.episode} airs in{' '}
                  {Math.ceil(anime.nextAiringEpisode.timeUntilAiring / 86400)} days (
                  {new Date(anime.nextAiringEpisode.airingAt * 1000).toLocaleDateString()})
                </p>
              </div>
            </div>
          )}

          {/* Synopsis */}
          <div className="space-y-2">
            <h2 className="text-lg font-bold text-[#F5F5F7] tracking-tight">Synopsis</h2>
            <p className="text-sm sm:text-base leading-relaxed text-[#A0A3AE] whitespace-pre-line">
              {anime.description || 'No synopsis available for this title.'}
            </p>
          </div>

          {/* Key Characters & Cast */}
          {anime.characters && anime.characters.length > 0 && (
            <div className="space-y-4">
              <h2 className="text-lg font-bold text-[#F5F5F7] tracking-tight">
                Characters & Voice Cast
              </h2>
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                {anime.characters.slice(0, 8).map((char) => (
                  <div
                    key={char.id}
                    className="flex items-center gap-3 p-2.5 rounded-xl bg-[#111318] border border-[#292C35]"
                  >
                    <img
                      src={char.image?.medium || char.image?.large || '/logo.jpg'}
                      alt={char.name.full || 'Character'}
                      className="h-12 w-12 rounded-lg object-cover bg-[#08090B] shrink-0"
                    />
                    <div className="min-w-0">
                      <p className="line-clamp-1 text-xs font-semibold text-[#F5F5F7]">
                        {char.name.full}
                      </p>
                      <p className="text-[10px] text-[#8585F7] capitalize">
                        {char.role.toLowerCase()}
                      </p>
                      {char.voiceActors?.[0]?.name.full && (
                        <p className="line-clamp-1 text-[10px] text-[#A0A3AE]">
                          VA: {char.voiceActors[0].name.full}
                        </p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Relations (Franchise Prequels, Sequels, etc.) */}
          {anime.relations && anime.relations.length > 0 && (
            <div className="space-y-4">
              <h2 className="text-lg font-bold text-[#F5F5F7] tracking-tight">
                Franchise Relations
              </h2>
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                {anime.relations.slice(0, 4).map((rel) => {
                  const nodeTitle =
                    rel.node.title.userPreferred || rel.node.title.english || rel.node.title.romaji || '';
                  const nodeUrl =
                    rel.node.type === 'MANGA' ? `/manga/${rel.node.id}` : `/anime/${rel.node.id}`;

                  return (
                    <Link
                      key={rel.node.id}
                      to={nodeUrl}
                      className="group flex flex-col p-2 rounded-xl bg-[#111318] border border-[#292C35] hover:border-[#8585F7]/50 transition-all"
                    >
                      <div className="relative aspect-[16/9] w-full rounded-lg overflow-hidden bg-[#08090B] mb-2">
                        <img
                          src={rel.node.coverImage?.large || rel.node.coverImage?.medium || ''}
                          alt={nodeTitle}
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                        />
                        <span className="absolute top-1 left-1 rounded bg-[#08090B]/80 px-1 py-0.5 text-[9px] font-bold uppercase tracking-wider text-[#8585F7]">
                          {rel.relationType.replace('_', ' ')}
                        </span>
                      </div>
                      <p className="line-clamp-1 text-xs font-semibold text-[#F5F5F7] group-hover:text-[#8585F7] transition-colors">
                        {nodeTitle}
                      </p>
                      <p className="text-[10px] text-[#A0A3AE]">
                        {rel.node.format || rel.node.type} • {rel.node.status || ''}
                      </p>
                    </Link>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Trailer Modal */}
      {showTrailer && anime.trailer?.site === 'youtube' && anime.trailer.id && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
          <div className="relative w-full max-w-4xl aspect-video bg-black rounded-2xl overflow-hidden border border-[#292C35] shadow-2xl">
            <button
              onClick={() => setShowTrailer(false)}
              className="absolute top-3 right-3 z-10 flex h-8 w-8 items-center justify-center rounded-full bg-[#191C22]/90 text-white hover:bg-[#8585F7] transition-colors"
            >
              <X size={18} />
            </button>
            <iframe
              src={`https://www.youtube.com/embed/${anime.trailer.id}?autoplay=1`}
              title="Official Trailer"
              className="w-full h-full border-0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
            />
          </div>
        </div>
      )}
    </div>
  );
};
