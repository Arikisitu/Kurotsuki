import React, { useState } from 'react';
import { Settings, Shield, Sliders, Palette, Database, Check } from 'lucide-react';

export const SettingsPage: React.FC = () => {
  const [titleLanguage, setTitleLanguage] = useState<'english' | 'romaji' | 'native'>('english');
  const [safeSearch, setSafeSearch] = useState(true);
  const [autoPlayTrailers, setAutoPlayTrailers] = useState(false);
  const [savedNotice, setSavedNotice] = useState(false);

  const handleSave = () => {
    setSavedNotice(true);
    setTimeout(() => setSavedNotice(false), 2500);
  };

  const handleClearCache = () => {
    if (confirm('Clear cached media queries and local storage?')) {
      localStorage.clear();
      window.location.reload();
    }
  };

  return (
    <div className="max-w-3xl space-y-8">
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-[#F5F5F7] tracking-tight">
          Preferences & Settings
        </h1>
        <p className="text-xs sm:text-sm text-[#A0A3AE]">
          Customize title formats, player defaults, and platform options
        </p>
      </div>

      {savedNotice && (
        <div className="flex items-center gap-2 p-3 rounded-xl bg-[#4ADE80]/15 border border-[#4ADE80]/30 text-xs font-semibold text-[#4ADE80] animate-in fade-in">
          <Check size={16} />
          <span>Settings saved successfully!</span>
        </div>
      )}

      {/* Title Localization Preference */}
      <div className="p-6 rounded-2xl bg-[#111318] border border-[#292C35] space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#8585F7]/10 text-[#8585F7]">
            <Sliders size={20} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-[#F5F5F7]">Title Display Preference</h3>
            <p className="text-xs text-[#A0A3AE]">Choose how series titles are presented across the UI</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
          {[
            { id: 'english', label: 'English', desc: 'Attack on Titan' },
            { id: 'romaji', label: 'Romaji', desc: 'Shingeki no Kyojin' },
            { id: 'native', label: 'Native (Kanji)', desc: '進撃の巨人' },
          ].map((lang) => (
            <button
              key={lang.id}
              onClick={() => setTitleLanguage(lang.id as any)}
              className={`p-3.5 rounded-xl border text-left transition-all ${
                titleLanguage === lang.id
                  ? 'bg-[#191C22] border-[#8585F7] text-[#F5F5F7] shadow-md shadow-[#8585F7]/10'
                  : 'bg-[#111318] border-[#292C35] text-[#A0A3AE] hover:border-[#8585F7]/40'
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold">{lang.label}</span>
                {titleLanguage === lang.id && (
                  <Check size={14} className="text-[#8585F7]" />
                )}
              </div>
              <p className="text-[11px] text-[#A0A3AE] mt-1">{lang.desc}</p>
            </button>
          ))}
        </div>
      </div>

      {/* Content & Safe Search */}
      <div className="p-6 rounded-2xl bg-[#111318] border border-[#292C35] space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#4ADE80]/10 text-[#4ADE80]">
            <Shield size={20} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-[#F5F5F7]">Content Moderation & Safety</h3>
            <p className="text-xs text-[#A0A3AE]">Manage NSFW and adult content filtering</p>
          </div>
        </div>

        <div className="divide-y divide-[#292C35]/60 pt-2">
          <div className="flex items-center justify-between py-3">
            <div>
              <p className="text-xs font-semibold text-[#F5F5F7]">Safe Search Filter</p>
              <p className="text-[11px] text-[#A0A3AE]">Strictly hide 18+ and adult content from discovery and search</p>
            </div>
            <input
              type="checkbox"
              checked={safeSearch}
              onChange={(e) => setSafeSearch(e.target.checked)}
              className="h-4 w-4 rounded accent-[#8585F7]"
            />
          </div>

          <div className="flex items-center justify-between py-3">
            <div>
              <p className="text-xs font-semibold text-[#F5F5F7]">Auto-play Trailers</p>
              <p className="text-[11px] text-[#A0A3AE]">Play YouTube promotional videos on detail pages</p>
            </div>
            <input
              type="checkbox"
              checked={autoPlayTrailers}
              onChange={(e) => setAutoPlayTrailers(e.target.checked)}
              className="h-4 w-4 rounded accent-[#8585F7]"
            />
          </div>
        </div>
      </div>

      {/* Theme & Palette Info */}
      <div className="p-6 rounded-2xl bg-[#111318] border border-[#292C35] space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#E64B55]/10 text-[#E64B55]">
            <Palette size={20} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-[#F5F5F7]">Visual Theme</h3>
            <p className="text-xs text-[#A0A3AE]">Cinematic AMOLED aesthetic active</p>
          </div>
        </div>

        <div className="flex items-center gap-3 pt-2">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-[#08090B] border border-[#292C35] text-xs">
            <span className="h-3 w-3 rounded-full bg-[#8585F7]" />
            <span className="text-[#F5F5F7] font-medium">Accent #8585F7</span>
          </div>
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-[#08090B] border border-[#292C35] text-xs">
            <span className="h-3 w-3 rounded-full bg-[#E64B55]" />
            <span className="text-[#F5F5F7] font-medium">Crimson #E64B55</span>
          </div>
        </div>
      </div>

      {/* Data Management */}
      <div className="p-6 rounded-2xl bg-[#111318] border border-[#292C35] space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#FBBF24]/10 text-[#FBBF24]">
            <Database size={20} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-[#F5F5F7]">Storage & Cache</h3>
            <p className="text-xs text-[#A0A3AE]">Clear offline cached metadata and user library lists</p>
          </div>
        </div>

        <div className="pt-2">
          <button
            onClick={handleClearCache}
            className="px-4 py-2 rounded-xl bg-[#EF4444]/15 hover:bg-[#EF4444]/25 border border-[#EF4444]/30 text-xs font-semibold text-[#EF4444] transition-colors"
          >
            Clear Application Cache & Storage
          </button>
        </div>
      </div>

      <div className="flex justify-end gap-3 pt-2">
        <button
          onClick={handleSave}
          className="px-6 py-2.5 rounded-xl bg-[#8585F7] hover:bg-[#7272E5] text-xs font-bold text-white shadow-lg shadow-[#8585F7]/20 transition-all"
        >
          Save Preferences
        </button>
      </div>
    </div>
  );
};
