import React from 'react';
import { Link } from 'react-router-dom';
import { Home, Compass, AlertCircle } from 'lucide-react';

export const NotFoundPage: React.FC = () => {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center px-4">
      <div className="flex h-20 w-20 items-center justify-center rounded-3xl bg-[#191C22] border border-[#292C35] text-[#8585F7] mb-6 shadow-xl shadow-[#8585F7]/10">
        <AlertCircle size={40} />
      </div>

      <h1 className="text-4xl sm:text-5xl font-black text-[#F5F5F7] tracking-tight mb-2">
        404
      </h1>
      <h2 className="text-xl font-bold text-[#AAA7FF] mb-3">
        Lost in the Multiverse
      </h2>
      <p className="text-sm text-[#A0A3AE] max-w-md mb-8">
        The page you are looking for has been relocated, cancelled, or doesn&apos;t exist in this timeline.
      </p>

      <div className="flex flex-wrap items-center justify-center gap-3">
        <Link
          to="/"
          className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#8585F7] hover:bg-[#7272E5] text-xs font-bold text-white transition-all shadow-lg shadow-[#8585F7]/20"
        >
          <Home size={15} />
          <span>Return Home</span>
        </Link>
        <Link
          to="/explore"
          className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#191C22] hover:bg-[#292C35] border border-[#292C35] text-xs font-semibold text-[#F5F5F7] transition-colors"
        >
          <Compass size={15} />
          <span>Explore Catalog</span>
        </Link>
      </div>
    </div>
  );
};
