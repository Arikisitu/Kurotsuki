import React, { useState, useRef, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useAnimeDetails } from '../hooks/useAnime';
import { useLibrary } from '../hooks/useLibrary';
import { ErrorState } from '../components/ui/ErrorState';
import { ScoreBadge } from '../components/ui/ScoreBadge';
import {
  Play,
  Pause,
  RotateCcw,
  RotateCw,
  Volume2,
  VolumeX,
  Maximize,
  Minimize,
  ArrowLeft,
  SkipForward,
  Settings as SettingsIcon,
  ExternalLink as LinkIcon,
  Tv,
  List,
  Sparkles,
  Info,
} from 'lucide-react';

interface WatchProgress {
  currentTime: number;
  duration: number;
  updatedAt: number;
}

export const WatchPage: React.FC = () => {
  const { id, episode: epParam } = useParams<{ id: string; episode?: string }>();
  const navigate = useNavigate();
  const animeId = id ? parseInt(id, 10) : undefined;
  const currentEpisode = epParam ? parseInt(epParam, 10) : 1;

  const { data: anime, isLoading, isError, refetch } = useAnimeDetails(animeId);
  const { getEntry, updateProgress } = useLibrary();

  const videoRef = useRef<HTMLVideoElement | null>(null);
  const playerContainerRef = useRef<HTMLDivElement | null>(null);

  // Player state
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [duration, setDuration] = useState<number>(0);
  const [volume, setVolume] = useState<number>(1);
  const [isMuted, setIsMuted] = useState<boolean>(false);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);
  const [playbackSpeed, setPlaybackSpeed] = useState<number>(1);
  const [showSpeedMenu, setShowSpeedMenu] = useState<boolean>(false);
  const [showControls, setShowControls] = useState<boolean>(true);
  const [useSampleStream, setUseSampleStream] = useState<boolean>(false);
  const [useTrailerStream, setUseTrailerStream] = useState<boolean>(false);
  const [showEpisodeDrawer, setShowEpisodeDrawer] = useState<boolean>(false);

  const controlsTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Sample legal demonstration media for testing player engine
  const sampleMediaUrl =
    'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4';

  const storageKey = `sahasra_progress_${animeId}_ep_${currentEpisode}`;

  // Restore saved progress on mount / episode change
  useEffect(() => {
    try {
      const saved = localStorage.getItem(storageKey);
      if (saved && videoRef.current) {
        const parsed: WatchProgress = JSON.parse(saved);
        if (parsed.currentTime > 5 && parsed.duration > 0 && parsed.currentTime < parsed.duration - 10) {
          videoRef.current.currentTime = parsed.currentTime;
          setCurrentTime(parsed.currentTime);
        }
      }
    } catch {
      // Ignore storage read error
    }
  }, [storageKey, useSampleStream]);

  // Save progress periodically
  useEffect(() => {
    if (!animeId || currentTime <= 0) return;
    const progressData: WatchProgress = {
      currentTime,
      duration,
      updatedAt: Date.now(),
    };
    try {
      localStorage.setItem(storageKey, JSON.stringify(progressData));
    } catch {
      // Ignore storage error
    }

    // Also update library progress when reaching 80% of an episode
    if (duration > 0 && currentTime / duration > 0.8) {
      const entry = getEntry(animeId);
      if (entry && entry.progress < currentEpisode) {
        updateProgress(animeId, currentEpisode);
      }
    }
  }, [currentTime, duration, animeId, currentEpisode, storageKey, getEntry, updateProgress]);

  // Activity timer to hide controls
  const handleMouseMove = () => {
    setShowControls(true);
    if (controlsTimeoutRef.current) {
      clearTimeout(controlsTimeoutRef.current);
    }
    if (isPlaying) {
      controlsTimeoutRef.current = setTimeout(() => {
        setShowControls(false);
      }, 3500);
    }
  };

  const togglePlay = () => {
    if (!videoRef.current) return;
    if (isPlaying) {
      videoRef.current.pause();
      setIsPlaying(false);
    } else {
      videoRef.current.play().catch(() => {});
      setIsPlaying(true);
    }
  };

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = parseFloat(e.target.value);
    if (videoRef.current) {
      videoRef.current.currentTime = target;
      setCurrentTime(target);
    }
  };

  const skipTime = (seconds: number) => {
    if (!videoRef.current) return;
    const newTime = Math.min(Math.max(videoRef.current.currentTime + seconds, 0), duration);
    videoRef.current.currentTime = newTime;
    setCurrentTime(newTime);
  };

  const toggleMute = () => {
    if (!videoRef.current) return;
    const newMuted = !isMuted;
    videoRef.current.muted = newMuted;
    setIsMuted(newMuted);
  };

  const handleVolumeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseFloat(e.target.value);
    setVolume(val);
    if (videoRef.current) {
      videoRef.current.volume = val;
      videoRef.current.muted = val === 0;
      setIsMuted(val === 0);
    }
  };

  const handleSpeedSelect = (speed: number) => {
    setPlaybackSpeed(speed);
    if (videoRef.current) {
      videoRef.current.playbackRate = speed;
    }
    setShowSpeedMenu(false);
  };

  const toggleFullscreen = () => {
    if (!playerContainerRef.current) return;
    if (!document.fullscreenElement) {
      playerContainerRef.current.requestFullscreen().catch(() => {});
      setIsFullscreen(true);
    } else {
      document.exitFullscreen().catch(() => {});
      setIsFullscreen(false);
    }
  };

  const formatTime = (seconds: number) => {
    if (isNaN(seconds) || seconds < 0) return '00:00';
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  if (isLoading) {
    return (
      <div className="flex h-[60vh] items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#8585F7] border-t-transparent" />
          <p className="text-sm font-semibold text-[#A0A3AE]">Preparing theater...</p>
        </div>
      </div>
    );
  }

  if (isError || !anime) {
    return (
      <ErrorState
        title="Anime not found"
        message="Could not load streaming media for this anime ID."
        onRetry={() => refetch()}
      />
    );
  }

  const displayTitle =
    anime.title.userPreferred || anime.title.english || anime.title.romaji || 'Untitled';
  const totalEpisodes = anime.episodes || 12;
  const hasNextEpisode = currentEpisode < totalEpisodes;

  return (
    <div className="space-y-6 pb-12" onMouseMove={handleMouseMove}>
      {/* Top Header Bar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Link
            to={`/anime/${anime.id}`}
            className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#111318] border border-[#292C35] text-[#A0A3AE] hover:text-[#F5F5F7] hover:border-[#8585F7] transition-colors"
          >
            <ArrowLeft size={18} />
          </Link>
          <div>
            <h1 className="text-lg font-bold text-[#F5F5F7] line-clamp-1">{displayTitle}</h1>
            <p className="text-xs text-[#8585F7] font-medium">Episode {currentEpisode} of {totalEpisodes}</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {anime.averageScore && <ScoreBadge score={anime.averageScore} size="sm" />}
          <button
            onClick={() => setShowEpisodeDrawer(!showEpisodeDrawer)}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-[#111318] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] hover:border-[#8585F7] transition-colors"
          >
            <List size={14} />
            <span>Episodes</span>
          </button>
        </div>
      </div>

      {/* Video Player Box */}
      <div
        ref={playerContainerRef}
        className="relative aspect-video w-full overflow-hidden rounded-3xl border border-[#292C35] bg-black shadow-2xl group select-none"
      >
        {useTrailerStream && anime.trailer?.site === 'youtube' && anime.trailer.id ? (
          <iframe
            src={`https://www.youtube.com/embed/${anime.trailer.id}?autoplay=1&enablejsapi=1`}
            title={displayTitle}
            className="h-full w-full border-0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          />
        ) : useSampleStream ? (
          <video
            ref={videoRef}
            src={sampleMediaUrl}
            className="h-full w-full object-contain cursor-pointer"
            onClick={togglePlay}
            onTimeUpdate={() => {
              if (videoRef.current) {
                setCurrentTime(videoRef.current.currentTime);
              }
            }}
            onLoadedMetadata={() => {
              if (videoRef.current) {
                setDuration(videoRef.current.duration);
              }
            }}
            onEnded={() => {
              setIsPlaying(false);
              if (hasNextEpisode) {
                navigate(`/watch/${anime.id}/${currentEpisode + 1}`);
              }
            }}
          />
        ) : (
          /* Notice for Metadata provider without direct raw video link */
          <div className="flex h-full w-full flex-col items-center justify-center p-6 text-center bg-[#08090B]">
            <div className="max-w-xl space-y-4">
              <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-[#8585F7]/15 text-[#8585F7] border border-[#8585F7]/30">
                <Tv size={28} />
              </div>

              <div className="space-y-1.5">
                <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#E64B55]/15 text-[#E64B55] border border-[#E64B55]/30 text-xs font-semibold uppercase tracking-wider">
                  <Sparkles size={12} />
                  Authorized Streaming Hub
                </span>
                <h3 className="text-xl font-bold text-[#F5F5F7]">
                  {displayTitle} — Episode {currentEpisode}
                </h3>
                <p className="text-xs text-[#A0A3AE] leading-relaxed">
                  AniList provides certified metadata and broadcast schedules. SAHASRA upholds strict
                  anti-piracy and creator-rights standards by routing directly to licensed streaming
                  providers.
                </p>
              </div>

              {/* Streaming Partners */}
              {anime.externalLinks && anime.externalLinks.length > 0 ? (
                <div className="space-y-2 pt-2">
                  <p className="text-xs font-semibold text-[#8585F7] uppercase tracking-wider">
                    Official Streaming Partners:
                  </p>
                  <div className="flex flex-wrap justify-center gap-2">
                    {anime.externalLinks.map((link) => (
                      <a
                        key={link.id}
                        href={link.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#111318] hover:bg-[#8585F7] hover:text-black border border-[#292C35] text-xs font-bold text-[#F5F5F7] transition-all shadow-md group"
                      >
                        <span>{link.site}</span>
                        <LinkIcon size={12} className="group-hover:translate-x-0.5 transition-transform" />
                      </a>
                    ))}
                  </div>
                </div>
              ) : null}

              {/* In-app Fallback Options */}
              <div className="flex flex-wrap justify-center gap-3 pt-3 border-t border-[#292C35]/50">
                {anime.trailer?.id && (
                  <button
                    onClick={() => {
                      setUseTrailerStream(true);
                      setUseSampleStream(false);
                    }}
                    className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[#8585F7] hover:bg-[#7272E5] text-black text-xs font-bold transition-colors"
                  >
                    <Play size={14} className="fill-current" />
                    <span>Watch Official Trailer</span>
                  </button>
                )}
                <button
                  onClick={() => {
                    setUseSampleStream(true);
                    setUseTrailerStream(false);
                    setIsPlaying(true);
                  }}
                  className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[#191C22] hover:bg-[#292C35] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] transition-colors"
                >
                  <Tv size={14} />
                  <span>Preview In-App Player Engine</span>
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Custom Overlay Controls (Visible during video playback) */}
        {useSampleStream && (
          <div
            className={`absolute inset-0 flex flex-col justify-between bg-gradient-to-t from-black/85 via-transparent to-black/60 p-4 sm:p-6 transition-opacity duration-300 ${
              showControls ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
            }`}
          >
            {/* Top Bar */}
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs font-bold text-[#8585F7]">Episode {currentEpisode}</p>
                <p className="text-sm font-semibold text-white line-clamp-1">{displayTitle}</p>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setUseSampleStream(false)}
                  className="px-3 py-1 rounded-lg bg-black/60 border border-[#292C35] text-[11px] font-semibold text-[#A0A3AE] hover:text-white"
                >
                  Switch Source
                </button>
              </div>
            </div>

            {/* Center Play Button Overlay */}
            <div className="flex items-center justify-center gap-6">
              <button
                onClick={() => skipTime(-10)}
                className="flex h-11 w-11 items-center justify-center rounded-full bg-black/60 text-white hover:bg-[#8585F7] hover:text-black transition-colors"
                title="Rewind 10s"
              >
                <RotateCcw size={18} />
              </button>
              <button
                onClick={togglePlay}
                className="flex h-16 w-16 items-center justify-center rounded-full bg-[#8585F7] text-black hover:scale-105 transition-all shadow-xl"
              >
                {isPlaying ? <Pause size={28} className="fill-current" /> : <Play size={28} className="fill-current ml-1" />}
              </button>
              <button
                onClick={() => skipTime(10)}
                className="flex h-11 w-11 items-center justify-center rounded-full bg-black/60 text-white hover:bg-[#8585F7] hover:text-black transition-colors"
                title="Forward 10s"
              >
                <RotateCw size={18} />
              </button>
            </div>

            {/* Bottom Controls */}
            <div className="space-y-3">
              {/* Scrubbing Bar */}
              <div className="flex items-center gap-3">
                <span className="text-xs font-medium text-white/80 w-10 text-right">
                  {formatTime(currentTime)}
                </span>
                <input
                  type="range"
                  min="0"
                  max={duration || 100}
                  value={currentTime}
                  onChange={handleSeek}
                  className="h-1.5 flex-1 cursor-pointer appearance-none rounded-lg bg-white/20 accent-[#8585F7] transition-all hover:h-2"
                />
                <span className="text-xs font-medium text-white/60 w-10">
                  {formatTime(duration)}
                </span>
              </div>

              {/* Action Buttons Row */}
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <button onClick={togglePlay} className="text-white hover:text-[#8585F7] transition-colors">
                    {isPlaying ? <Pause size={18} /> : <Play size={18} />}
                  </button>

                  {hasNextEpisode && (
                    <button
                      onClick={() => navigate(`/watch/${anime.id}/${currentEpisode + 1}`)}
                      className="text-white hover:text-[#8585F7] transition-colors"
                      title="Next Episode"
                    >
                      <SkipForward size={18} />
                    </button>
                  )}

                  {/* Volume Control */}
                  <div className="flex items-center gap-2 group/vol">
                    <button onClick={toggleMute} className="text-white hover:text-[#8585F7] transition-colors">
                      {isMuted || volume === 0 ? <VolumeX size={18} /> : <Volume2 size={18} />}
                    </button>
                    <input
                      type="range"
                      min="0"
                      max="1"
                      step="0.05"
                      value={isMuted ? 0 : volume}
                      onChange={handleVolumeChange}
                      className="w-16 h-1 cursor-pointer appearance-none rounded bg-white/30 accent-[#8585F7]"
                    />
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  {/* Speed Selector */}
                  <div className="relative">
                    <button
                      onClick={() => setShowSpeedMenu(!showSpeedMenu)}
                      className="px-2.5 py-1 rounded-lg bg-black/50 border border-white/20 text-xs font-semibold text-white hover:border-[#8585F7]"
                    >
                      {playbackSpeed}x
                    </button>
                    {showSpeedMenu && (
                      <div className="absolute bottom-9 right-0 flex flex-col rounded-xl bg-[#191C22] border border-[#292C35] p-1.5 shadow-xl">
                        {[0.5, 0.75, 1, 1.25, 1.5, 2].map((s) => (
                          <button
                            key={s}
                            onClick={() => handleSpeedSelect(s)}
                            className={`px-3 py-1 rounded-lg text-xs font-semibold text-left transition-colors ${
                              playbackSpeed === s
                                ? 'bg-[#8585F7] text-black'
                                : 'text-[#F5F5F7] hover:bg-[#292C35]'
                            }`}
                          >
                            {s}x
                          </button>
                        ))}
                      </div>
                    )}
                  </div>

                  {/* Fullscreen Toggle */}
                  <button onClick={toggleFullscreen} className="text-white hover:text-[#8585F7] transition-colors">
                    {isFullscreen ? <Minimize size={18} /> : <Maximize size={18} />}
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Episode Selection Grid */}
      <div className="space-y-4 rounded-3xl bg-[#111318] border border-[#292C35] p-6">
        <div className="flex items-center justify-between border-b border-[#292C35] pb-4">
          <div>
            <h2 className="text-lg font-bold text-[#F5F5F7]">Episodes</h2>
            <p className="text-xs text-[#A0A3AE]">Select an episode to begin playback</p>
          </div>
          <span className="text-xs font-bold text-[#8585F7]">
            {totalEpisodes} Episodes Available
          </span>
        </div>

        <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 gap-2.5 max-h-72 overflow-y-auto pr-1">
          {Array.from({ length: totalEpisodes }, (_, i) => i + 1).map((ep) => {
            const isCurrent = ep === currentEpisode;
            return (
              <button
                key={ep}
                onClick={() => {
                  navigate(`/watch/${anime.id}/${ep}`);
                  setCurrentTime(0);
                  setIsPlaying(true);
                }}
                className={`flex flex-col items-center justify-center p-3 rounded-xl border text-xs font-bold transition-all ${
                  isCurrent
                    ? 'bg-[#8585F7] text-black border-[#8585F7] shadow-lg scale-105'
                    : 'bg-[#191C22] text-[#F5F5F7] border-[#292C35] hover:border-[#8585F7] hover:text-[#8585F7]'
                }`}
              >
                <span>EP {ep}</span>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default WatchPage;
