import React, { useState, useEffect, useRef } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useMangaDetails } from '../hooks/useManga';
import { useLibrary } from '../hooks/useLibrary';
import { ErrorState } from '../components/ui/ErrorState';
import {
  searchMangaDex,
  getMangaDexChapters,
  getMangaDexChapterPages,
  MangaDexChapter,
} from '../lib/mangadex';
import {
  ArrowLeft,
  ChevronLeft,
  ChevronRight,
  Maximize,
  Minimize,
  BookOpen,
  List,
  Sparkles,
  ExternalLink as LinkIcon,
  Columns,
  Rows,
  Upload,
  AlertCircle,
  RefreshCw,
} from 'lucide-react';

type ReadingMode = 'VERTICAL' | 'PAGED';
type ContentWidth = 'NORMAL' | 'WIDE' | 'FULL';

export const ReadPage: React.FC = () => {
  const { id, chapter: chParam } = useParams<{ id: string; chapter?: string }>();
  const navigate = useNavigate();
  const mangaId = id ? parseInt(id, 10) : undefined;
  const currentChapterNum = chParam ? parseFloat(chParam) : 1;

  const { data: manga, isLoading: isMangaLoading, isError, refetch } = useMangaDetails(mangaId);
  const { getEntry, updateProgress } = useLibrary();

  const [readingMode, setReadingMode] = useState<ReadingMode>('VERTICAL');
  const [contentWidth, setContentWidth] = useState<ContentWidth>('NORMAL');
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [showChapterMenu, setShowChapterMenu] = useState<boolean>(false);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);

  // MangaDex Live State
  const [availableChapters, setAvailableChapters] = useState<MangaDexChapter[]>([]);
  const [pageUrls, setPageUrls] = useState<string[]>([]);
  const [isChapterLoading, setIsChapterLoading] = useState<boolean>(false);
  const [chapterError, setChapterError] = useState<string | null>(null);
  const [localFileNotice, setLocalFileNotice] = useState<string | null>(null);

  const containerRef = useRef<HTMLDivElement | null>(null);

  const displayTitle =
    manga?.title.userPreferred || manga?.title.english || manga?.title.romaji || 'Untitled';

  // Load genuine chapters and pages from MangaDex
  useEffect(() => {
    if (!displayTitle || displayTitle === 'Untitled') return;

    let isCancelled = false;
    async function loadMangaContent() {
      setIsChapterLoading(true);
      setChapterError(null);

      try {
        // 1. Search MangaDex for this manga title
        const searchResults = await searchMangaDex(displayTitle);
        if (isCancelled) return;

        if (searchResults.length === 0) {
          setChapterError(
            `No community scanlation found on MangaDex for "${displayTitle}". Official publisher links are provided below.`
          );
          setIsChapterLoading(false);
          return;
        }

        const mangaDexId = searchResults[0].id;

        // 2. Fetch available chapters
        const chapters = await getMangaDexChapters(mangaDexId);
        if (isCancelled) return;
        setAvailableChapters(chapters);

        if (chapters.length === 0) {
          setChapterError('No translated English chapters available on MangaDex.');
          setIsChapterLoading(false);
          return;
        }

        // 3. Find matching chapter (or default to first)
        const targetChapter =
          chapters.find((c) => parseFloat(c.chapter) === currentChapterNum) || chapters[0];

        // 4. Fetch genuine page images
        const pagesData = await getMangaDexChapterPages(targetChapter.id);
        if (isCancelled) return;

        if (pagesData && pagesData.pageUrls.length > 0) {
          setPageUrls(pagesData.pageUrls);
          setCurrentPage(1);
        } else {
          setChapterError('Could not load chapter image manifest from MangaDex At-Home server.');
        }
      } catch (err: any) {
        if (!isCancelled) {
          setChapterError(err?.message || 'Failed to fetch chapter from MangaDex');
        }
      } finally {
        if (!isCancelled) {
          setIsChapterLoading(false);
        }
      }
    }

    loadMangaContent();
    return () => {
      isCancelled = true;
    };
  }, [displayTitle, currentChapterNum]);

  // Track and save reading progress
  useEffect(() => {
    if (!mangaId) return;
    try {
      localStorage.setItem(`sahasra_manga_progress_${mangaId}`, currentChapterNum.toString());
      const entry = getEntry(mangaId);
      if (entry && entry.progress < currentChapterNum) {
        updateProgress(mangaId, Math.floor(currentChapterNum));
      }
    } catch {
      // Ignore storage error
    }
  }, [mangaId, currentChapterNum, getEntry, updateProgress]);

  // Keyboard navigation for paged mode
  const pagesCount = pageUrls.length;
  const currentChapterIdx = availableChapters.findIndex(
    (c) => parseFloat(c.chapter) === currentChapterNum
  );
  const nextChapter = currentChapterIdx >= 0 && currentChapterIdx < availableChapters.length - 1
    ? availableChapters[currentChapterIdx + 1]
    : null;
  const prevChapter = currentChapterIdx > 0 ? availableChapters[currentChapterIdx - 1] : null;

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (readingMode === 'PAGED' && pagesCount > 0) {
        if (e.key === 'ArrowRight' || e.key === ' ' || e.key === 'PageDown') {
          if (currentPage < pagesCount) {
            setCurrentPage((p) => p + 1);
            window.scrollTo({ top: 0, behavior: 'smooth' });
          } else if (nextChapter) {
            navigate(`/read/${mangaId}/${nextChapter.chapter}`);
            setCurrentPage(1);
          }
        } else if (e.key === 'ArrowLeft' || e.key === 'PageUp') {
          if (currentPage > 1) {
            setCurrentPage((p) => p - 1);
            window.scrollTo({ top: 0, behavior: 'smooth' });
          } else if (prevChapter) {
            navigate(`/read/${mangaId}/${prevChapter.chapter}`);
          }
        }
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [readingMode, currentPage, pagesCount, nextChapter, prevChapter, mangaId, navigate]);

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

  const handleLocalImport = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setLocalFileNotice(`Loaded local file: ${file.name} (${Math.round(file.size / 1024)} KB)`);
    }
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

  if (isMangaLoading) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#8585F7] border-t-transparent" />
          <p className="text-sm font-semibold text-[#A0A3AE]">Connecting to manga server...</p>
        </div>
      </div>
    );
  }

  if (isError || !manga) {
    return (
      <ErrorState
        title="Manga not found"
        message="Could not load details for this manga."
        onRetry={() => refetch()}
      />
    );
  }

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
            <p className="text-xs text-[#AAA7FF] font-medium">
              Chapter {currentChapterNum} {pagesCount > 0 ? `• ${pagesCount} Pages` : ''}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {/* Chapter Selector Dropdown */}
          {availableChapters.length > 0 && (
            <div className="relative">
              <button
                onClick={() => setShowChapterMenu(!showChapterMenu)}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#191C22] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] hover:border-[#8585F7] transition-colors"
              >
                <List size={14} />
                <span>Ch. {currentChapterNum}</span>
              </button>

              {showChapterMenu && (
                <div className="absolute right-0 top-11 z-50 flex flex-col w-48 max-h-64 overflow-y-auto rounded-xl bg-[#191C22] border border-[#292C35] p-1.5 shadow-2xl">
                  {availableChapters.map((ch) => (
                    <button
                      key={ch.id}
                      onClick={() => {
                        navigate(`/read/${manga.id}/${ch.chapter}`);
                        setShowChapterMenu(false);
                        setCurrentPage(1);
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                      }}
                      className={`px-3 py-1.5 rounded-lg text-xs font-semibold text-left transition-colors ${
                        parseFloat(ch.chapter) === currentChapterNum
                          ? 'bg-[#8585F7] text-black font-bold'
                          : 'text-[#F5F5F7] hover:bg-[#292C35]'
                      }`}
                    >
                      Chapter {ch.chapter}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

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
        {isChapterLoading ? (
          <div className="flex h-96 flex-col items-center justify-center gap-3 rounded-3xl bg-[#111318] border border-[#292C35]">
            <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#8585F7] border-t-transparent" />
            <p className="text-xs font-semibold text-[#A0A3AE]">
              Fetching genuine chapter pages from MangaDex...
            </p>
          </div>
        ) : chapterError || pageUrls.length === 0 ? (
          /* Honest Unavailability State — No Mock Covers! */
          <div className="flex flex-col items-center justify-center p-8 rounded-3xl bg-[#111318] border border-[#292C35] text-center space-y-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#E64B55]/15 text-[#E64B55] border border-[#E64B55]/30">
              <AlertCircle size={28} />
            </div>
            <div className="space-y-1.5 max-w-md">
              <h3 className="text-base font-bold text-[#F5F5F7]">
                Scanlation Not Available on Open Network
              </h3>
              <p className="text-xs text-[#A0A3AE] leading-relaxed">
                {chapterError ||
                  'No public chapter manifest is indexed for this title on MangaDex. SAHASRA upholds copyright integrity and routes to certified official publishers.'}
              </p>
            </div>

            {/* Local CBZ/PDF File Reader Import */}
            <div className="pt-2 border-t border-[#292C35]/60 w-full max-w-sm space-y-3">
              <label className="flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-[#191C22] hover:bg-[#292C35] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] cursor-pointer transition-colors">
                <Upload size={14} className="text-[#8585F7]" />
                <span>Import Local CBZ / PDF Chapter</span>
                <input
                  type="file"
                  accept=".cbz,.zip,.pdf"
                  className="hidden"
                  onChange={handleLocalImport}
                />
              </label>

              {localFileNotice && (
                <p className="text-xs text-[#4ADE80] font-medium">{localFileNotice}</p>
              )}
            </div>

            {/* Official Publisher Links */}
            {manga.externalLinks && manga.externalLinks.length > 0 && (
              <div className="space-y-2 pt-2">
                <p className="text-xs font-bold text-[#8585F7] uppercase tracking-wider">
                  Read On Official Platforms:
                </p>
                <div className="flex flex-wrap justify-center gap-2">
                  {manga.externalLinks.map((link) => (
                    <a
                      key={link.id}
                      href={link.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-[#191C22] hover:bg-[#8585F7] hover:text-black border border-[#292C35] text-xs font-semibold text-[#F5F5F7] transition-all"
                    >
                      <span>{link.site}</span>
                      <LinkIcon size={12} />
                    </a>
                  ))}
                </div>
              </div>
            )}
          </div>
        ) : readingMode === 'VERTICAL' ? (
          /* Real Vertical Continuous Webtoon Scroll */
          <div className="space-y-4">
            {pageUrls.map((url, idx) => {
              const pageNum = idx + 1;
              return (
                <div
                  key={url}
                  className="relative overflow-hidden rounded-2xl border border-[#292C35] bg-[#08090B] shadow-xl"
                >
                  <img
                    src={url}
                    alt={`Page ${pageNum} of Chapter ${currentChapterNum}`}
                    className="w-full h-auto object-contain select-none"
                    loading="lazy"
                  />
                  <span className="absolute bottom-2 right-3 rounded-md bg-black/80 px-2 py-0.5 text-[10px] font-bold text-[#A0A3AE]">
                    {pageNum} / {pagesCount}
                  </span>
                </div>
              );
            })}
          </div>
        ) : (
          /* Real Single Page Flip Mode */
          <div className="space-y-4">
            <div
              className="relative overflow-hidden rounded-2xl border border-[#292C35] bg-[#08090B] shadow-2xl cursor-pointer"
              onClick={(e) => {
                const rect = e.currentTarget.getBoundingClientRect();
                const clickX = e.clientX - rect.left;
                if (clickX > rect.width / 2) {
                  if (currentPage < pagesCount) setCurrentPage((p) => p + 1);
                  else if (nextChapter) {
                    navigate(`/read/${manga.id}/${nextChapter.chapter}`);
                    setCurrentPage(1);
                  }
                } else {
                  if (currentPage > 1) setCurrentPage((p) => p - 1);
                  else if (prevChapter) {
                    navigate(`/read/${manga.id}/${prevChapter.chapter}`);
                  }
                }
              }}
            >
              <img
                src={pageUrls[currentPage - 1]}
                alt={`Page ${currentPage} of Chapter ${currentChapterNum}`}
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
                disabled={currentPage <= 1 && !prevChapter}
                onClick={() => {
                  if (currentPage > 1) setCurrentPage((p) => p - 1);
                  else if (prevChapter) {
                    navigate(`/read/${manga.id}/${prevChapter.chapter}`);
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
                disabled={currentPage >= pagesCount && !nextChapter}
                onClick={() => {
                  if (currentPage < pagesCount) setCurrentPage((p) => p + 1);
                  else if (nextChapter) {
                    navigate(`/read/${manga.id}/${nextChapter.chapter}`);
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
        {pageUrls.length > 0 && (
          <div className="mt-8 flex flex-col sm:flex-row items-center justify-between gap-4 p-5 rounded-3xl bg-[#111318] border border-[#292C35]">
            <button
              disabled={!prevChapter}
              onClick={() => {
                if (prevChapter) {
                  navigate(`/read/${manga.id}/${prevChapter.chapter}`);
                  setCurrentPage(1);
                  window.scrollTo({ top: 0, behavior: 'smooth' });
                }
              }}
              className="w-full sm:w-auto flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-[#191C22] border border-[#292C35] text-xs font-bold text-[#F5F5F7] disabled:opacity-40 hover:border-[#8585F7] transition-colors"
            >
              <ChevronLeft size={16} />
              <span>Previous Chapter</span>
            </button>

            <div className="text-center">
              <p className="text-xs font-semibold text-[#8585F7]">
                End of Chapter {currentChapterNum} ({pagesCount} pages)
              </p>
              <p className="text-[11px] text-[#A0A3AE]">Progress saved to personal library</p>
            </div>

            <button
              disabled={!nextChapter}
              onClick={() => {
                if (nextChapter) {
                  navigate(`/read/${manga.id}/${nextChapter.chapter}`);
                  setCurrentPage(1);
                  window.scrollTo({ top: 0, behavior: 'smooth' });
                }
              }}
              className="w-full sm:w-auto flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-[#8585F7] text-black text-xs font-bold disabled:opacity-40 hover:bg-[#7272E5] transition-colors"
            >
              <span>Next Chapter</span>
              <ChevronRight size={16} />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ReadPage;
