import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, X, Loader2, ArrowRight } from 'lucide-react';
import { useDebounce, useAnimeSearch, useMangaSearch } from '../../hooks/useSearch';
import { ScoreBadge } from './ScoreBadge';

interface SearchBarProps {
  placeholder?: string;
  className?: string;
  onSelect?: () => void;
}

export const SearchBar: React.FC<SearchBarProps> = ({
  placeholder = 'Search anime, manga, studios...',
  className = '',
  onSelect,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const debouncedTerm = useDebounce(searchTerm, 300);
  const containerRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  // Search anime & manga with debounced query
  const { data: animeData, isLoading: animeLoading } = useAnimeSearch({
    search: debouncedTerm,
    perPage: 5,
  });

  const { data: mangaData, isLoading: mangaLoading } = useMangaSearch({
    search: debouncedTerm,
    perPage: 4,
  });

  const isLoading = (animeLoading || mangaLoading) && debouncedTerm.trim().length > 0;
  const animeResults = animeData?.items || [];
  const mangaResults = mangaData?.items || [];
  const hasResults = animeResults.length > 0 || mangaResults.length > 0;

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      setIsOpen(false);
      navigate(`/search?q=${encodeURIComponent(searchTerm.trim())}`);
      onSelect?.();
    }
  };

  const handleItemClick = (type: 'anime' | 'manga', id: number) => {
    setIsOpen(false);
    setSearchTerm('');
    navigate(`/${type}/${id}`);
    onSelect?.();
  };

  return (
    <div ref={containerRef} className={`relative w-full ${className}`}>
      <form onSubmit={handleSubmit} className="relative w-full">
        <div className="relative flex items-center w-full">
          <Search
            size={16}
            className="absolute left-3.5 text-[#A0A3AE] pointer-events-none"
          />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setIsOpen(true);
            }}
            onFocus={() => setIsOpen(true)}
            placeholder={placeholder}
            className="h-10 w-full rounded-xl bg-[#111318] pl-10 pr-9 text-sm text-[#F5F5F7] placeholder-[#A0A3AE] border border-[#292C35] focus:border-[#8585F7] focus:outline-none focus:ring-1 focus:ring-[#8585F7]/40 transition-colors"
          />
          {searchTerm && (
            <button
              type="button"
              onClick={() => {
                setSearchTerm('');
                setIsOpen(false);
              }}
              className="absolute right-3 text-[#A0A3AE] hover:text-[#F5F5F7]"
            >
              <X size={16} />
            </button>
          )}
        </div>
      </form>

      {/* Dropdown Results Overlay */}
      {isOpen && debouncedTerm.trim().length >= 2 && (
        <div className="absolute top-full left-0 right-0 mt-2 z-50 rounded-2xl bg-[#111318]/95 backdrop-blur-xl border border-[#292C35] shadow-2xl overflow-hidden divide-y divide-[#292C35]/60 animate-in fade-in zoom-in-95 duration-150">
          {isLoading ? (
            <div className="flex items-center justify-center p-8 text-[#A0A3AE]">
              <Loader2 size={24} className="animate-spin text-[#8585F7]" />
              <span className="ml-2.5 text-xs font-medium">Searching catalog...</span>
            </div>
          ) : !hasResults ? (
            <div className="p-6 text-center text-xs text-[#A0A3AE]">
              No results found for &ldquo;<span className="text-[#F5F5F7]">{debouncedTerm}</span>&rdquo;
            </div>
          ) : (
            <>
              {/* Anime Results */}
              {animeResults.length > 0 && (
                <div className="p-2">
                  <div className="px-3 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-[#A0A3AE]">
                    Anime
                  </div>
                  {animeResults.map((anime) => {
                    const title =
                      anime.title.userPreferred || anime.title.english || anime.title.romaji || '';
                    const cover = anime.artwork.cover.medium || anime.artwork.cover.large;
                    return (
                      <button
                        key={anime.id}
                        type="button"
                        onClick={() => handleItemClick('anime', anime.id)}
                        className="flex w-full items-center gap-3 rounded-lg p-2 text-left hover:bg-[#191C22] transition-colors"
                      >
                        <img
                          src={cover || ''}
                          alt={title}
                          className="h-12 w-9 rounded-md object-cover bg-[#08090B] shrink-0"
                        />
                        <div className="flex-1 min-w-0">
                          <p className="line-clamp-1 text-sm font-medium text-[#F5F5F7]">
                            {title}
                          </p>
                          <div className="flex items-center gap-2 text-xs text-[#A0A3AE]">
                            <span>{anime.format || 'Anime'}</span>
                            {anime.seasonYear && <span>• {anime.seasonYear}</span>}
                            {anime.episodes && <span>• {anime.episodes} eps</span>}
                          </div>
                        </div>
                        {anime.averageScore && (
                          <ScoreBadge score={anime.averageScore} size="sm" />
                        )}
                      </button>
                    );
                  })}
                </div>
              )}

              {/* Manga Results */}
              {mangaResults.length > 0 && (
                <div className="p-2">
                  <div className="px-3 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-[#AAA7FF]">
                    Manga & Light Novels
                  </div>
                  {mangaResults.map((manga) => {
                    const title =
                      manga.title.userPreferred || manga.title.english || manga.title.romaji || '';
                    const cover = manga.artwork.cover.medium || manga.artwork.cover.large;
                    return (
                      <button
                        key={manga.id}
                        type="button"
                        onClick={() => handleItemClick('manga', manga.id)}
                        className="flex w-full items-center gap-3 rounded-lg p-2 text-left hover:bg-[#191C22] transition-colors"
                      >
                        <img
                          src={cover || ''}
                          alt={title}
                          className="h-12 w-9 rounded-md object-cover bg-[#08090B] shrink-0"
                        />
                        <div className="flex-1 min-w-0">
                          <p className="line-clamp-1 text-sm font-medium text-[#F5F5F7]">
                            {title}
                          </p>
                          <div className="flex items-center gap-2 text-xs text-[#A0A3AE]">
                            <span>{manga.format || 'Manga'}</span>
                            {manga.chapters && <span>• {manga.chapters} chs</span>}
                          </div>
                        </div>
                        {manga.averageScore && (
                          <ScoreBadge score={manga.averageScore} size="sm" />
                        )}
                      </button>
                    );
                  })}
                </div>
              )}

              {/* View All Footer */}
              <button
                type="button"
                onClick={handleSubmit}
                className="flex w-full items-center justify-between p-3 text-xs font-semibold text-[#8585F7] hover:bg-[#191C22] transition-colors"
              >
                <span>View all results for &ldquo;{debouncedTerm}&rdquo;</span>
                <ArrowRight size={14} />
              </button>
            </>
          )}
        </div>
      )}
    </div>
  );
};
