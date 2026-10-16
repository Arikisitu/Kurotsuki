import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAnimeSearch, useMangaSearch } from '../hooks/useSearch';
import { AnimeCard } from '../components/ui/AnimeCard';
import { MangaCard } from '../components/ui/MangaCard';
import { AnimeCardSkeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { Search, Film, BookOpen } from 'lucide-react';

export const SearchPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const queryParam = searchParams.get('q') || '';
  const [activeTab, setActiveTab] = useState<'anime' | 'manga'>('anime');
  const [inputQuery, setInputQuery] = useState(queryParam);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (inputQuery.trim()) {
      setSearchParams({ q: inputQuery.trim() });
    }
  };

  const animeQuery = useAnimeSearch({
    search: queryParam,
    perPage: 24,
  });

  const mangaQuery = useMangaSearch({
    search: queryParam,
    perPage: 24,
  });

  const isSearching =
    (activeTab === 'anime' && animeQuery.isLoading) ||
    (activeTab === 'manga' && mangaQuery.isLoading);

  const animeList = animeQuery.data?.items || [];
  const mangaList = mangaQuery.data?.items || [];
  const currentList = activeTab === 'anime' ? animeList : mangaList;

  return (
    <div className="space-y-6">
      {/* Search Header */}
      <div className="max-w-3xl">
        <h1 className="text-2xl sm:text-3xl font-extrabold text-[#F5F5F7] tracking-tight mb-2">
          Search Results
        </h1>
        <form onSubmit={handleSearchSubmit} className="relative flex items-center">
          <Search size={18} className="absolute left-4 text-[#A0A3AE]" />
          <input
            type="text"
            value={inputQuery}
            onChange={(e) => setInputQuery(e.target.value)}
            placeholder="Search anime series, movies, manga, or authors..."
            className="h-12 w-full rounded-2xl bg-[#111318] pl-12 pr-28 text-sm text-[#F5F5F7] border border-[#292C35] focus:border-[#8585F7] focus:outline-none focus:ring-1 focus:ring-[#8585F7]/30"
          />
          <button
            type="submit"
            className="absolute right-2 px-4 py-2 rounded-xl bg-[#8585F7] hover:bg-[#7272E5] text-xs font-semibold text-white transition-colors"
          >
            Search
          </button>
        </form>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-[#292C35] pb-3">
        <button
          onClick={() => setActiveTab('anime')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
            activeTab === 'anime'
              ? 'bg-[#191C22] text-[#8585F7] border border-[#292C35]'
              : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
          }`}
        >
          <Film size={15} />
          <span>Anime ({animeQuery.data?.pageInfo?.total ?? 0})</span>
        </button>

        <button
          onClick={() => setActiveTab('manga')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
            activeTab === 'manga'
              ? 'bg-[#191C22] text-[#AAA7FF] border border-[#292C35]'
              : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
          }`}
        >
          <BookOpen size={15} />
          <span>Manga ({mangaQuery.data?.pageInfo?.total ?? 0})</span>
        </button>
      </div>

      {/* Results Content */}
      {isSearching ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4 sm:gap-6">
          {Array.from({ length: 12 }).map((_, i) => (
            <AnimeCardSkeleton key={i} />
          ))}
        </div>
      ) : !queryParam ? (
        <div className="p-12 text-center text-[#A0A3AE]">
          <Search size={32} className="mx-auto mb-3 text-[#292C35]" />
          <p className="text-sm">Type a title or keywords above to search AniList database</p>
        </div>
      ) : currentList.length === 0 ? (
        <EmptyState
          title={`No ${activeTab} found`}
          message={`We couldn't find any ${activeTab} matching "${queryParam}". Check your spelling or try another keyword.`}
          actionLabel="Explore Popular"
          actionLink="/explore"
        />
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4 sm:gap-6">
          {activeTab === 'anime'
            ? animeList.map((anime, i) => (
                <AnimeCard key={anime.id} anime={anime} priority={i < 6} />
              ))
            : mangaList.map((manga, i) => (
                <MangaCard key={manga.id} manga={manga} priority={i < 6} />
              ))}
        </div>
      )}
    </div>
  );
};
