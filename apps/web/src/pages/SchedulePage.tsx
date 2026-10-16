import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAiringSchedule } from '../hooks/useAnime';
import { ScoreBadge } from '../components/ui/ScoreBadge';
import { AnimeCardSkeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { Calendar, Clock } from 'lucide-react';

const DAYS = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

export const SchedulePage: React.FC = () => {
  const today = new Date();
  const currentDayIndex = today.getDay();
  const [selectedDayIndex, setSelectedDayIndex] = useState<number>(currentDayIndex);

  // Calculate start and end unix timestamps for the week window
  const startOfWeek = new Date();
  startOfWeek.setDate(today.getDate() - today.getDay());
  startOfWeek.setHours(0, 0, 0, 0);
  const startTimestamp = Math.floor(startOfWeek.getTime() / 1000);
  const endTimestamp = startTimestamp + 7 * 86400;

  const { data: scheduleItems, isLoading, isError, refetch } = useAiringSchedule(
    startTimestamp,
    endTimestamp
  );

  // Filter items by selected day of week
  const itemsForDay = (scheduleItems || []).filter((item) => {
    const airDate = new Date(item.airingAt * 1000);
    return airDate.getDay() === selectedDayIndex;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-[#F5F5F7] tracking-tight">
          Airing Schedule
        </h1>
        <p className="text-xs sm:text-sm text-[#A0A3AE]">
          Weekly television broadcast timetable and streaming release hours
        </p>
      </div>

      {/* Days Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b border-[#292C35] no-scrollbar">
        {DAYS.map((dayName, idx) => {
          const isToday = idx === currentDayIndex;
          const isSelected = idx === selectedDayIndex;

          return (
            <button
              key={dayName}
              onClick={() => setSelectedDayIndex(idx)}
              className={`flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-semibold shrink-0 transition-colors ${
                isSelected
                  ? 'bg-[#8585F7] text-white shadow'
                  : 'bg-[#111318] text-[#A0A3AE] hover:text-[#F5F5F7] border border-[#292C35]'
              }`}
            >
              <span>{dayName}</span>
              {isToday && (
                <span className="rounded-full bg-[#4ADE80] h-1.5 w-1.5 ml-0.5" />
              )}
            </button>
          );
        })}
      </div>

      {/* Schedule Items Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="h-28 rounded-2xl bg-[#111318] animate-pulse" />
          ))}
        </div>
      ) : isError ? (
        <div className="p-8 text-center text-xs text-[#EF4444]">
          Failed to load schedule.
          <button onClick={() => refetch()} className="ml-2 underline">
            Retry
          </button>
        </div>
      ) : itemsForDay.length === 0 ? (
        <EmptyState
          title={`No releases scheduled for ${DAYS[selectedDayIndex]}`}
          message="There are no broadcasts scheduled on AniList for this day."
          actionLabel="View Today"
          actionLink="/schedule"
          icon={<Calendar size={28} className="text-[#8585F7]" />}
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {itemsForDay.map((item) => {
            const anime = item.media;
            const displayTitle =
              anime.title.userPreferred || anime.title.english || anime.title.romaji || 'Untitled';
            const airTime = new Date(item.airingAt * 1000).toLocaleTimeString([], {
              hour: '2-digit',
              minute: '2-digit',
            });
            const hasAired = item.timeUntilAiring <= 0;

            return (
              <Link
                key={item.id}
                to={`/anime/${anime.id}`}
                className="group flex gap-3 p-3 rounded-2xl bg-[#111318] border border-[#292C35] hover:border-[#8585F7]/50 transition-all hover:-translate-y-0.5"
              >
                <div className="relative aspect-[2/3] w-20 shrink-0 rounded-xl overflow-hidden bg-[#08090B]">
                  <img
                    src={anime.artwork.cover.medium || anime.artwork.cover.large || '/logo.jpg'}
                    alt={displayTitle}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                  />
                  <span className="absolute top-1 left-1 rounded bg-[#08090B]/80 px-1 py-0.5 text-[8px] font-bold text-[#8585F7]">
                    Ep {item.episode}
                  </span>
                </div>

                <div className="flex-1 min-w-0 flex flex-col justify-between py-0.5">
                  <div>
                    <p className="line-clamp-2 text-xs font-semibold text-[#F5F5F7] group-hover:text-[#8585F7] transition-colors">
                      {displayTitle}
                    </p>
                    <div className="flex items-center gap-1.5 text-[11px] text-[#A0A3AE] mt-1">
                      <span>{anime.format || 'TV'}</span>
                      {anime.studios?.[0]?.name && (
                        <>
                          <span>•</span>
                          <span className="line-clamp-1">{anime.studios[0].name}</span>
                        </>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center justify-between pt-2">
                    <div className="flex items-center gap-1 text-xs font-semibold text-[#8585F7]">
                      <Clock size={12} />
                      <span>{airTime}</span>
                    </div>

                    <span
                      className={`text-[10px] font-medium px-2 py-0.5 rounded-full ${
                        hasAired
                          ? 'bg-[#292C35] text-[#A0A3AE]'
                          : 'bg-[#4ADE80]/10 text-[#4ADE80] border border-[#4ADE80]/20'
                      }`}
                    >
                      {hasAired ? 'Aired' : 'Scheduled'}
                    </span>
                  </div>
                </div>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
};
