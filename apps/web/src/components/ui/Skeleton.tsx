import React from 'react';

interface SkeletonProps {
  className?: string;
}

export const Skeleton: React.FC<SkeletonProps> = ({ className = '' }) => {
  return (
    <div
      className={`animate-pulse rounded-lg bg-[#191C22]/80 border border-[#292C35]/50 ${className}`}
    />
  );
};

export const AnimeCardSkeleton: React.FC = () => {
  return (
    <div className="flex flex-col gap-2 w-full">
      {/* 2:3 Poster aspect ratio */}
      <div className="relative aspect-[2/3] w-full rounded-xl overflow-hidden bg-[#111318] border border-[#292C35]/60 animate-pulse">
        <div className="absolute top-2 right-2 h-5 w-12 rounded-md bg-[#191C22]" />
        <div className="absolute top-2 left-2 h-5 w-10 rounded-md bg-[#191C22]" />
      </div>
      <div className="space-y-1.5 px-0.5">
        <div className="h-4 w-4/5 rounded bg-[#191C22] animate-pulse" />
        <div className="h-3 w-1/2 rounded bg-[#191C22]/60 animate-pulse" />
      </div>
    </div>
  );
};

export const MediaCarouselSkeleton: React.FC<{ count?: number }> = ({ count = 6 }) => {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="h-7 w-48 rounded-md bg-[#191C22] animate-pulse" />
        <div className="h-5 w-16 rounded-md bg-[#191C22] animate-pulse" />
      </div>
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
        {Array.from({ length: count }).map((_, i) => (
          <AnimeCardSkeleton key={i} />
        ))}
      </div>
    </div>
  );
};

export const HeroSkeleton: React.FC = () => {
  return (
    <div className="relative w-full h-[480px] md:h-[560px] rounded-3xl overflow-hidden bg-[#111318] border border-[#292C35]/60 animate-pulse p-6 md:p-12 flex flex-col justify-end">
      <div className="max-w-2xl space-y-4">
        <div className="flex gap-2">
          <div className="h-6 w-16 rounded-full bg-[#191C22]" />
          <div className="h-6 w-24 rounded-full bg-[#191C22]" />
        </div>
        <div className="h-10 md:h-14 w-3/4 rounded-xl bg-[#191C22]" />
        <div className="h-16 w-full rounded-lg bg-[#191C22]/80" />
        <div className="flex gap-3 pt-2">
          <div className="h-11 w-32 rounded-xl bg-[#191C22]" />
          <div className="h-11 w-36 rounded-xl bg-[#191C22]" />
        </div>
      </div>
    </div>
  );
};

export const DetailSkeleton: React.FC = () => {
  return (
    <div className="space-y-8 animate-pulse">
      <div className="h-72 md:h-96 w-full rounded-2xl bg-[#111318]" />
      <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
        <div className="aspect-[2/3] rounded-xl bg-[#191C22]" />
        <div className="md:col-span-3 space-y-4">
          <div className="h-8 w-2/3 rounded-lg bg-[#191C22]" />
          <div className="h-4 w-1/3 rounded bg-[#191C22]/60" />
          <div className="h-28 w-full rounded-lg bg-[#191C22]/40" />
        </div>
      </div>
    </div>
  );
};
