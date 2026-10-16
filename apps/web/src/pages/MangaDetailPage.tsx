import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useMangaDetails } from '../hooks/useManga';
import { ScoreBadge } from '../components/ui/ScoreBadge';
import { GenreBadge } from '../components/ui/GenreBadge';
import { DetailSkeleton } from '../components/ui/Skeleton';
import { ErrorState } from '../components/ui/ErrorState';
import { useLibrary, MediaStatusType } from '../hooks/useLibrary';
import {
  Heart,
  ExternalLink as LinkIcon,
  BookOpen,
  Share2,
  Check,
  User,
  Layers,
} from 'lucide-react';

export const MangaDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const mangaId = id ? parseInt(id, 10) : undefined;
  const { data: manga, isLoading, isError, refetch } = useMangaDetails(mangaId);
  const { getEntry, setStatus, toggleFavorite, updateProgress } = useLibrary();
  const [copied, setCopied] = useState(false);

  if (isLoading) {
    return <DetailSkeleton />;
  }

  if (isError || !manga) {
    return (
      <ErrorState
        title="Manga not found"
        message="Could not retrieve details for this manga ID from AniList."
        onRetry={() => refetch()}
      />
    );
  }

  const libraryEntry = getEntry(manga.id);
  const displayTitle =
    manga.title.userPreferred || manga.title.english || manga.title.romaji || 'Untitled';
  const bannerUrl = manga.artwork.banner || manga.artwork.cover.extraLarge || manga.artwork.cover.large;
  const coverUrl = manga.artwork.cover.large || manga.artwork.cover.medium || '/logo.jpg';
  const authorName = manga.authors?.[0]?.name?.full || manga.staff?.[0]?.name?.full;

  const handleShare = () => {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(window.location.href);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const statusOptions: MediaStatusType[] = [
    'WATCHING', // represents "Reading"
    'PLANNING',
    'COMPLETED',
    'PAUSED',
    'DROPPED',
  ];

  return (
    <div className="space-y-8 pb-12">
      {/* Banner */}
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

      {/* Grid */}
      <div className="relative -mt-24 sm:-mt-32 md:-mt-40 z-10 grid grid-cols-1 md:grid-cols-12 gap-8 px-2 sm:px-4">
        {/* Left column */}
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
            <select
              value={libraryEntry?.status || ''}
              onChange={(e) => {
                if (e.target.value) {
                  setStatus(manga, e.target.value as MediaStatusType);
                }
              }}
              className={`w-full h-11 px-4 rounded-xl text-xs font-semibold border appearance-none transition-colors ${
                libraryEntry
                  ? 'bg-[#AAA7FF] text-black border-[#AAA7FF]'
                  : 'bg-[#191C22] text-[#F5F5F7] border-[#292C35] hover:border-[#AAA7FF]'
              }`}
            >
              <option value="" disabled>
                {libraryEntry ? `Status: ${libraryEntry.status === 'WATCHING' ? 'READING' : libraryEntry.status}` : '+ Add to Reading List'}
              </option>
              {statusOptions.map((st) => (
                <option key={st} value={st} className="bg-[#191C22] text-white">
                  {st === 'WATCHING'
                    ? 'Reading'
                    : st === 'PLANNING'
                    ? 'Planning to Read'
                    : st === 'COMPLETED'
                    ? 'Completed'
                    : st === 'PAUSED'
                    ? 'On Hold / Paused'
                    : 'Dropped'}
                </option>
              ))}
            </select>

            {/* Chapter Progress Counter */}
            {libraryEntry && (
              <div className="flex items-center justify-between p-3 rounded-xl bg-[#111318] border border-[#292C35]">
                <span className="text-xs text-[#A0A3AE]">Progress:</span>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => updateProgress(manga.id, Math.max(0, libraryEntry.progress - 1))}
                    className="flex h-7 w-7 items-center justify-center rounded-lg bg-[#191C22] text-white font-bold hover:bg-[#292C35]"
                  >
                    -
                  </button>
                  <span className="text-xs font-semibold text-[#F5F5F7]">
                    {libraryEntry.progress} / {manga.chapters || '∞'} chs
                  </span>
                  <button
                    onClick={() => updateProgress(manga.id, libraryEntry.progress + 1)}
                    className="flex h-7 w-7 items-center justify-center rounded-lg bg-[#8585F7] text-white font-bold hover:bg-[#7272E5]"
                  >
                    +
                  </button>
                </div>
              </div>
            )}

            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => toggleFavorite(manga)}
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

              <button
                type="button"
                onClick={handleShare}
                className="flex items-center justify-center h-10 w-10 rounded-xl bg-[#111318] border border-[#292C35] text-[#A0A3AE] hover:text-[#F5F5F7] transition-colors"
                title="Copy share link"
              >
                {copied ? <Check size={16} className="text-[#4ADE80]" /> : <Share2 size={16} />}
              </button>
            </div>
          </div>

          {/* Quick Info Box */}
          <div className="p-4 rounded-2xl bg-[#111318] border border-[#292C35] space-y-3 text-xs">
            <h3 className="font-bold text-[#F5F5F7] uppercase tracking-wider text-[11px] pb-1 border-b border-[#292C35]">
              Details
            </h3>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Format</span>
              <span className="font-medium text-[#F5F5F7]">{manga.format}</span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Chapters</span>
              <span className="font-medium text-[#F5F5F7]">{manga.chapters ?? 'Ongoing'}</span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Volumes</span>
              <span className="font-medium text-[#F5F5F7]">{manga.volumes ?? 'Ongoing'}</span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Status</span>
              <span className="font-medium text-[#F5F5F7] capitalize">
                {manga.status.toLowerCase().replace('_', ' ')}
              </span>
            </div>

            <div className="flex justify-between">
              <span className="text-[#A0A3AE]">Author / Creator</span>
              <span className="font-medium text-[#AAA7FF]">{authorName || 'Unknown'}</span>
            </div>
          </div>

          {/* External Links */}
          {manga.externalLinks && manga.externalLinks.length > 0 && (
            <div className="p-4 rounded-2xl bg-[#111318] border border-[#292C35] space-y-2.5 text-xs">
              <h3 className="font-bold text-[#F5F5F7] uppercase tracking-wider text-[11px] pb-1 border-b border-[#292C35]">
                External Links
              </h3>
              <div className="flex flex-wrap gap-2 pt-1">
                {manga.externalLinks.map((link) => (
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

        {/* Right column */}
        <div className="md:col-span-8 lg:col-span-9 space-y-8">
          <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-2">
              {manga.averageScore && (
                <ScoreBadge score={manga.averageScore} size="lg" />
              )}
              {manga.popularity && (
                <span className="text-xs text-[#AAA7FF] font-medium">
                  • {manga.popularity.toLocaleString()} readers
                </span>
              )}
            </div>

            <h1 className="text-3xl sm:text-4xl md:text-5xl font-extrabold tracking-tight text-[#F5F5F7]">
              {displayTitle}
            </h1>

            {/* Genres */}
            <div className="flex flex-wrap gap-2 pt-1">
              {manga.genres.map((g) => (
                <GenreBadge key={g} genre={g} clickable size="md" />
              ))}
            </div>
          </div>

          {/* Synopsis */}
          <div className="space-y-2">
            <h2 className="text-lg font-bold text-[#F5F5F7] tracking-tight">Synopsis</h2>
            <p className="text-sm sm:text-base leading-relaxed text-[#A0A3AE] whitespace-pre-line">
              {manga.description || 'No synopsis available for this title.'}
            </p>
          </div>

          {/* Authors and Artists */}
          {manga.staff && manga.staff.length > 0 && (
            <div className="space-y-4">
              <h2 className="text-lg font-bold text-[#F5F5F7] tracking-tight">
                Credits & Contributors
              </h2>
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                {manga.staff.slice(0, 6).map((staffMember) => (
                  <div
                    key={staffMember.id}
                    className="flex items-center gap-3 p-2.5 rounded-xl bg-[#111318] border border-[#292C35]"
                  >
                    <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-[#191C22] text-[#AAA7FF] shrink-0">
                      <User size={18} />
                    </div>
                    <div className="min-w-0">
                      <p className="line-clamp-1 text-xs font-semibold text-[#F5F5F7]">
                        {staffMember.name.full}
                      </p>
                      <p className="line-clamp-1 text-[10px] text-[#A0A3AE]">
                        {staffMember.role || 'Contributor'}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Franchise Relations */}
          {manga.relations && manga.relations.length > 0 && (
            <div className="space-y-4">
              <h2 className="text-lg font-bold text-[#F5F5F7] tracking-tight">
                Related Works & Adaptations
              </h2>
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                {manga.relations.slice(0, 4).map((rel) => {
                  const nodeTitle =
                    rel.node.title.userPreferred || rel.node.title.english || rel.node.title.romaji || '';
                  const nodeUrl =
                    rel.node.type === 'ANIME' ? `/anime/${rel.node.id}` : `/manga/${rel.node.id}`;

                  return (
                    <Link
                      key={rel.node.id}
                      to={nodeUrl}
                      className="group flex flex-col p-2 rounded-xl bg-[#111318] border border-[#292C35] hover:border-[#AAA7FF]/50 transition-all"
                    >
                      <div className="relative aspect-[16/9] w-full rounded-lg overflow-hidden bg-[#08090B] mb-2">
                        <img
                          src={rel.node.coverImage?.large || rel.node.coverImage?.medium || ''}
                          alt={nodeTitle}
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                        />
                        <span className="absolute top-1 left-1 rounded bg-[#08090B]/80 px-1 py-0.5 text-[9px] font-bold uppercase tracking-wider text-[#AAA7FF]">
                          {rel.relationType.replace('_', ' ')}
                        </span>
                      </div>
                      <p className="line-clamp-1 text-xs font-semibold text-[#F5F5F7] group-hover:text-[#AAA7FF] transition-colors">
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
    </div>
  );
};
