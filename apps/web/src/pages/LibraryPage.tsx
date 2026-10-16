import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useLibrary, MediaStatusType } from '../hooks/useLibrary';
import { EmptyState } from '../components/ui/EmptyState';
import { Bookmark, Star, Trash2, Plus, Minus, Check, Heart, Play } from 'lucide-react';

export const LibraryPage: React.FC = () => {
  const { listEntries, updateProgress, setStatus, removeEntry, toggleFavorite } = useLibrary();
  const [activeTab, setActiveTab] = useState<'ALL' | MediaStatusType | 'FAVORITES'>('ALL');
  const [mediaFilter, setMediaFilter] = useState<'ALL' | 'ANIME' | 'MANGA'>('ALL');

  const filteredEntries = listEntries.filter((entry) => {
    if (mediaFilter !== 'ALL' && entry.mediaType !== mediaFilter) return false;
    if (activeTab === 'ALL') return true;
    if (activeTab === 'FAVORITES') return entry.favorite;
    return entry.status === activeTab;
  });

  const getTabCount = (status: 'ALL' | MediaStatusType | 'FAVORITES') => {
    if (status === 'ALL') return listEntries.length;
    if (status === 'FAVORITES') return listEntries.filter((e) => e.favorite).length;
    return listEntries.filter((e) => e.status === status).length;
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-[#F5F5F7] tracking-tight">
            My Library
          </h1>
          <p className="text-xs sm:text-sm text-[#A0A3AE]">
            Track your personal watching, reading, favorites, and progress
          </p>
        </div>

        {/* Media filter switcher */}
        <div className="flex p-1 rounded-xl bg-[#111318] border border-[#292C35] self-start sm:self-auto">
          <button
            onClick={() => setMediaFilter('ALL')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              mediaFilter === 'ALL' ? 'bg-[#8585F7] text-white shadow' : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
            }`}
          >
            All ({listEntries.length})
          </button>
          <button
            onClick={() => setMediaFilter('ANIME')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              mediaFilter === 'ANIME' ? 'bg-[#8585F7] text-white shadow' : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
            }`}
          >
            Anime ({listEntries.filter((e) => e.mediaType === 'ANIME').length})
          </button>
          <button
            onClick={() => setMediaFilter('MANGA')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              mediaFilter === 'MANGA' ? 'bg-[#8585F7] text-white shadow' : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
            }`}
          >
            Manga ({listEntries.filter((e) => e.mediaType === 'MANGA').length})
          </button>
        </div>
      </div>

      {/* Status Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b border-[#292C35] no-scrollbar">
        <button
          onClick={() => setActiveTab('ALL')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold shrink-0 transition-colors ${
            activeTab === 'ALL'
              ? 'bg-[#191C22] text-[#8585F7] border border-[#292C35]'
              : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
          }`}
        >
          All Titles ({getTabCount('ALL')})
        </button>

        <button
          onClick={() => setActiveTab('WATCHING')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold shrink-0 transition-colors ${
            activeTab === 'WATCHING'
              ? 'bg-[#191C22] text-[#4ADE80] border border-[#292C35]'
              : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
          }`}
        >
          Watching / Reading ({getTabCount('WATCHING')})
        </button>

        <button
          onClick={() => setActiveTab('PLANNING')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold shrink-0 transition-colors ${
            activeTab === 'PLANNING'
              ? 'bg-[#191C22] text-[#8585F7] border border-[#292C35]'
              : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
          }`}
        >
          Planning ({getTabCount('PLANNING')})
        </button>

        <button
          onClick={() => setActiveTab('COMPLETED')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold shrink-0 transition-colors ${
            activeTab === 'COMPLETED'
              ? 'bg-[#191C22] text-[#AAA7FF] border border-[#292C35]'
              : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
          }`}
        >
          Completed ({getTabCount('COMPLETED')})
        </button>

        <button
          onClick={() => setActiveTab('FAVORITES')}
          className={`px-4 py-2 rounded-xl text-xs font-semibold shrink-0 transition-colors ${
            activeTab === 'FAVORITES'
              ? 'bg-[#191C22] text-[#E64B55] border border-[#292C35]'
              : 'text-[#A0A3AE] hover:text-[#F5F5F7]'
          }`}
        >
          Favorites ({getTabCount('FAVORITES')})
        </button>
      </div>

      {/* Library Items */}
      {filteredEntries.length === 0 ? (
        <EmptyState
          title="Your library is empty"
          message="You haven't tracked any titles under this category yet. Explore titles to add them to your collection."
          actionLabel="Discover Anime & Manga"
          actionLink="/explore"
          icon={<Bookmark size={28} className="text-[#8585F7]" />}
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filteredEntries.map((item) => (
            <div
              key={item.id}
              className="flex gap-3 p-3 rounded-2xl bg-[#111318] border border-[#292C35] hover:border-[#8585F7]/40 transition-all"
            >
              <Link
                to={item.mediaType === 'ANIME' ? `/anime/${item.id}` : `/manga/${item.id}`}
                className="relative aspect-[2/3] w-20 shrink-0 rounded-xl overflow-hidden bg-[#08090B]"
              >
                <img
                  src={item.coverImage}
                  alt={item.title}
                  className="w-full h-full object-cover"
                />
                <span className="absolute top-1 left-1 rounded bg-[#08090B]/80 px-1 py-0.5 text-[8px] font-bold text-[#8585F7]">
                  {item.mediaType}
                </span>
              </Link>

              <div className="flex-1 min-w-0 flex flex-col justify-between py-0.5">
                <div>
                  <Link
                    to={item.mediaType === 'ANIME' ? `/anime/${item.id}` : `/manga/${item.id}`}
                    className="line-clamp-1 text-sm font-semibold text-[#F5F5F7] hover:text-[#8585F7]"
                  >
                    {item.title}
                  </Link>

                  <div className="flex items-center gap-2 text-xs text-[#A0A3AE] mt-0.5">
                    <span className="capitalize">{item.status.toLowerCase()}</span>
                    <span>•</span>
                    <span>{item.format || item.mediaType}</span>
                  </div>
                </div>

                {/* Progress control */}
                <div className="flex items-center justify-between pt-2">
                  <div className="flex items-center gap-1.5 text-xs text-[#A0A3AE]">
                    <button
                      onClick={() => updateProgress(item.id, Math.max(0, item.progress - 1))}
                      className="flex h-6 w-6 items-center justify-center rounded-md bg-[#191C22] hover:bg-[#292C35] text-white"
                      title="Decrease"
                    >
                      <Minus size={12} />
                    </button>
                    <span className="font-semibold text-[#F5F5F7] px-1">
                      {item.progress} / {item.totalProgress || '∞'}
                    </span>
                    <button
                      onClick={() => updateProgress(item.id, item.progress + 1)}
                      className="flex h-6 w-6 items-center justify-center rounded-md bg-[#8585F7] hover:bg-[#7272E5] text-white"
                      title="Increment"
                    >
                      <Plus size={12} />
                    </button>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => removeEntry(item.id)}
                      className="flex h-7 w-7 items-center justify-center rounded-lg text-[#A0A3AE] hover:text-[#EF4444] hover:bg-[#EF4444]/10 transition-colors"
                      title="Remove from library"
                    >
                      <Trash2 size={13} />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
