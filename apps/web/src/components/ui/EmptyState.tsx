import React from 'react';
import { Film } from 'lucide-react';
import { Link } from 'react-router-dom';

interface EmptyStateProps {
  title?: string;
  message?: string;
  actionLabel?: string;
  actionLink?: string;
  icon?: React.ReactNode;
  className?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title = 'No titles found',
  message = 'Try adjusting your filters or search keywords to find what you are looking for.',
  actionLabel,
  actionLink,
  icon,
  className = '',
}) => {
  return (
    <div
      className={`flex flex-col items-center justify-center p-8 sm:p-14 text-center rounded-2xl bg-[#111318]/60 border border-[#292C35]/60 max-w-md mx-auto my-8 ${className}`}
    >
      <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#191C22] text-[#A0A3AE] border border-[#292C35] mb-4">
        {icon || <Film size={26} />}
      </div>
      <h3 className="text-lg font-bold text-[#F5F5F7] mb-2">{title}</h3>
      <p className="text-sm text-[#A0A3AE] mb-6 max-w-xs">{message}</p>
      {actionLabel && actionLink && (
        <Link
          to={actionLink}
          className="inline-flex items-center gap-2 rounded-xl bg-[#8585F7] hover:bg-[#7272E5] px-4 py-2 text-sm font-semibold text-white transition-colors"
        >
          {actionLabel}
        </Link>
      )}
    </div>
  );
};
