import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';

interface ErrorStateProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
  className?: string;
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  title = 'Something went wrong',
  message = 'Failed to load content from AniList. Please check your internet connection and try again.',
  onRetry,
  className = '',
}) => {
  return (
    <div
      className={`flex flex-col items-center justify-center p-8 sm:p-12 text-center rounded-2xl bg-[#111318] border border-[#292C35] max-w-lg mx-auto my-6 ${className}`}
    >
      <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#EF4444]/10 text-[#EF4444] border border-[#EF4444]/20 mb-4">
        <AlertCircle size={28} />
      </div>
      <h3 className="text-lg font-bold text-[#F5F5F7] mb-2">{title}</h3>
      <p className="text-sm text-[#A0A3AE] mb-6 max-w-sm">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="inline-flex items-center gap-2 rounded-xl bg-[#191C22] hover:bg-[#292C35] border border-[#292C35] px-4 py-2 text-sm font-semibold text-[#F5F5F7] transition-colors"
        >
          <RefreshCw size={15} />
          <span>Try Again</span>
        </button>
      )}
    </div>
  );
};
