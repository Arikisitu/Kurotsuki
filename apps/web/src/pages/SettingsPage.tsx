import React, { useState, useEffect } from 'react';
import {
  Settings,
  Shield,
  Sliders,
  Palette,
  Database,
  Check,
  Languages,
  Tv,
  Sparkles,
  Layers,
} from 'lucide-react';
import { AudioType } from '@sahasra/shared-types';

export const SettingsPage: React.FC = () => {
  const [titleLanguage, setTitleLanguage] = useState<'english' | 'romaji' | 'native'>('english');
  const [playbackLanguage, setPlaybackLanguage] = useState<'en' | 'hi' | 'ja'>(() => {
    return (localStorage.getItem('sahasra_pref_language') as 'en' | 'hi' | 'ja') || 'en';
  });
  const [preferredAudioType, setPreferredAudioType] = useState<AudioType>(() => {
    return (localStorage.getItem('sahasra_pref_audio_type') as AudioType) || 'dub';
  });
  const [defaultQuality, setDefaultQuality] = useState<string>(() => {
    return localStorage.getItem('sahasra_pref_quality') || '1080p';
  });
  const [autoplayNext, setAutoplayNext] = useState<boolean>(() => {
    return localStorage.getItem('sahasra_pref_autoplay') !== 'false';
  });

  const [safeSearch, setSafeSearch] = useState(true);
  const [savedNotice, setSavedNotice] = useState(false);

  const handleSave = () => {
    localStorage.setItem('sahasra_pref_language', playbackLanguage);
    localStorage.setItem('sahasra_pref_audio_type', preferredAudioType);
    localStorage.setItem('sahasra_pref_quality', defaultQuality);
    localStorage.setItem('sahasra_pref_autoplay', String(autoplayNext));
    setSavedNotice(true);
    setTimeout(() => setSavedNotice(false), 2500);
  };

  const handleClearCache = () => {
    if (confirm('Clear cached media queries, extension manifests, and local storage?')) {
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
          Manage playback languages, auto-streaming hierarchy, video quality, and platform preferences
        </p>
      </div>

      {savedNotice && (
        <div className="flex items-center gap-2 p-3 rounded-xl bg-[#4ADE80]/15 border border-[#4ADE80]/30 text-xs font-semibold text-[#4ADE80] animate-in fade-in">
          <Check size={16} />
          <span>Playback and platform preferences saved successfully!</span>
        </div>
      )}

      {/* Playback & Audio Language Preference (Strictly Hindi, English, Japanese) */}
      <div className="p-6 rounded-2xl bg-[#111318] border border-[#292C35] space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#8585F7]/10 text-[#8585F7]">
            <Languages size={20} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-[#F5F5F7]">One-Click Streaming Language</h3>
            <p className="text-xs text-[#A0A3AE]">
              Automatic speed hierarchy matches your preferred language (strictly Hindi, English, Japanese)
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
          {[
            { id: 'en', label: 'English', desc: 'English Dub / Sub' },
            { id: 'hi', label: 'Hindi', desc: 'Hindi Dub / Multi' },
            { id: 'ja', label: 'Japanese', desc: 'Japanese Original (Sub)' },
          ].map((lang) => (
            <button
              key={lang.id}
              onClick={() => setPlaybackLanguage(lang.id as any)}
              className={`p-3.5 rounded-xl border text-left transition-all ${
                playbackLanguage === lang.id
                  ? 'bg-[#191C22] border-[#8585F7] text-[#F5F5F7] shadow-md shadow-[#8585F7]/10'
                  : 'bg-[#111318] border-[#292C35] text-[#A0A3AE] hover:border-[#8585F7]/40'
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold">{lang.label}</span>
                {playbackLanguage === lang.id && (
                  <Check size={14} className="text-[#8585F7]" />
                )}
              </div>
              <p className="text-[11px] text-[#A0A3AE] mt-1">{lang.desc}</p>
            </button>
          ))}
        </div>

        {/* Audio Type Preference (Sub vs Dub) */}
        <div className="pt-2 flex items-center gap-3">
          <span className="text-xs font-semibold text-[#A0A3AE]">Audio Mode:</span>
          <div className="flex gap-2">
            {[
              { id: 'dub', label: 'Dubbed (Voiceover)' },
              { id: 'sub', label: 'Subtitled (Captions)' },
            ].map((t) => (
              <button
                key={t.id}
                onClick={() => setPreferredAudioType(t.id as AudioType)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition-colors ${
                  preferredAudioType === t.id
                    ? 'bg-[#8585F7] text-black border-[#8585F7]'
                    : 'bg-[#08090B] border-[#292C35] text-[#A0A3AE] hover:text-white'
                }`}
              >
                {t.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Video Quality & Player Automation */}
      <div className="p-6 rounded-2xl bg-[#111318] border border-[#292C35] space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#8585F7]/10 text-[#8585F7]">
            <Tv size={20} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-[#F5F5F7]">Video Player Defaults</h3>
            <p className="text-xs text-[#A0A3AE]">Initial resolution and autoplay configuration</p>
          </div>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-2">
          {['1080p', '720p', '480p', 'Auto'].map((q) => (
            <button
              key={q}
              onClick={() => setDefaultQuality(q)}
              className={`p-2.5 rounded-xl border text-center text-xs font-bold transition-all ${
                defaultQuality === q
                  ? 'bg-[#8585F7] text-black border-[#8585F7]'
                  : 'bg-[#08090B] border-[#292C35] text-[#A0A3AE] hover:text-white'
              }`}
            >
              {q}
            </button>
          ))}
        </div>

        <div className="divide-y divide-[#292C35]/60 pt-2">
          <div className="flex items-center justify-between py-3">
            <div>
              <p className="text-xs font-semibold text-[#F5F5F7]">Autoplay Next Episode</p>
              <p className="text-[11px] text-[#A0A3AE]">Automatically resolve and launch next episode on finish</p>
            </div>
            <input
              type="checkbox"
              checked={autoplayNext}
              onChange={(e) => setAutoplayNext(e.target.checked)}
              className="h-4 w-4 rounded accent-[#8585F7]"
            />
          </div>
        </div>
      </div>

      {/* Headless Stream Provider Attribution & Whitelist Status */}
      <div className="p-6 rounded-2xl bg-[#111318] border border-[#292C35] space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#4ADE80]/10 text-[#4ADE80]">
            <Layers size={20} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-[#F5F5F7]">Integrated In-Built Stream Aggregator</h3>
            <p className="text-xs text-[#A0A3AE]">Headless extension repositories & active multi-source engine</p>
          </div>
        </div>

        <div className="space-y-2 pt-1 text-xs">
          <div className="flex items-center justify-between p-3 rounded-xl bg-[#08090B] border border-[#292C35]">
            <div>
              <p className="font-semibold text-white">Yūzōnō Anime Repository</p>
              <p className="text-[11px] text-[#A0A3AE]">251 extensions indexed • Auto-filtered to EN, HI, JA</p>
            </div>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 font-bold">
              Active
            </span>
          </div>

          <div className="flex items-center justify-between p-3 rounded-xl bg-[#08090B] border border-[#292C35]">
            <div>
              <p className="font-semibold text-white">Secozzi Aniyomi Extensions</p>
              <p className="text-[11px] text-[#A0A3AE]">Protobuf manifest parsed • Multi-source stream engine</p>
            </div>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 font-bold">
              Active
            </span>
          </div>

          <div className="flex items-center justify-between p-3 rounded-xl bg-[#08090B] border border-[#292C35]">
            <div>
              <p className="font-semibold text-white">Language Filtering Whitelist</p>
              <p className="text-[11px] text-[#A0A3AE]">Strictly enforces Hindi, English, and Japanese sources</p>
            </div>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#8585F7]/20 text-[#8585F7] font-bold">
              HI • EN • JA
            </span>
          </div>
        </div>
      </div>

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

      {/* Data Management */}
      <div className="p-6 rounded-2xl bg-[#111318] border border-[#292C35] space-y-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#FBBF24]/10 text-[#FBBF24]">
            <Database size={20} />
          </div>
          <div>
            <h3 className="text-sm font-bold text-[#F5F5F7]">Storage & Cache</h3>
            <p className="text-xs text-[#A0A3AE]">Clear offline cached metadata, stream ranks, and local preferences</p>
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
