import React from 'react';
import { Link } from 'react-router-dom';

interface GenreBadgeProps {
  genre: string;
  size?: 'sm' | 'md';
  clickable?: boolean;
  className?: string;
}

export const GenreBadge: React.FC<GenreBadgeProps> = ({
  genre,
  size = 'sm',
  clickable = false,
  className = '',
}) => {
  const sizeClasses = size === 'sm' ? 'text-[11px] px-2 py-0.5' : 'text-xs px-2.5 py-1';

  const content = (
    <span
      className={`inline-flex items-center font-medium rounded-full bg-[#191C22]/80 text-[#A0A3AE] hover:text-[#F5F5F7] hover:border-[#8585F7]/40 border border-[#292C35] transition-colors duration-150 ${sizeClasses} ${className}`}
    >
      {genre}
    </span>
  );

  if (clickable) {
    return <Link to={`/explore?genre=${encodeURIComponent(genre)}`}>{content}</Link>;
  }

  return content;
};
