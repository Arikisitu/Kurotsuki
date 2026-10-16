/**
 * SAHASRA Design Tokens
 * Premium dark AMOLED aesthetic for Anime & Manga discovery platform
 */

export const colors = {
  bg: {
    DEFAULT: '#08090B',
    surface: '#111318',
    elevated: '#191C22',
  },
  accent: {
    DEFAULT: '#8585F7',
    secondary: '#AAA7FF',
    glow: 'rgba(133, 133, 247, 0.15)',
  },
  text: {
    DEFAULT: '#F5F5F7',
    secondary: '#A0A3AE',
    muted: '#6B7280',
  },
  border: '#292C35',
  borderLight: '#383B46',
  crimson: '#E64B55',
  crimsonGlow: 'rgba(230, 75, 85, 0.15)',
  success: '#4ADE80',
  warning: '#FBBF24',
  error: '#EF4444',
} as const;

export const typography = {
  fontFamily: {
    sans: ['"Plus Jakarta Sans"', 'system-ui', 'sans-serif'],
    display: ['"Outfit"', 'system-ui', 'sans-serif'],
  },
} as const;

export const transitions = {
  default: 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
  smooth: 'all 0.35s cubic-bezier(0.16, 1, 0.3, 1)',
  spring: 'all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1)',
} as const;
