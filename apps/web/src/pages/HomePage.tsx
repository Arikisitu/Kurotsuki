import React from 'react';
import {
  useTrendingAnime,
  useSeasonalAnime,
  usePopularAnime,
  useUpcomingAnime,
  useTopRatedAnime,
  getCurrentSeason,
} from '../hooks/useAnime';
import { useTrendingManga } from '../hooks/useManga';
import { HeroSection } from '../components/ui/HeroSection';
import { MediaCarousel } from '../components/ui/MediaCarousel';
import { HeroSkeleton, MediaCarouselSkeleton } from '../components/ui/Skeleton';
import { ErrorState } from '../components/ui/ErrorState';

export const HomePage: React.FC = () => {
  const { season, year } = getCurrentSeason();

  // Queries
  const {
    data: trendingData,
    isLoading: trendingLoading,
    isError: trendingError,
    refetch: refetchTrending,
  } = useTrendingAnime(1, 20);

  const {
    data: seasonalData,
    isLoading: seasonalLoading,
  } = useSeasonalAnime(season, year, 1, 20);

  const {
    data: popularData,
    isLoading: popularLoading,
  } = usePopularAnime(1, 20);

  const {
    data: upcomingData,
    isLoading: upcomingLoading,
  } = useUpcomingAnime(1, 20);

  const {
    data: topRatedData,
    isLoading: topRatedLoading,
  } = useTopRatedAnime(1, 20);

  const {
    data: trendingMangaData,
    isLoading: mangaLoading,
  } = useTrendingManga(1, 20);

  if (trendingError) {
    return (
      <ErrorState
        title="Could not connect to AniList"
        message="Unable to retrieve trending anime catalog. Please check your internet connection."
        onRetry={() => refetchTrending()}
      />
    );
  }

  return (
    <div className="space-y-10 pb-8">
      {/* Hero Section */}
      {trendingLoading || !trendingData?.items ? (
        <HeroSkeleton />
      ) : (
        <HeroSection items={trendingData.items} />
      )}

      {/* Trending Anime Carousel */}
      {trendingLoading || !trendingData?.items ? (
        <MediaCarouselSkeleton />
      ) : (
        <MediaCarousel
          title="Trending Now"
          subtitle="What the anime community is watching right this second"
          items={trendingData.items}
          badge="Live"
          viewAllLink="/explore?sort=TRENDING_DESC"
        />
      )}

      {/* Current Season Carousel */}
      {seasonalLoading || !seasonalData?.items ? (
        <MediaCarouselSkeleton />
      ) : (
        <MediaCarousel
          title={`${season} ${year} Highlights`}
          subtitle="The standout weekly premieres and broadcast series"
          items={seasonalData.items}
          badge="Current Season"
          viewAllLink={`/explore?season=${season}&year=${year}`}
        />
      )}

      {/* All-time Popular Carousel */}
      {popularLoading || !popularData?.items ? (
        <MediaCarouselSkeleton />
      ) : (
        <MediaCarousel
          title="All-Time Classics & Popular"
          subtitle="The most-watched masterpieces in anime history"
          items={popularData.items}
          viewAllLink="/explore?sort=POPULARITY_DESC"
        />
      )}

      {/* Trending Manga Section */}
      {mangaLoading || !trendingMangaData?.items ? (
        <MediaCarouselSkeleton />
      ) : (
        <MediaCarousel
          title="Popular & Trending Manga"
          subtitle="Top manga series and light novels readers are following"
          items={trendingMangaData.items}
          type="MANGA"
          badge="Manga"
          viewAllLink="/explore?type=manga"
        />
      )}

      {/* Upcoming Most Anticipated */}
      {upcomingLoading || !upcomingData?.items ? (
        <MediaCarouselSkeleton />
      ) : (
        <MediaCarousel
          title="Most Anticipated Releases"
          subtitle="Upcoming series, movies, and seasons coming soon"
          items={upcomingData.items}
          badge="Upcoming"
          viewAllLink="/explore?status=NOT_YET_RELEASED"
        />
      )}

      {/* Top Rated Anime */}
      {topRatedLoading || !topRatedData?.items ? (
        <MediaCarouselSkeleton />
      ) : (
        <MediaCarousel
          title="Highest Community Rated"
          subtitle="Critically acclaimed titles with superior ratings"
          items={topRatedData.items}
          viewAllLink="/explore?sort=SCORE_DESC"
        />
      )}
    </div>
  );
};
