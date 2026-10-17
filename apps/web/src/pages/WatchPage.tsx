import React, { useState, useRef, useEffect, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Hls from 'hls.js';
import { useAnimeDetails } from '../hooks/useAnime';
import { useLibrary } from '../hooks/useLibrary';
import { ErrorState } from '../components/ui/ErrorState';
import { ScoreBadge } from '../components/ui/ScoreBadge';
import { resolveStreamSources } from '../lib/stream';
import { StreamSource, AudioType, SupportedAudioLanguage } from '@sahasra/shared-types';
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
  AlertTriangle,
  RefreshCw,
  Sliders,
  Check,
  Languages,
  Subtitles,
  Gauge,
  Layers,
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
  const hlsRef = useRef<Hls | null>(null);
  const stallTimerRef = useRef<NodeJS.Timeout | null>(null);
  const controlsTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Streaming & Sources State
  const [activeSource, setActiveSource] = useState<StreamSource | null>(null);
  const [fallbackQueue, setFallbackQueue] = useState<StreamSource[]>([]);
  const [allSources, setAllSources] = useState<StreamSource[]>([]);
  const [isResolvingSources, setIsResolvingSources] = useState<boolean>(true);
  const [failoverToast, setFailoverToast] = useState<string | null>(null);

  // User Playback Preferences
  const [preferredLang, setPreferredLang] = useState<'en' | 'hi' | 'ja'>(() => {
    return (localStorage.getItem('sahasra_pref_language') as 'en' | 'hi' | 'ja') || 'en';
  });
  const [preferredAudioType, setPreferredAudioType] = useState<AudioType>(() => {
    return (localStorage.getItem('sahasra_pref_audio_type') as AudioType) || 'dub';
  });

  // Video state
  const [isPlaying, setIsPlaying] = useState<boolean>(false);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [duration, setDuration] = useState<number>(0);
  const [volume, setVolume] = useState<number>(1);
  const [isMuted, setIsMuted] = useState<boolean>(false);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);
  const [playbackSpeed, setPlaybackSpeed] = useState<number>(1);
  const [showControls, setShowControls] = useState<boolean>(true);
  const [showEpisodeDrawer, setShowEpisodeDrawer] = useState<boolean>(false);
  const [playbackError, setPlaybackError] = useState<string | null>(null);
  const [isBuffering, setIsBuffering] = useState<boolean>(false);

  // Menus
  const [showSpeedMenu, setShowSpeedMenu] = useState<boolean>(false);
  const [showQualityMenu, setShowQualityMenu] = useState<boolean>(false);
  const [showAudioMenu, setShowAudioMenu] = useState<boolean>(false);
  const [showSourceMenu, setShowSourceMenu] = useState<boolean>(false);
  const [subtitlesEnabled, setSubtitlesEnabled] = useState<boolean>(true);
  const [currentQuality, setCurrentQuality] = useState<string>('Auto');

  // Double-tap ripple animation state
  const [doubleTapFeedback, setDoubleTapFeedback] = useState<'forward' | 'rewind' | null>(null);
  const lastTapRef = useRef<{ time: number; x: number }>({ time: 0, x: 0 });

  const storageKey = `sahasra_progress_${animeId}_ep_${currentEpisode}`;

  // 1. One-Click Streaming Resolution Pipeline
  const resolveAndStartStream = useCallback(async (lang = preferredLang, audioType = preferredAudioType) => {
    if (!anime) return;
    setIsResolvingSources(true);
    setPlaybackError(null);

    const title = anime.title.userPreferred || anime.title.english || anime.title.romaji || 'Anime';
    try {
      const result = await resolveStreamSources({
        animeTitle: title,
        episodeNumber: currentEpisode,
        animeId: anime.id,
        preferredLanguage: lang,
        preferredAudioType: audioType,
      });

      setActiveSource(result.primary);
      setFallbackQueue(result.fallbackQueue);
      setAllSources(result.allWorkingSources);
    } catch (err: any) {
      setPlaybackError(err.message || 'Failed to resolve streaming sources.');
    } finally {
      setIsResolvingSources(false);
    }
  }, [anime, currentEpisode, preferredLang, preferredAudioType]);

  // Initial resolve on anime load or episode change
  useEffect(() => {
    if (anime) {
      resolveAndStartStream();
    }
  }, [anime, currentEpisode, resolveAndStartStream]);

  // 2. Invisible Auto-Failover to next source in fallback queue
  const triggerAutoFailover = useCallback(() => {
    if (fallbackQueue.length === 0) {
      setPlaybackError('All streaming sources for this episode are currently unavailable.');
      return;
    }

    const nextSource = fallbackQueue[0];
    const remaining = fallbackQueue.slice(1);

    const preservedPosition = videoRef.current ? videoRef.current.currentTime : currentTime;

    setActiveSource(nextSource);
    setFallbackQueue(remaining);

    // Show non-intrusive alert
    setFailoverToast(`Switched to backup: ${nextSource.providerName} (${nextSource.ttfbMs ?? 50}ms)`);
    setTimeout(() => setFailoverToast(null), 4000);

    // Restore playback position on new stream
    setTimeout(() => {
      if (videoRef.current && preservedPosition > 0) {
        videoRef.current.currentTime = preservedPosition;
        videoRef.current.play().then(() => setIsPlaying(true)).catch(() => {});
      }
    }, 400);
  }, [fallbackQueue, currentTime]);

  // 3. Initialize HLS or HTML5 Video on activeSource change
  useEffect(() => {
    const video = videoRef.current;
    if (!video || !activeSource) return;

    setPlaybackError(null);
    setIsBuffering(true);

    const url = activeSource.url;
    const isHls = activeSource.isHls || url.includes('.m3u8');

    if (isHls && Hls.isSupported()) {
      if (hlsRef.current) {
        hlsRef.current.destroy();
      }

      const hls = new Hls({
        enableWorker: true,
        lowLatencyMode: true,
        backBufferLength: 90,
      });
      hlsRef.current = hls;

      hls.loadSource(url);
      hls.attachMedia(video);

      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        setIsBuffering(false);

        // Restore saved progress
        try {
          const saved = localStorage.getItem(storageKey);
          if (saved) {
            const parsed: WatchProgress = JSON.parse(saved);
            if (parsed.currentTime > 5 && parsed.duration > 0 && parsed.currentTime < parsed.duration - 10) {
              video.currentTime = parsed.currentTime;
            }
          }
        } catch {
          // Ignore
        }

        video.play().then(() => setIsPlaying(true)).catch(() => setIsPlaying(false));
      });

      hls.on(Hls.Events.ERROR, (_, errorData) => {
        if (errorData.fatal) {
          switch (errorData.type) {
            case Hls.ErrorTypes.NETWORK_ERROR:
              hls.startLoad();
              break;
            case Hls.ErrorTypes.MEDIA_ERROR:
              hls.recoverMediaError();
              break;
            default:
              hls.destroy();
              // Trigger auto-failover seamlessly
              triggerAutoFailover();
              break;
          }
        }
      });
    } else {
      // Native Safari HLS or direct MP4
      video.src = url;
      video.play().then(() => setIsPlaying(true)).catch(() => setIsPlaying(false));
    }

    return () => {
      if (hlsRef.current) {
        hlsRef.current.destroy();
        hlsRef.current = null;
      }
    };
  }, [activeSource, storageKey, triggerAutoFailover]);

  // Buffer stall detection (auto failover if stalled > 3.5s)
  useEffect(() => {
    if (isBuffering && isPlaying) {
      stallTimerRef.current = setTimeout(() => {
        if (isBuffering && isPlaying && fallbackQueue.length > 0) {
          triggerAutoFailover();
        }
      }, 3500);
    } else if (stallTimerRef.current) {
      clearTimeout(stallTimerRef.current);
      stallTimerRef.current = null;
    }

    return () => {
      if (stallTimerRef.current) clearTimeout(stallTimerRef.current);
    };
  }, [isBuffering, isPlaying, fallbackQueue, triggerAutoFailover]);

  // Progress Saving
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
      // Ignore
    }

    if (duration > 0 && currentTime / duration > 0.8) {
      const entry = getEntry(animeId);
      if (entry && entry.progress < currentEpisode) {
        updateProgress(animeId, currentEpisode);
      }
    }
  }, [currentTime, duration, animeId, currentEpisode, storageKey, getEntry, updateProgress]);

  // Controls auto-hide
  const handleMouseMove = () => {
    setShowControls(true);
    if (controlsTimeoutRef.current) clearTimeout(controlsTimeoutRef.current);
    if (isPlaying) {
      controlsTimeoutRef.current = setTimeout(() => setShowControls(false), 3500);
    }
  };

  const togglePlay = () => {
    if (!videoRef.current) return;
    if (isPlaying) {
      videoRef.current.pause();
      setIsPlaying(false);
    } else {
      videoRef.current.play().then(() => setIsPlaying(true)).catch(() => {});
    }
  };

  const skipTime = (seconds: number) => {
    if (!videoRef.current) return;
    const newTime = Math.min(Math.max(videoRef.current.currentTime + seconds, 0), duration);
    videoRef.current.currentTime = newTime;
    setCurrentTime(newTime);
  };

  // Double-tap 10s skip handler for player container
  const handleContainerClick = (e: React.MouseEvent<HTMLDivElement>) => {
    const now = Date.now();
    const rect = e.currentTarget.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const isLeftSide = clickX < rect.width / 2;

    if (now - lastTapRef.current.time < 300) {
      // Double click detected!
      if (isLeftSide) {
        skipTime(-10);
        setDoubleTapFeedback('rewind');
      } else {
        skipTime(10);
        setDoubleTapFeedback('forward');
      }
      setTimeout(() => setDoubleTapFeedback(null), 700);
    } else {
      // Single click: toggle controls
      handleMouseMove();
    }
    lastTapRef.current = { time: now, x: clickX };
  };

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    const target = parseFloat(e.target.value);
    if (videoRef.current) {
      videoRef.current.currentTime = target;
      setCurrentTime(target);
    }
  };

  const toggleMute = () => {
    if (!videoRef.current) return;
    const next = !isMuted;
    videoRef.current.muted = next;
    setIsMuted(next);
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
    if (videoRef.current) videoRef.current.playbackRate = speed;
    setShowSpeedMenu(false);
  };

  const handleLanguageSelect = (lang: 'en' | 'hi' | 'ja', type: AudioType) => {
    setPreferredLang(lang);
    setPreferredAudioType(type);
    localStorage.setItem('sahasra_pref_language', lang);
    localStorage.setItem('sahasra_pref_audio_type', type);
    setShowAudioMenu(false);
    resolveAndStartStream(lang, type);
  };

  const handleManualSourceSelect = (source: StreamSource) => {
    const preservedPosition = videoRef.current ? videoRef.current.currentTime : currentTime;
    setActiveSource(source);
    setShowSourceMenu(false);
    setTimeout(() => {
      if (videoRef.current && preservedPosition > 0) {
        videoRef.current.currentTime = preservedPosition;
        videoRef.current.play().then(() => setIsPlaying(true)).catch(() => {});
      }
    }, 400);
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

      {/* Failover Toast Notification */}
      {failoverToast && (
        <div className="fixed top-16 right-6 z-50 flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[#111318]/95 border border-[#8585F7] text-[#F5F5F7] text-xs font-semibold shadow-2xl backdrop-blur-md animate-fade-in">
          <Sparkles size={14} className="text-[#8585F7]" />
          <span>{failoverToast}</span>
        </div>
      )}

      {/* Main Video Player Container */}
      <div
        ref={playerContainerRef}
        onClick={handleContainerClick}
        className="relative aspect-video w-full overflow-hidden rounded-3xl border border-[#292C35] bg-black shadow-2xl group select-none"
      >
        {isResolvingSources ? (
          <div className="flex h-full w-full flex-col items-center justify-center gap-3 bg-[#08090B]">
            <div className="h-12 w-12 animate-spin rounded-full border-4 border-[#8585F7] border-t-transparent" />
            <p className="text-sm font-semibold text-[#A0A3AE]">Aggregating best streams & testing latency...</p>
          </div>
        ) : activeSource ? (
          <>
            <video
              ref={videoRef}
              className="h-full w-full object-contain cursor-pointer"
              onClick={togglePlay}
              onTimeUpdate={() => {
                if (videoRef.current) setCurrentTime(videoRef.current.currentTime);
              }}
              onLoadedMetadata={() => {
                if (videoRef.current) {
                  setDuration(videoRef.current.duration);
                  setIsBuffering(false);
                }
              }}
              onWaiting={() => setIsBuffering(true)}
              onPlaying={() => setIsBuffering(false)}
              onError={() => triggerAutoFailover()}
              onEnded={() => {
                setIsPlaying(false);
                if (hasNextEpisode) {
                  navigate(`/watch/${anime.id}/${currentEpisode + 1}`);
                }
              }}
            />

            {/* Double-Tap 10s Feedback Animation */}
            {doubleTapFeedback && (
              <div className={`absolute top-1/2 -translate-y-1/2 flex items-center justify-center pointer-events-none z-20 ${
                doubleTapFeedback === 'rewind' ? 'left-12' : 'right-12'
              }`}>
                <div className="flex flex-col items-center justify-center h-20 w-20 rounded-full bg-black/70 backdrop-blur-md text-white border border-white/20 animate-ping">
                  {doubleTapFeedback === 'rewind' ? <RotateCcw size={32} /> : <RotateCw size={32} />}
                  <span className="text-[10px] font-bold mt-1">10s</span>
                </div>
              </div>
            )}

            {/* In-Player Error Recovery Banner */}
            {playbackError && (
              <div className="absolute inset-0 flex flex-col items-center justify-center p-6 text-center bg-black/90 backdrop-blur-md z-30">
                <div className="max-w-md space-y-4">
                  <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-[#E64B55]/20 text-[#E64B55] border border-[#E64B55]/40">
                    <AlertTriangle size={28} />
                  </div>
                  <div className="space-y-1">
                    <h3 className="text-lg font-bold text-[#F5F5F7]">Stream Interrupted</h3>
                    <p className="text-xs text-[#A0A3AE]">{playbackError}</p>
                  </div>
                  <div className="flex flex-wrap justify-center gap-3 pt-2">
                    <button
                      onClick={() => resolveAndStartStream()}
                      className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#8585F7] hover:bg-[#7272E5] text-black text-xs font-bold transition-colors"
                    >
                      <RefreshCw size={14} />
                      <span>Retry Fastest Stream</span>
                    </button>
                    {fallbackQueue.length > 0 && (
                      <button
                        onClick={triggerAutoFailover}
                        className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#191C22] hover:bg-[#292C35] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] transition-colors"
                      >
                        <span>Try Next Backup ({fallbackQueue[0].providerName})</span>
                      </button>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* Buffering Indicator */}
            {isBuffering && !playbackError && (
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none z-20">
                <div className="h-12 w-12 animate-spin rounded-full border-4 border-[#8585F7] border-t-transparent" />
              </div>
            )}
          </>
        ) : null}

        {/* Custom YouTube-Style Video Player Overlay Controls */}
        {activeSource && !playbackError && (
          <div
            className={`absolute inset-0 flex flex-col justify-between bg-gradient-to-t from-black/90 via-black/20 to-black/70 p-4 sm:p-6 transition-opacity duration-300 z-10 ${
              showControls ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
            }`}
          >
            {/* Top Overlay Bar */}
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <span className="px-2 py-0.5 rounded-md bg-[#8585F7]/20 border border-[#8585F7]/30 text-[10px] font-bold text-[#8585F7] uppercase tracking-wide">
                    {activeSource.audioLanguage.toUpperCase()} {activeSource.audioType.toUpperCase()}
                  </span>
                  <span className="text-xs font-bold text-white/90">
                    {activeSource.providerName} • {activeSource.quality}
                  </span>
                  {activeSource.ttfbMs && (
                    <span className="text-[10px] font-semibold text-emerald-400 bg-emerald-500/10 px-1.5 py-0.5 rounded">
                      {activeSource.ttfbMs}ms
                    </span>
                  )}
                </div>
                <p className="text-sm font-semibold text-white line-clamp-1 mt-0.5">{displayTitle}</p>
              </div>

              {/* Source Switcher Pill */}
              <div className="relative">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setShowSourceMenu(!showSourceMenu);
                  }}
                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-black/60 border border-[#292C35] text-xs font-semibold text-white/90 hover:border-[#8585F7] transition-colors"
                >
                  <Layers size={14} className="text-[#8585F7]" />
                  <span>Sources ({allSources.length})</span>
                </button>

                {/* Sources Dropdown */}
                {showSourceMenu && (
                  <div
                    onClick={(e) => e.stopPropagation()}
                    className="absolute right-0 top-10 w-72 rounded-2xl bg-[#111318] border border-[#292C35] p-2 shadow-2xl space-y-1 z-50 animate-fade-in"
                  >
                    <p className="text-[11px] font-bold text-[#A0A3AE] px-2 py-1 uppercase tracking-wider">
                      Speed-Ranked Sources
                    </p>
                    {allSources.map((src) => {
                      const isCurrent = src.id === activeSource.id;
                      return (
                        <button
                          key={src.id}
                          onClick={() => handleManualSourceSelect(src)}
                          className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-left text-xs transition-colors ${
                            isCurrent
                              ? 'bg-[#8585F7]/20 text-[#8585F7] font-bold'
                              : 'text-white hover:bg-[#191C22]'
                          }`}
                        >
                          <div>
                            <div className="flex items-center gap-1.5">
                              <span className="font-semibold">{src.providerName}</span>
                              <span className="text-[9px] px-1 py-0.2 rounded bg-white/10 uppercase">
                                {src.audioLanguage}
                              </span>
                            </div>
                            <span className="text-[10px] text-[#A0A3AE]">{src.quality}</span>
                          </div>
                          {src.ttfbMs && (
                            <span className="text-[10px] font-mono text-emerald-400">
                              {src.ttfbMs}ms
                            </span>
                          )}
                        </button>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>

            {/* Center Playback Controls */}
            <div className="flex items-center justify-center gap-6">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  skipTime(-10);
                }}
                className="flex h-11 w-11 items-center justify-center rounded-full bg-black/60 text-white hover:bg-[#8585F7] hover:text-black transition-colors"
                title="Rewind 10s"
              >
                <RotateCcw size={18} />
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  togglePlay();
                }}
                className="flex h-16 w-16 items-center justify-center rounded-full bg-[#8585F7] text-black hover:scale-105 transition-all shadow-xl"
              >
                {isPlaying ? <Pause size={28} className="fill-current" /> : <Play size={28} className="fill-current ml-1" />}
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  skipTime(10);
                }}
                className="flex h-11 w-11 items-center justify-center rounded-full bg-black/60 text-white hover:bg-[#8585F7] hover:text-black transition-colors"
                title="Forward 10s"
              >
                <RotateCw size={18} />
              </button>
            </div>

            {/* Bottom Controls Bar */}
            <div className="space-y-3" onClick={(e) => e.stopPropagation()}>
              {/* Scrubber Progress Slider */}
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
                  className="w-full h-1.5 bg-white/20 rounded-lg appearance-none cursor-pointer accent-[#8585F7]"
                />
                <span className="text-xs font-medium text-white/60 w-10">
                  {formatTime(duration)}
                </span>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center justify-between">
                {/* Volume & Play/Pause */}
                <div className="flex items-center gap-3">
                  <button
                    onClick={togglePlay}
                    className="text-white hover:text-[#8585F7] transition-colors"
                  >
                    {isPlaying ? <Pause size={20} /> : <Play size={20} />}
                  </button>
                  <div className="flex items-center gap-2 group/volume">
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
                      className="w-16 h-1 bg-white/20 rounded-lg appearance-none cursor-pointer accent-[#8585F7]"
                    />
                  </div>
                </div>

                {/* Right Settings Controls (Language, Subtitles, Speed, Quality, Fullscreen) */}
                <div className="flex items-center gap-3 relative">
                  {/* Language / Audio Switcher (Hindi, English, Japanese, Sub, Dub) */}
                  <div className="relative">
                    <button
                      onClick={() => setShowAudioMenu(!showAudioMenu)}
                      className={`flex items-center gap-1 px-2.5 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                        showAudioMenu
                          ? 'bg-[#8585F7] text-black border-[#8585F7]'
                          : 'bg-black/60 border-[#292C35] text-white hover:border-[#8585F7]'
                      }`}
                    >
                      <Languages size={14} />
                      <span className="uppercase">{preferredLang} • {preferredAudioType}</span>
                    </button>

                    {showAudioMenu && (
                      <div className="absolute bottom-10 right-0 w-48 rounded-2xl bg-[#111318] border border-[#292C35] p-2 shadow-2xl space-y-1 z-50">
                        <p className="text-[10px] font-bold text-[#A0A3AE] px-2 py-1 uppercase tracking-wider">
                          Audio & Dub Options
                        </p>
                        <button
                          onClick={() => handleLanguageSelect('en', 'dub')}
                          className={`w-full flex items-center justify-between px-3 py-1.5 rounded-xl text-xs ${
                            preferredLang === 'en' && preferredAudioType === 'dub'
                              ? 'bg-[#8585F7]/20 text-[#8585F7] font-bold'
                              : 'text-white hover:bg-[#191C22]'
                          }`}
                        >
                          <span>English (Dub)</span>
                          {preferredLang === 'en' && preferredAudioType === 'dub' && <Check size={12} />}
                        </button>
                        <button
                          onClick={() => handleLanguageSelect('hi', 'dub')}
                          className={`w-full flex items-center justify-between px-3 py-1.5 rounded-xl text-xs ${
                            preferredLang === 'hi'
                              ? 'bg-[#8585F7]/20 text-[#8585F7] font-bold'
                              : 'text-white hover:bg-[#191C22]'
                          }`}
                        >
                          <span>Hindi (Dub)</span>
                          {preferredLang === 'hi' && <Check size={12} />}
                        </button>
                        <button
                          onClick={() => handleLanguageSelect('ja', 'sub')}
                          className={`w-full flex items-center justify-between px-3 py-1.5 rounded-xl text-xs ${
                            preferredLang === 'ja' || preferredAudioType === 'sub'
                              ? 'bg-[#8585F7]/20 text-[#8585F7] font-bold'
                              : 'text-white hover:bg-[#191C22]'
                          }`}
                        >
                          <span>Japanese (Sub)</span>
                          {preferredAudioType === 'sub' && <Check size={12} />}
                        </button>
                      </div>
                    )}
                  </div>

                  {/* Subtitles Toggle (ON/OFF) */}
                  <button
                    onClick={() => setSubtitlesEnabled(!subtitlesEnabled)}
                    className={`flex items-center gap-1 px-2.5 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                      subtitlesEnabled
                        ? 'bg-[#8585F7]/20 border-[#8585F7] text-[#8585F7]'
                        : 'bg-black/60 border-[#292C35] text-white/50 hover:text-white'
                    }`}
                    title="Toggle Subtitles / Captions"
                  >
                    <Subtitles size={14} />
                    <span>CC</span>
                  </button>

                  {/* Playback Speed Switcher (0.5x - 2.0x) */}
                  <div className="relative">
                    <button
                      onClick={() => setShowSpeedMenu(!showSpeedMenu)}
                      className="flex items-center gap-1 px-2 py-1.5 rounded-lg bg-black/60 border border-[#292C35] text-xs font-semibold text-white hover:border-[#8585F7] transition-colors"
                    >
                      <Gauge size={14} />
                      <span>{playbackSpeed}x</span>
                    </button>

                    {showSpeedMenu && (
                      <div className="absolute bottom-10 right-0 w-32 rounded-2xl bg-[#111318] border border-[#292C35] p-2 shadow-2xl space-y-1 z-50">
                        <p className="text-[10px] font-bold text-[#A0A3AE] px-2 py-1 uppercase tracking-wider">
                          Speed
                        </p>
                        {[0.5, 0.75, 1, 1.25, 1.5, 2].map((s) => (
                          <button
                            key={s}
                            onClick={() => handleSpeedSelect(s)}
                            className={`w-full flex items-center justify-between px-3 py-1.5 rounded-xl text-xs ${
                              playbackSpeed === s
                                ? 'bg-[#8585F7]/20 text-[#8585F7] font-bold'
                                : 'text-white hover:bg-[#191C22]'
                            }`}
                          >
                            <span>{s}x</span>
                            {playbackSpeed === s && <Check size={12} />}
                          </button>
                        ))}
                      </div>
                    )}
                  </div>

                  {/* Resolution Quality Dropdown */}
                  <div className="relative">
                    <button
                      onClick={() => setShowQualityMenu(!showQualityMenu)}
                      className="flex items-center gap-1 px-2 py-1.5 rounded-lg bg-black/60 border border-[#292C35] text-xs font-semibold text-white hover:border-[#8585F7] transition-colors"
                    >
                      <Sliders size={14} />
                      <span>{currentQuality}</span>
                    </button>

                    {showQualityMenu && (
                      <div className="absolute bottom-10 right-0 w-36 rounded-2xl bg-[#111318] border border-[#292C35] p-2 shadow-2xl space-y-1 z-50">
                        <p className="text-[10px] font-bold text-[#A0A3AE] px-2 py-1 uppercase tracking-wider">
                          Quality
                        </p>
                        {['Auto', '1080p', '720p', '480p', '360p'].map((q) => (
                          <button
                            key={q}
                            onClick={() => {
                              setCurrentQuality(q);
                              setShowQualityMenu(false);
                            }}
                            className={`w-full flex items-center justify-between px-3 py-1.5 rounded-xl text-xs ${
                              currentQuality === q
                                ? 'bg-[#8585F7]/20 text-[#8585F7] font-bold'
                                : 'text-white hover:bg-[#191C22]'
                            }`}
                          >
                            <span>{q}</span>
                            {currentQuality === q && <Check size={12} />}
                          </button>
                        ))}
                      </div>
                    )}
                  </div>

                  {/* Fullscreen Button */}
                  <button
                    onClick={toggleFullscreen}
                    className="p-1.5 text-white hover:text-[#8585F7] transition-colors"
                  >
                    {isFullscreen ? <Minimize size={18} /> : <Maximize size={18} />}
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Episode Selector Drawer / Modal */}
      {showEpisodeDrawer && (
        <div className="rounded-3xl bg-[#111318] border border-[#292C35] p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-[#F5F5F7]">Episodes</h3>
            <span className="text-xs text-[#A0A3AE] font-semibold">{totalEpisodes} Total</span>
          </div>

          <div className="grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 lg:grid-cols-12 gap-2">
            {Array.from({ length: totalEpisodes }).map((_, idx) => {
              const epNum = idx + 1;
              const isCurrent = epNum === currentEpisode;
              return (
                <button
                  key={epNum}
                  onClick={() => {
                    navigate(`/watch/${anime.id}/${epNum}`);
                    setShowEpisodeDrawer(false);
                  }}
                  className={`h-11 rounded-xl font-bold text-xs transition-all ${
                    isCurrent
                      ? 'bg-[#8585F7] text-black shadow-lg shadow-[#8585F7]/20'
                      : 'bg-[#191C22] text-[#A0A3AE] hover:text-[#F5F5F7] hover:border-[#8585F7] border border-[#292C35]'
                  }`}
                >
                  {epNum}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
