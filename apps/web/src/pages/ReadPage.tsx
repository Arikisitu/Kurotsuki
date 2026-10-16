import React, { useState, useEffect, useRef } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useMangaDetails } from '../hooks/useManga';
import { useLibrary } from '../hooks/useLibrary';
import { ErrorState } from '../components/ui/ErrorState';
import {
  ArrowLeft,
  ChevronLeft,
  ChevronRight,
  Maximize,
  Minimize,
  BookOpen,
  Settings as SettingsIcon,
  List,
  Sparkles,
  ExternalLink as LinkIcon,
  Check,
  Columns,
  Rows,
} from 'lucide-react';

type ReadingMode = 'VERTICAL' | 'PAGED';
type ContentWidth = 'NORMAL' | 'WIDE' | 'FULL';

export const ReadPage: React.FC = () => {
  const { id, chapter: chParam } = useParams<{ id: string; chapter?: string }>();
  const navigate = useNavigate();
  const mangaId = id ? parseInt(id, 10) : undefined;
  const currentChapter = chParam ? parseInt(chParam, 10) : 1;

  const { data: manga, isLoading, isError, refetch } = useMangaDetails(mangaId);
  const { getEntry, updateProgress } = useLibrary();

  const [readingMode, setReadingMode] = useState<ReadingMode>('VERTICAL');
  const [contentWidth, setContentWidth] = useState<ContentWidth>('NORMAL');
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [showSettings, setShowSettings] = useState<boolean>(false);
  const [showChapterMenu, setShowChapterMenu] = useState<boolean>(false);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);

  const containerRef = useRef<HTMLDivElement | null>(null);

  // Total pages per simulated chapter
  const pagesCount = 18;
  const totalChapters = manga?.chapters || 50;
  const hasNextChapter = currentChapter < totalChapters;
  const hasPrevChapter = currentChapter > 1;

  // Track and save reading progress
  useEffect(() => {
    if (!mangaId) return;
    try {
      localStorage.setItem(`sahasra_manga_progress_${mangaId}`, currentChapter.toString());
      const entry = getEntry(mangaId);
      if (entry && entry.progress < currentChapter) {
        updateProgress(mangaId, currentChapter);
      }
    } catch {
      // Ignore storage error
    }
  }, [mangaId, currentChapter, getEntry, updateProgress]);

  // Keyboard navigation for paged mode
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (readingMode === 'PAGED') {
        if (e.key === 'ArrowRight' || e.key === ' ' || e.key === 'PageDown') {
          if (currentPage < pagesCount) {
            setCurrentPage((p) => p + 1);
            window.scrollTo({ top: 0, behavior: 'smooth' });
          } else if (hasNextChapter) {
            navigate(`/read/${mangaId}/${currentChapter + 1}`);
            setCurrentPage(1);
          }
        } else if (e.key === 'ArrowLeft' || e.key === 'PageUp') {
          if (currentPage > 1) {
            setCurrentPage((p) => p - 1);
            window.scrollTo({ top: 0, behavior: 'smooth' });
          } else if (hasPrevChapter) {
            navigate(`/read/${mangaId}/${currentChapter - 1}`);
            setCurrentPage(pagesCount);
          }
        }
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [readingMode, currentPage, pagesCount, hasNextChapter, hasPrevChapter, mangaId, currentChapter, navigate]);

  const toggleFullscreen = () => {
    if (!containerRef.current) return;
    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen().catch(() => {});
      setIsFullscreen(true);
    } else {
      document.exitFullscreen().catch(() => {});
      setIsFullscreen(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#8585F7] border-t-transparent" />
          <p className="text-sm font-semibold text-[#A0A3AE]">Preparing chapter viewer...</p>
        </div>
      </div>
    );
  }

  if (isError || !manga) {
    return (
      <ErrorState
        title="Manga not found"
        message="Could not load chapter pages for this manga."
        onRetry={() => refetch()}
      />
    );
  }

  const displayTitle =
    manga.title.userPreferred || manga.title.english || manga.title.romaji || 'Untitled';
  const coverUrl = manga.artwork.cover.large || manga.artwork.cover.medium;

  // Generate verified dynamic chapter demonstration artwork panels
  const getPageUrl = (pageNumber: number) => {
    // Elegant manga aesthetic placeholder canvas with chapter and page details
    const canvasBg = '#08090B';
    const textCrimson = '%23E64B55';
    const textAccent = '%238585F7';
    const textMuted = '%23A0A3AE';
    const titleEncoded = encodeURIComponent(displayTitle.slice(0, 30));

    return `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="800" height="1160" viewBox="0 0 800 1160" fill="${canvasBg}"><rect width="800" height="1160" fill="${canvasBg}" stroke="%23292C35" stroke-width="4"/><circle cx="400" cy="460" r="140" fill="%23111318" stroke="%238585F7" stroke-width="2" opacity="0.4"/><polygon points="400,380 430,440 500,450 450,500 460,570 400,535 340,570 350,500 300,450 370,440" fill="${textCrimson}" opacity="0.6"/><text x="400" y="650" font-family="sans-serif" font-size="28" font-weight="bold" fill="%23F5F5F7" text-anchor="middle">${titleEncoded}</text><text x="400" y="700" font-family="sans-serif" font-size="20" font-weight="600" fill="${textAccent}" text-anchor="middle">Chapter ${currentChapter} — Page ${pageNumber} of ${pagesCount}</text><text x="400" y="740" font-family="sans-serif" font-size="14" fill="${textMuted}" text-anchor="middle">SAHASRA Inbuilt Manga Reader Engine</text><line x1="160" y1="780" x2="640" y2="780" stroke="%23292C35" stroke-width="2"/><text x="400" y="820" font-family="sans-serif" font-size="12" fill="${textMuted}" text-anchor="middle">Official publication and licensed translation rights reserved by original authors</text></svg>`;
  };

  const getContainerWidthClass = () => {
    switch (contentWidth) {
      case 'WIDE':
        return 'max-w-4xl';
      case 'FULL':
        return 'max-w-full';
      case 'NORMAL':
      default:
        return 'max-w-2xl';
    }
  };

  return (
    <div ref={containerRef} className="space-y-6 pb-20 select-none">
      {/* Top Floating Control Bar */}
      <div className="sticky top-16 z-30 flex items-center justify-between p-3 rounded-2xl bg-[#111318]/90 backdrop-blur-md border border-[#292C35] shadow-lg">
        <div className="flex items-center gap-3">
          <Link
            to={`/manga/${manga.id}`}
            className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#191C22] border border-[#292C35] text-[#A0A3AE] hover:text-[#F5F5F7] hover:border-[#8585F7] transition-colors"
          >
            <ArrowLeft size={16} />
          </Link>
          <div>
            <h1 className="text-sm font-bold text-[#F5F5F7] line-clamp-1">{displayTitle}</h1>
            <p className="text-xs text-[#AAA7FF] font-medium">Chapter {currentChapter} of {totalChapters}</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Chapter Selector Dropdown */}
          <div className="relative">
            <button
              onClick={() => setShowChapterMenu(!showChapterMenu)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#191C22] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] hover:border-[#8585F7] transition-colors"
            >
              <List size={14} />
              <span>Ch. {currentChapter}</span>
            </button>

            {showChapterMenu && (
              <div className="absolute right-0 top-11 z-50 flex flex-col w-48 max-h-64 overflow-y-auto rounded-xl bg-[#191C22] border border-[#292C35] p-1.5 shadow-2xl">
                {Array.from({ length: totalChapters }, (_, i) => i + 1).map((ch) => (
                  <button
                    key={ch}
                    onClick={() => {
                      navigate(`/read/${manga.id}/${ch}`);
                      setShowChapterMenu(false);
                      setCurrentPage(1);
                      window.scrollTo({ top: 0, behavior: 'smooth' });
                    }}
                    className={`px-3 py-1.5 rounded-lg text-xs font-semibold text-left transition-colors ${
                      ch === currentChapter
                        ? 'bg-[#8585F7] text-black font-bold'
                        : 'text-[#F5F5F7] hover:bg-[#292C35]'
                    }`}
                  >
                    Chapter {ch}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Reading Mode Switcher */}
          <button
            onClick={() => setReadingMode(readingMode === 'VERTICAL' ? 'PAGED' : 'VERTICAL')}
            className="flex items-center gap-1 px-2.5 py-1.5 rounded-xl bg-[#191C22] border border-[#292C35] text-xs font-medium text-[#F5F5F7] hover:border-[#8585F7] transition-colors"
            title={readingMode === 'VERTICAL' ? 'Switch to Page-by-Page' : 'Switch to Vertical Scroll'}
          >
            {readingMode === 'VERTICAL' ? <Rows size={14} /> : <Columns size={14} />}
            <span className="hidden sm:inline">{readingMode === 'VERTICAL' ? 'Scroll' : 'Paged'}</span>
          </button>

          {/* Width Toggle */}
          <button
            onClick={() => {
              if (contentWidth === 'NORMAL') setContentWidth('WIDE');
              else if (contentWidth === 'WIDE') setContentWidth('FULL');
              else setContentWidth('NORMAL');
            }}
            className="px-2.5 py-1.5 rounded-xl bg-[#191C22] border border-[#292C35] text-xs font-medium text-[#F5F5F7] hover:border-[#8585F7] transition-colors"
            title="Toggle Reader Width"
          >
            {contentWidth}
          </button>

          {/* Fullscreen */}
          <button
            onClick={toggleFullscreen}
            className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#191C22] border border-[#292C35] text-[#A0A3AE] hover:text-[#F5F5F7] transition-colors"
          >
            {isFullscreen ? <Minimize size={15} /> : <Maximize size={15} />}
          </button>
        </div>
      </div>

      {/* Reader Body Canvas */}
      <div className={`mx-auto transition-all duration-200 ${getContainerWidthClass()}`}>
        {readingMode === 'VERTICAL' ? (
          /* Vertical Continuous Webtoon Scroll */
          <div className="space-y-4">
            {Array.from({ length: pagesCount }, (_, i) => i + 1).map((pg) => (
              <div
                key={pg}
                className="relative overflow-hidden rounded-2xl border border-[#292C35] bg-[#08090B] shadow-xl"
              >
                <img
                  src={getPageUrl(pg)}
                  alt={`Chapter ${currentChapter} Page ${pg}`}
                  className="w-full h-auto object-contain select-none"
                  loading="lazy"
                />
                <span className="absolute bottom-2 right-3 rounded-md bg-black/80 px-2 py-0.5 text-[10px] font-bold text-[#A0A3AE]">
                  {pg} / {pagesCount}
                </span>
              </div>
            ))}
          </div>
        ) : (
          /* Single Page Flip Mode */
          <div className="space-y-4">
            <div
              className="relative overflow-hidden rounded-2xl border border-[#292C35] bg-[#08090B] shadow-2xl cursor-pointer"
              onClick={(e) => {
                const rect = e.currentTarget.getBoundingClientRect();
                const clickX = e.clientX - rect.left;
                if (clickX > rect.width / 2) {
                  if (currentPage < pagesCount) setCurrentPage((p) => p + 1);
                  else if (hasNextChapter) {
                    navigate(`/read/${manga.id}/${currentChapter + 1}`);
                    setCurrentPage(1);
                  }
                } else {
                  if (currentPage > 1) setCurrentPage((p) => p - 1);
                  else if (hasPrevChapter) {
                    navigate(`/read/${manga.id}/${currentChapter - 1}`);
                    setCurrentPage(pagesCount);
                  }
                }
              }}
            >
              <img
                src={getPageUrl(currentPage)}
                alt={`Chapter ${currentChapter} Page ${currentPage}`}
                className="w-full h-auto object-contain select-none min-h-[600px]"
              />
              <div className="absolute inset-y-0 left-0 w-16 hover:bg-black/20 flex items-center justify-start pl-2 transition-colors">
                <ChevronLeft size={28} className="text-white/60" />
              </div>
              <div className="absolute inset-y-0 right-0 w-16 hover:bg-black/20 flex items-center justify-end pr-2 transition-colors">
                <ChevronRight size={28} className="text-white/60" />
              </div>
              <span className="absolute bottom-3 right-4 rounded-md bg-black/80 px-2.5 py-1 text-xs font-bold text-[#8585F7]">
                Page {currentPage} of {pagesCount}
              </span>
            </div>

            {/* Paged Navigation Bar */}
            <div className="flex items-center justify-between p-3 rounded-2xl bg-[#111318] border border-[#292C35]">
              <button
                disabled={currentPage <= 1 && !hasPrevChapter}
                onClick={() => {
                  if (currentPage > 1) setCurrentPage((p) => p - 1);
                  else if (hasPrevChapter) {
                    navigate(`/read/${manga.id}/${currentChapter - 1}`);
                    setCurrentPage(pagesCount);
                  }
                }}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#191C22] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] disabled:opacity-40 hover:border-[#8585F7]"
              >
                <ChevronLeft size={16} />
                <span>Previous Page</span>
              </button>

              <div className="flex items-center gap-2">
                <input
                  type="range"
                  min="1"
                  max={pagesCount}
                  value={currentPage}
                  onChange={(e) => setCurrentPage(parseInt(e.target.value, 10))}
                  className="w-32 sm:w-48 h-1.5 accent-[#8585F7] cursor-pointer"
                />
                <span className="text-xs font-bold text-[#8585F7] w-12 text-center">
                  {currentPage} / {pagesCount}
                </span>
              </div>

              <button
                disabled={currentPage >= pagesCount && !hasNextChapter}
                onClick={() => {
                  if (currentPage < pagesCount) setCurrentPage((p) => p + 1);
                  else if (hasNextChapter) {
                    navigate(`/read/${manga.id}/${currentChapter + 1}`);
                    setCurrentPage(1);
                  }
                }}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#8585F7] text-black text-xs font-bold disabled:opacity-40 hover:bg-[#7272E5]"
              >
                <span>Next Page</span>
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        )}

        {/* Chapter Transition Footer */}
        <div className="mt-8 flex flex-col sm:flex-row items-center justify-between gap-4 p-5 rounded-3xl bg-[#111318] border border-[#292C35]">
          <button
            disabled={!hasPrevChapter}
            onClick={() => {
              navigate(`/read/${manga.id}/${currentChapter - 1}`);
              setCurrentPage(1);
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
            className="w-full sm:w-auto flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-[#191C22] border border-[#292C35] text-xs font-bold text-[#F5F5F7] disabled:opacity-40 hover:border-[#8585F7] transition-colors"
          >
            <ChevronLeft size={16} />
            <span>Previous Chapter</span>
          </button>

          <div className="text-center">
            <p className="text-xs font-semibold text-[#8585F7]">You've reached the end of Chapter {currentChapter}</p>
            <p className="text-[11px] text-[#A0A3AE]">Progress saved to your personal library</p>
          </div>

          <button
            disabled={!hasNextChapter}
            onClick={() => {
              navigate(`/read/${manga.id}/${currentChapter + 1}`);
              setCurrentPage(1);
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
            className="w-full sm:w-auto flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-[#8585F7] text-black text-xs font-bold disabled:opacity-40 hover:bg-[#7272E5] transition-colors"
          >
            <span>Next Chapter</span>
            <ChevronRight size={16} />
          </button>
        </div>

        {/* Official Publisher Links */}
        {manga.externalLinks && manga.externalLinks.length > 0 && (
          <div className="mt-6 p-5 rounded-3xl bg-[#111318] border border-[#292C35] space-y-3">
            <div className="flex items-center gap-2">
              <Sparkles size={16} className="text-[#8585F7]" />
              <h3 className="text-xs font-bold uppercase tracking-wider text-[#F5F5F7]">
                Official Publishers & Reading Platforms
              </h3>
            </div>
            <div className="flex flex-wrap gap-2">
              {manga.externalLinks.map((link) => (
                <a
                  key={link.id}
                  href={link.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#191C22] hover:bg-[#292C35] border border-[#292C35] text-xs font-semibold text-[#A0A3AE] hover:text-[#F5F5F7] transition-colors"
                >
                  <span>{link.site}</span>
                  <LinkIcon size={12} />
                </a>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ReadPage;
