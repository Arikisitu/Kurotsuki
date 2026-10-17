/**
 * @file stream.ts
 * @description API router for one-click streaming resolution, repository extension sync, and speed ping tests.
 */

import { Router, Request, Response, NextFunction } from 'express';
import { streamResolverService } from '../services/stream-resolver.js';
import { repoParserService } from '../services/repo-parser.js';

export const streamRouter = Router();

/**
 * GET /api/v1/stream/resolve
 * One-click streaming resolution: queries providers, pings latency in parallel,
 * ranks by speed & quality hierarchy, returns primary stream + fallback queue.
 */
streamRouter.get('/resolve', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const title = (req.query.title as string) || 'Demo Anime';
    const episode = parseInt(req.query.episode as string, 10) || 1;
    const animeId = req.query.animeId ? parseInt(req.query.animeId as string, 10) : undefined;
    const rawLang = (req.query.lang as string)?.toLowerCase();
    const preferredLanguage = (rawLang === 'hi' || rawLang === 'ja' || rawLang === 'en') ? rawLang : 'en';
    const preferredAudioType = req.query.type === 'dub' ? 'dub' : req.query.type === 'sub' ? 'sub' : undefined;

    const result = await streamResolverService.resolve({
      animeTitle: title,
      episodeNumber: episode,
      animeId,
      preferredLanguage,
      preferredAudioType,
    });

    res.json({
      success: true,
      data: result,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * GET /api/v1/stream/repo/extensions
 * Retrieves Aniyomi extension catalogue entries filtered strictly to ['en', 'hi', 'ja', 'all'].
 */
streamRouter.get('/repo/extensions', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const forceRefresh = req.query.refresh === 'true';
    const syncResult = await repoParserService.syncAllRepositories(forceRefresh);

    const langFilter = (req.query.lang as string)?.toLowerCase();
    let extensions = syncResult.combinedExtensions;

    if (langFilter && ['en', 'hi', 'ja', 'all'].includes(langFilter)) {
      extensions = extensions.filter((e) => e.lang === langFilter);
    }

    res.json({
      success: true,
      data: {
        totalAvailable: syncResult.totalAvailable,
        totalFiltered: extensions.length,
        allowedLanguages: ['en', 'hi', 'ja', 'all'],
        repositories: syncResult.repositories.map((r) => ({
          name: r.repoName,
          url: r.repoUrl,
          total: r.totalExtensions,
          filtered: r.filteredCount,
        })),
        extensions,
      },
    });
  } catch (error) {
    next(error);
  }
});

/**
 * POST /api/v1/stream/ping
 * Measures latency (TTFB) and liveness for a given media stream URL.
 */
streamRouter.post('/ping', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { url, headers, timeoutMs } = req.body;
    if (!url || typeof url !== 'string') {
      res.status(400).json({ success: false, error: 'url is required' });
      return;
    }

    const ping = await streamResolverService.pingStream(url, headers || {}, timeoutMs || 2000);
    res.json({
      success: true,
      data: ping,
    });
  } catch (error) {
    next(error);
  }
});
