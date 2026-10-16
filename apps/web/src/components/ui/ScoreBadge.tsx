import React from 'react';
import { Star } from 'lucide-react';

interface ScoreBadgeProps {
  score?: number | null;
  size?: 'sm' | 'md' | 'lg';
  showStar?: boolean;
  className?: string;
}

export const ScoreBadge: React.FC<ScoreBadgeProps> = ({
  score,
  size = 'md',
  showStar = true,
  className = '',
}) => {
  if (score === undefined || score === null || score <= 0) {
    return null;
  }

  // Format score to out of 10 or percentage
  const displayScore = (score / 10).toFixed(1);

  // Dynamic color matching
  let badgeColor = 'text-[#4ADE80] border-[#4ADE80]/30 bg-[#4ADE80]/10'; // high > 7.5
  if (score < 60) {
    badgeColor = 'text-[#EF4444] border-[#EF4444]/30 bg-[#EF4444]/10'; // low < 6.0
  } else if (score < 75) {
    badgeColor = 'text-[#FBBF24] border-[#FBBF24]/30 bg-[#FBBF24]/10'; // mid 6.0 - 7.4
  }

  const sizeClasses = {
    sm: 'text-xs px-1.5 py-0.5 gap-1',
    md: 'text-xs px-2 py-0.5 gap-1.5 font-medium',
    lg: 'text-sm px-2.5 py-1 gap-1.5 font-semibold',
  };

  const starSizes = {
    sm: 10,
    md: 12,
    lg: 14,
  };

  return (
    <div
      className={`inline-flex items-center rounded-md border backdrop-blur-md ${badgeColor} ${sizeClasses[size]} ${className}`}
      title={`Community Score: ${score}%`}
    >
      {showStar && <Star size={starSizes[size]} className="fill-current" />}
      <span>{displayScore}</span>
    </div>
  );
};
