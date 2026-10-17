import { describe, expect, it } from 'vitest';
import { StreamResolverService } from '../../services/stream-resolver';
import { StreamSource } from '@sahasra/shared-types';

describe('StreamResolverService', () => {
  const service = new StreamResolverService();

  const mockSources: StreamSource[] = [
    {
      id: 'src-en-dub-1080p',
      providerId: 'p-en',
      providerName: 'EN Dub HighSpeed',
      url: 'https://example.com/en-1080.m3u8',
      quality: '1080p',
      isHls: true,
      isDash: false,
      audioLanguage: 'en',
      audioType: 'dub',
      ttfbMs: 65,
      status: 'active',
    },
    {
      id: 'src-ja-sub-1080p',
      providerId: 'p-ja',
      providerName: 'JA Sub Master',
      url: 'https://example.com/ja-1080.m3u8',
      quality: '1080p',
      isHls: true,
      isDash: false,
      audioLanguage: 'ja',
      audioType: 'sub',
      ttfbMs: 40,
      status: 'active',
    },
    {
      id: 'src-hi-dub-1080p',
      providerId: 'p-hi',
      providerName: 'HI Dub Fast',
      url: 'https://example.com/hi-1080.mp4',
      quality: '1080p',
      isHls: false,
      isDash: false,
      audioLanguage: 'hi',
      audioType: 'dub',
      ttfbMs: 50,
      status: 'active',
    },
    {
      id: 'src-hi-dub-720p',
      providerId: 'p-hi-2',
      providerName: 'HI Dub Backup',
      url: 'https://example.com/hi-720.mp4',
      quality: '720p',
      isHls: false,
      isDash: false,
      audioLanguage: 'hi',
      audioType: 'dub',
      ttfbMs: 350,
      status: 'active',
    },
  ];

  it('ranks Hindi stream first when preferred language is "hi"', () => {
    const ranked = service.sortHierarchy(mockSources, 'hi');
    expect(ranked[0]!.audioLanguage).toBe('hi');
    expect(ranked[0]!.id).toBe('src-hi-dub-1080p');
  });

  it('ranks English stream first when preferred language is "en"', () => {
    const ranked = service.sortHierarchy(mockSources, 'en');
    expect(ranked[0]!.audioLanguage).toBe('en');
    expect(ranked[0]!.id).toBe('src-en-dub-1080p');
  });

  it('ranks Japanese stream first when preferred language is "ja"', () => {
    const ranked = service.sortHierarchy(mockSources, 'ja');
    expect(ranked[0]!.audioLanguage).toBe('ja');
    expect(ranked[0]!.id).toBe('src-ja-sub-1080p');
  });

  it('prioritizes low latency TTFB when languages match', () => {
    const hindiSources: StreamSource[] = [
      {
        ...mockSources[3]!,
        quality: '1080p',
      },
      {
        ...mockSources[2]!,
        quality: '1080p',
      },
    ];

    const ranked = service.sortHierarchy(hindiSources, 'hi');
    expect(ranked[0]!.ttfbMs).toBe(50);
    expect(ranked[1]!.ttfbMs).toBe(350);
  });

  it('generates a primary stream and ordered fallback queue', async () => {
    const result = await service.resolve({
      animeTitle: 'Attack on Titan',
      episodeNumber: 1,
      preferredLanguage: 'en',
    });

    expect(result.primary).toBeDefined();
    expect(result.fallbackQueue.length).toBeGreaterThan(0);
    expect(result.allWorkingSources.length).toBeGreaterThan(0);
    expect(result.preferredLanguage).toBe('en');
    expect(result.episodeNumber).toBe(1);
  });
});
