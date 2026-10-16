/**
 * @file anime.ts
 * @description Express router handling all Anime API endpoints.
 */

import { Router } from 'express';
import { z } from 'zod';
import { config } from '../config';
import { NotFoundError } from '../errors';
import { validate } from '../middleware/validation';
import { orchestrator } from '../services/orchestrator';

export const animeRouter = Router();

const pageQuerySchema = z.object({
  page: z
    .string()
    .optional()
    .transform((val) => (val ? Math.max(1, parseInt(val, 10) || 1) : 1)),
});

const searchQuerySchema = z.object({
  q: z.string().optional().default(''),
  page: z
    .string()
    .optional()
    .transform((val) => (val ? Math.max(1, parseInt(val, 10) || 1) : 1)),
});

const seasonalParamsSchema = z.object({
  year: z.string().regex(/^\d{4}$/, 'Year must be a 4-digit number').transform(Number),
  season: z.enum(['winter', 'spring', 'summer', 'fall', 'WINTER', 'SPRING', 'SUMMER', 'FALL']),
});

const idParamSchema = z.object({
  id: z.string().min(1, 'ID is required'),
});

/**
 * GET /trending
 * Retrieves trending anime titles
 */
animeRouter.get(
  '/trending',
  validate({ query: pageQuerySchema }),
  async (req, res, next) => {
    try {
      const page = (req.query as any).page || 1;
      const data = await orchestrator.getTrending(page);

      res.setHeader(
        'Cache-Control',
        `public, max-age=${config.cache.trendingTtlSeconds}, stale-while-revalidate=${config.cache.staleTtlSeconds}`
      );

      res.json({
        success: true,
        data: data.results.items,
        pageInfo: data.results.pageInfo,
      });
    } catch (err) {
      next(err);
    }
  }
);

/**
 * GET /popular
 * Retrieves all-time popular anime
 */
animeRouter.get(
  '/popular',
  validate({ query: pageQuerySchema }),
  async (req, res, next) => {
    try {
      const page = (req.query as any).page || 1;
      const data = await orchestrator.getPopular(page);

      res.setHeader(
        'Cache-Control',
        `public, max-age=${config.cache.popularTtlSeconds}, stale-while-revalidate=${config.cache.staleTtlSeconds}`
      );

      res.json({
        success: true,
        data: data.results.items,
        pageInfo: data.results.pageInfo,
      });
    } catch (err) {
      next(err);
    }
  }
);

/**
 * GET /seasonal/:year/:season
 * Retrieves anime for a specific broadcast season & year
 */
animeRouter.get(
  '/seasonal/:year/:season',
  validate({ params: seasonalParamsSchema, query: pageQuerySchema }),
  async (req, res, next) => {
    try {
      const { year, season } = req.params as any;
      const page = (req.query as any).page || 1;
      const data = await orchestrator.getSeasonal(year, season, page);

      res.setHeader(
        'Cache-Control',
        `public, max-age=${config.cache.seasonalTtlSeconds}, stale-while-revalidate=${config.cache.staleTtlSeconds}`
      );

      res.json({
        success: true,
        data: data.results.items,
        pageInfo: data.results.pageInfo,
      });
    } catch (err) {
      next(err);
    }
  }
);

/**
 * GET /search
 * Searches anime by keyword
 */
animeRouter.get(
  '/search',
  validate({ query: searchQuerySchema }),
  async (req, res, next) => {
    try {
      const { q, page } = req.query as any;
      const data = await orchestrator.searchAnime(q, page);

      res.setHeader(
        'Cache-Control',
        `public, max-age=${config.cache.searchTtlSeconds}, stale-while-revalidate=${config.cache.staleTtlSeconds}`
      );

      res.json({
        success: true,
        data: data.results.items,
        pageInfo: data.results.pageInfo,
      });
    } catch (err) {
      next(err);
    }
  }
);

/**
 * GET /:id
 * Retrieves full details for a single anime title
 */
animeRouter.get(
  '/:id',
  validate({ params: idParamSchema }),
  async (req, res, next) => {
    try {
      const id = req.params.id as string;
      const anime = await orchestrator.getAnimeById(id);

      if (!anime) {
        throw new NotFoundError(`Anime with id '${id}'`);
      }

      res.setHeader(
        'Cache-Control',
        `public, max-age=${config.cache.detailsTtlSeconds}, stale-while-revalidate=${config.cache.staleTtlSeconds}`
      );

      res.json({
        success: true,
        data: anime,
      });
    } catch (err) {
      next(err);
    }
  }
);
