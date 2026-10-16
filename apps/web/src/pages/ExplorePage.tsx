import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAnimeSearch, useMangaSearch } from '../hooks/useSearch';
import { AnimeCard } from '../components/ui/AnimeCard';
import { MangaCard } from '../components/ui/MangaCard';
import { AnimeCardSkeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { Filter, X, ChevronLeft, ChevronRight, SlidersHorizontal } from 'lucide-react';

const GENRES = [
  'Action',
  'Adventure',
  'Comedy',
  'Drama',
  'Ecchi',
  'Fantasy',
  'Horror',
  'Mahou Shoujo',
  'Mecha',
  'Music',
  'Mystery',
  'Psychological',
  'Romance',
  'Sci-Fi',
  'Slice of Life',
  'Sports',
  'Supernatural',
  'Thriller',
];

const SEASONS = ['WINTER', 'SPRING', 'SUMMER', 'FALL'];
const FORMATS = ['TV', 'TV_SHORT', 'MOVIE', 'SPECIAL', 'OVA', 'ONA'];
const STATUSES = ['RELEASING', 'FINISHED', 'NOT_YET_RELEASED', 'CANCELLED'];
const SORTS = [
  { label: 'Popularity', value: 'POPULARITY_DESC' },
  { label: 'Trending', value: 'TRENDING_DESC' },
  { label: 'Highest Rated', value: 'SCORE_DESC' },
  { label: 'Community Favorites', value: 'FAVOURITES_DESC' },
  { label: 'Release Date', value: 'START_DATE_DESC' },
];

export const ExplorePage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const typeParam = searchParams.get('type') === 'manga' ? 'manga' : 'anime';
  const genreParam = searchParams.get('genre') || '';
  const seasonParam = searchParams.get('season') || '';
  const yearParam = searchParams.get('year') ? parseInt(searchParams.get('year')!, 10) : undefined;
  const formatParam = searchParams.get('format') || '';
  const statusParam = searchParams.get('status') || '';
  const sortParam = searchParams.get('sort') || 'POPULARITY_DESC';
  const pageParam = searchParams.get('page') ? parseInt(searchParams.get('page')!, 10) : 1;

  const [mediaType, setMediaType] = useState<'anime' | 'manga'>(typeParam);
  const [selectedGenre, setSelectedGenre] = useState<string>(genreParam);
  const [selectedSeason, setSelectedSeason] = useState<string>(seasonParam);
  const [selectedYear, setSelectedYear] = useState<number | undefined>(yearParam);
  const [selectedFormat, setSelectedFormat] = useState<string>(formatParam);
  const [selectedStatus, setSelectedStatus] = useState<string>(statusParam);
  const [selectedSort, setSelectedSort] = useState<string>(sortParam);
  const [page, setPage] = useState<number>(pageParam);
  const [showFiltersMobile, setShowFiltersMobile] = useState(false);

  // Sync state with URL params
  useEffect(() => {
    const params = new URLSearchParams();
    if (mediaType === 'manga') params.set('type', 'manga');
    if (selectedGenre) params.set('genre', selectedGenre);
    if (selectedSeason) params.set('season', selectedSeason);
    if (selectedYear) params.set('year', selectedYear.toString());
    if (selectedFormat) params.set('format', selectedFormat);
    if (selectedStatus) params.set('status', selectedStatus);
    if (selectedSort !== 'POPULARITY_DESC') params.set('sort', selectedSort);
    if (page > 1) params.set('page', page.toString());
    setSearchParams(params, { replace: true });
  }, [mediaType, selectedGenre, selectedSeason, selectedYear, selectedFormat, selectedStatus, selectedSort, page, setSearchParams]);

  // Anime Search Query
  const animeQuery = useAnimeSearch({
    genre: selectedGenre ? [selectedGenre] : undefined,
    season: selectedSeason || undefined,
    year: selectedYear,
    format: selectedFormat ? [selectedFormat] : undefined,
    status: selectedStatus || undefined,
    sort: [selectedSort],
    page,
    perPage: 24,
  });

  // Manga Search Query
  const mangaQuery = useMangaSearch({
    genre: selectedGenre ? [selectedGenre] : undefined,
    format: selectedFormat ? [selectedFormat] : undefined,
    status: selectedStatus || undefined,
    sort: [selectedSort],
    page,
    perPage: 24,
  });

  const activeQuery = mediaType === 'anime' ? animeQuery : mangaQuery;
  const items = activeQuery.data?.items || [];
  const pageInfo = activeQuery.data?.pageInfo;
  const isLoading = activeQuery.isLoading;

  const clearAllFilters = () => {
    setSelectedGenre('');
    setSelectedSeason('');
    setSelectedYear(undefined);
    setSelectedFormat('');
    setSelectedStatus('');
    setSelectedSort('POPULARITY_DESC');
    setPage(1);
  };

  const hasActiveFilters =
    Boolean(selectedGenre) ||
    Boolean(selectedSeason) ||
    Boolean(selectedYear) ||
    Boolean(selectedFormat) ||
    Boolean(selectedStatus) ||
    selectedSort !== 'POPULARITY_DESC';

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-[#F5F5F7] tracking-tight">
            Explore Catalog
          </h1>
          <p className="text-xs sm:text-sm text-[#A0A3AE]">
            Browse and discover across thousands of titles with precision filters
          </p>
        </div>

        {/* Media Type Switcher & Mobile Filter Toggle */}
        <div className="flex items-center gap-2.5">
          <div className="flex p-1 rounded-xl bg-[#111318] border border-[#292C35]">
            <button
              onClick={() => {
                setMediaType('anime');
                setPage(1);
              }}
              className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                mediaType === 'anime'
                  ? 'bg-[#8585F7] text-white shadow'
                  : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
              }`}
            >
              Anime
            </button>
            <button
              onClick={() => {
                setMediaType('manga');
                setPage(1);
              }}
              className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                mediaType === 'manga'
                  ? 'bg-[#8585F7] text-white shadow'
                  : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
              }`}
            >
              Manga
            </button>
          </div>

          <button
            onClick={() => setShowFiltersMobile(!showFiltersMobile)}
            className="sm:hidden flex items-center gap-1.5 px-3 py-2 rounded-xl bg-[#111318] border border-[#292C35] text-xs font-semibold text-[#F5F5F7]"
          >
            <SlidersHorizontal size={14} />
            <span>Filters</span>
          </button>
        </div>
      </div>

      {/* Filter Bar */}
      <div
        className={`${
          showFiltersMobile ? 'flex' : 'hidden'
        } sm:flex flex-wrap items-center gap-2.5 p-4 rounded-2xl bg-[#111318] border border-[#292C35]`}
      >
        <div className="flex items-center gap-1.5 text-xs font-bold text-[#8585F7] mr-1">
          <Filter size={14} />
          <span>Filters:</span>
        </div>

        {/* Genre Selector */}
        <select
          value={selectedGenre}
          onChange={(e) => {
            setSelectedGenre(e.target.value);
            setPage(1);
          }}
          className="h-9 rounded-xl bg-[#191C22] px-3 text-xs font-medium text-[#F5F5F7] border border-[#292C35] focus:border-[#8585F7] focus:outline-none"
        >
          <option value="">All Genres</option>
          {GENRES.map((g) => (
            <option key={g} value={g}>
              {g}
            </option>
          ))}
        </select>

        {/* Season Selector (Anime only) */}
        {mediaType === 'anime' && (
          <select
            value={selectedSeason}
            onChange={(e) => {
              setSelectedSeason(e.target.value);
              setPage(1);
            }}
            className="h-9 rounded-xl bg-[#191C22] px-3 text-xs font-medium text-[#F5F5F7] border border-[#292C35] focus:border-[#8585F7] focus:outline-none"
          >
            <option value="">All Seasons</option>
            {SEASONS.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        )}

        {/* Format Selector */}
        <select
          value={selectedFormat}
          onChange={(e) => {
            setSelectedFormat(e.target.value);
            setPage(1);
          }}
          className="h-9 rounded-xl bg-[#191C22] px-3 text-xs font-medium text-[#F5F5F7] border border-[#292C35] focus:border-[#8585F7] focus:outline-none"
        >
          <option value="">All Formats</option>
          {FORMATS.map((f) => (
            <option key={f} value={f}>
              {f.replace('_', ' ')}
            </option>
          ))}
        </select>

        {/* Status Selector */}
        <select
          value={selectedStatus}
          onChange={(e) => {
            setSelectedStatus(e.target.value);
            setPage(1);
          }}
          className="h-9 rounded-xl bg-[#191C22] px-3 text-xs font-medium text-[#F5F5F7] border border-[#292C35] focus:border-[#8585F7] focus:outline-none"
        >
          <option value="">All Statuses</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s.toLowerCase().replace('_', ' ')}
            </option>
          ))}
        </select>

        {/* Sort Order Selector */}
        <select
          value={selectedSort}
          onChange={(e) => {
            setSelectedSort(e.target.value);
            setPage(1);
          }}
          className="h-9 rounded-xl bg-[#191C22] px-3 text-xs font-medium text-[#F5F5F7] border border-[#292C35] focus:border-[#8585F7] focus:outline-none ml-auto"
        >
          {SORTS.map((s) => (
            <option key={s.value} value={s.value}>
              Sort by: {s.label}
            </option>
          ))}
        </select>

        {/* Clear Filters Button */}
        {hasActiveFilters && (
          <button
            onClick={clearAllFilters}
            className="flex items-center gap-1 h-9 px-3 rounded-xl bg-[#EF4444]/10 hover:bg-[#EF4444]/20 border border-[#EF4444]/30 text-xs font-medium text-[#EF4444] transition-colors"
          >
            <X size={14} />
            <span>Reset</span>
          </button>
        )}
      </div>

      {/* Media Results Grid */}
      {isLoading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4 sm:gap-6">
          {Array.from({ length: 24 }).map((_, i) => (
            <AnimeCardSkeleton key={i} />
          ))}
        </div>
      ) : items.length === 0 ? (
        <EmptyState
          title="No results match your filters"
          message="Try adjusting your genre, release status, or sort criteria to discover more titles."
          actionLabel="Clear Filters"
          actionLink="/explore"
        />
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4 sm:gap-6">
          {items.map((item, index) =>
            mediaType === 'manga' ? (
              <MangaCard key={item.id} manga={item as any} priority={index < 6} />
            ) : (
              <AnimeCard key={item.id} anime={item as any} priority={index < 6} />
            )
          )}
        </div>
      )}

      {/* Pagination Controls */}
      {pageInfo && (pageInfo.hasNextPage || page > 1) && (
        <div className="flex items-center justify-center gap-3 pt-6 pb-4">
          <button
            onClick={() => {
              setPage((prev) => Math.max(1, prev - 1));
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
            disabled={page <= 1}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#111318] hover:bg-[#191C22] disabled:opacity-40 border border-[#292C35] text-xs font-semibold text-[#F5F5F7] transition-colors"
          >
            <ChevronLeft size={16} />
            <span>Previous</span>
          </button>

          <span className="text-xs font-medium text-[#A0A3AE] px-3">
            Page {page} {pageInfo.lastPage ? `of ${pageInfo.lastPage}` : ''}
          </span>

          <button
            onClick={() => {
              setPage((prev) => prev + 1);
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
            disabled={!pageInfo.hasNextPage}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#111318] hover:bg-[#191C22] disabled:opacity-40 border border-[#292C35] text-xs font-semibold text-[#F5F5F7] transition-colors"
          >
            <span>Next</span>
            <ChevronRight size={16} />
          </button>
        </div>
      )}
    </div>
  );
};
