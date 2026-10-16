/**
 * Custom hook for local library tracking and state management
 */

import { useState, useEffect, useCallback } from 'react';
import { Anime, Manga } from '@sahasra/shared-types';

export type MediaStatusType = 'WATCHING' | 'PLANNING' | 'COMPLETED' | 'PAUSED' | 'DROPPED';

export interface LibraryEntry {
  id: number;
  mediaType: 'ANIME' | 'MANGA';
  title: string;
  coverImage: string;
  format?: string | null;
  status: MediaStatusType;
  progress: number;
  totalProgress?: number | null;
  score?: number | null;
  favorite: boolean;
  updatedAt: number;
}

const STORAGE_KEY = 'sahasra_user_library_v1';

export function useLibrary() {
  const [entries, setEntries] = useState<Record<number, LibraryEntry>>(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return stored ? JSON.parse(stored) : {};
    } catch {
      return {};
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(entries));
    } catch (e) {
      console.error('Failed to persist library:', e);
    }
  }, [entries]);

  const getEntry = useCallback(
    (id: number): LibraryEntry | undefined => {
      return entries[id];
    },
    [entries]
  );

  const setStatus = useCallback((media: Anime | Manga, status: MediaStatusType) => {
    setEntries((prev) => {
      const existing = prev[media.id];
      const totalProgress =
        media.type === 'ANIME'
          ? (media as Anime).episodes
          : (media as Manga).chapters;

      return {
        ...prev,
        [media.id]: {
          id: media.id,
          mediaType: media.type,
          title: media.title.userPreferred || media.title.english || media.title.romaji || 'Untitled',
          coverImage: media.artwork.cover.large || media.artwork.cover.medium || '',
          format: media.format,
          status,
          progress: existing ? existing.progress : 0,
          totalProgress: totalProgress || null,
          score: existing ? existing.score : null,
          favorite: existing ? existing.favorite : false,
          updatedAt: Date.now(),
        },
      };
    });
  }, []);

  const updateProgress = useCallback((id: number, progress: number) => {
    setEntries((prev) => {
      const existing = prev[id];
      if (!existing) return prev;
      return {
        ...prev,
        [id]: {
          ...existing,
          progress: Math.max(0, progress),
          status:
            existing.totalProgress && progress >= existing.totalProgress
              ? 'COMPLETED'
              : existing.status,
          updatedAt: Date.now(),
        },
      };
    });
  }, []);

  const setScore = useCallback((id: number, score: number) => {
    setEntries((prev) => {
      const existing = prev[id];
      if (!existing) return prev;
      return {
        ...prev,
        [id]: {
          ...existing,
          score,
          updatedAt: Date.now(),
        },
      };
    });
  }, []);

  const toggleFavorite = useCallback((media: Anime | Manga) => {
    setEntries((prev) => {
      const existing = prev[media.id];
      if (existing) {
        return {
          ...prev,
          [media.id]: {
            ...existing,
            favorite: !existing.favorite,
            updatedAt: Date.now(),
          },
        };
      } else {
        const totalProgress =
          media.type === 'ANIME'
            ? (media as Anime).episodes
            : (media as Manga).chapters;

        return {
          ...prev,
          [media.id]: {
            id: media.id,
            mediaType: media.type,
            title: media.title.userPreferred || media.title.english || media.title.romaji || 'Untitled',
            coverImage: media.artwork.cover.large || media.artwork.cover.medium || '',
            format: media.format,
            status: 'PLANNING',
            progress: 0,
            totalProgress: totalProgress || null,
            score: null,
            favorite: true,
            updatedAt: Date.now(),
          },
        };
      }
    });
  }, []);

  const removeEntry = useCallback((id: number) => {
    setEntries((prev) => {
      const next = { ...prev };
      delete next[id];
      return next;
    });
  }, []);

  const listEntries = Object.values(entries);

  return {
    entries,
    listEntries,
    getEntry,
    setStatus,
    updateProgress,
    setScore,
    toggleFavorite,
    removeEntry,
  };
}
