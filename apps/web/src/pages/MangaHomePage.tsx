import React from 'react';
import {
  useTrendingManga,
  usePopularManga,
  useTopRatedManga,
} from '../hooks/useManga';
import { MediaCarousel } from '../components/ui/MediaCarousel';
import { HeroSection } from '../components/ui/HeroSection';
import { HeroSkeleton, MediaCarouselSkeleton } from '../components/ui/Skeleton';
import { ErrorState } from '../components/ui/ErrorState';
import { BookOpen, Sparkles, Flame, Star, Compass } from 'lucide-react';
import { Link } from 'react-router-dom';

export const MangaHomePage: React.FC = () => {
  const {
    data: trendingData,
    isLoading: trendingLoading,
    isError: trendingError,
    refetch: refetchTrending,
  } = useTrendingManga(1, 20);

  const {
    data: popularData,
    isLoading: popularLoading,
  } = usePopularManga(1, 20);

  const {
    data: topRatedData,
    isLoading: topRatedLoading,
  } = useTopRatedManga(1, 20);

  if (trendingError) {
    return (
      <ErrorState
        title="Could not connect to Manga Catalog"
        message="Unable to retrieve manga listings. Please check your network connection."
        onRetry={() => refetchTrending()}
      />
    );
  }

  // Filter manhwa / webtoons from popular/trending
  const manhwaItems = (popularData?.items || []).filter(
    (item) => item.countryOfOrigin === 'KR' || item.format === 'MANGA'
  );

  return (
    <div className="space-y-10 pb-12">
      {/* Category Pills Bar */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
        <Link
          to="/explore?type=manga"
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#8585F7] text-black text-xs font-bold shadow-md shrink-0"
        >
          <BookOpen size={14} />
          <span>All Manga</span>
        </Link>
        <Link
          to="/explore?type=manga&format=MANGA"
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#111318] hover:bg-[#191C22] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] shrink-0 transition-colors"
        >
          <Sparkles size={14} className="text-[#8585F7]" />
          <span>Japanese Manga</span>
        </Link>
        <Link
          to="/explore?type=manga&country=KR"
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#111318] hover:bg-[#191C22] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] shrink-0 transition-colors"
        >
          <Flame size={14} className="text-[#E64B55]" />
          <span>Korean Manhwa & Webtoons</span>
        </Link>
        <Link
          to="/explore?type=manga&format=NOVEL"
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#111318] hover:bg-[#191C22] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] shrink-0 transition-colors"
        >
          <Star size={14} className="text-[#AAA7FF]" />
          <span>Light Novels</span>
        </Link>
      </div>

      {/* Hero Section */}
      {trendingLoading || !trendingData?.items ? (
        <HeroSkeleton />
      ) : (
        <HeroSection items={trendingData.items} />
      )}

      {/* Trending Manga Carousel */}
      {trendingLoading || !trendingData?.items ? (
        <MediaCarouselSkeleton />
      ) : (
        <MediaCarousel
          title="Trending Manga & Manhwa"
          subtitle="The most engaged series across global readership"
          items={trendingData.items}
          badge="Popular"
          viewAllLink="/explore?type=manga&sort=TRENDING_DESC"
        />
      )}

      {/* Popular Webtoons & Manhwa */}
      {popularLoading || !popularData?.items ? (
        <MediaCarouselSkeleton />
      ) : (
        <MediaCarousel
          title="Popular Serializations"
          subtitle="Top ranked long-running and ongoing releases"
          items={popularData.items}
          badge="Top Hits"
          viewAllLink="/explore?type=manga&sort=POPULARITY_DESC"
        />
      )}

      {/* All-time Top Rated Manga */}
      {topRatedLoading || !topRatedData?.items ? (
        <MediaCarouselSkeleton />
      ) : (
        <MediaCarousel
          title="Highest Rated Masterpieces"
          subtitle="Critically acclaimed manga, manhwa, and graphic novels"
          items={topRatedData.items}
          badge="Must Read"
          viewAllLink="/explore?type=manga&sort=SCORE_DESC"
        />
      )}
    </div>
  );
};

export default MangaHomePage;
