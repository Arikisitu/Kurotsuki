/**
 * @file manga.ts
 * @description Express router handling all Manga API endpoints.
 */

import { Router } from 'express';
import { z } from 'zod';
import { config } from '../config';
import { NotFoundError } from '../errors';
import { validate } from '../middleware/validation';
import { orchestrator } from '../services/orchestrator';

export const mangaRouter = Router();

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

const idParamSchema = z.object({
  id: z.string().min(1, 'ID is required'),
});

/**
 * GET /trending
 * Retrieves trending manga titles
 */
mangaRouter.get(
  '/trending',
  validate({ query: pageQuerySchema }),
  async (req, res, next) => {
    try {
      const page = (req.query as any).page || 1;
      const data = await orchestrator.getMangaTrending(page);

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
 * Retrieves popular manga titles
 */
mangaRouter.get(
  '/popular',
  validate({ query: pageQuerySchema }),
  async (req, res, next) => {
    try {
      const page = (req.query as any).page || 1;
      const data = await orchestrator.getMangaPopular(page);

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
 * GET /search
 * Searches manga by keyword
 */
mangaRouter.get(
  '/search',
  validate({ query: searchQuerySchema }),
  async (req, res, next) => {
    try {
      const { q, page } = req.query as any;
      const data = await orchestrator.searchManga(q, page);

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
 * Retrieves full details for a single manga title
 */
mangaRouter.get(
  '/:id',
  validate({ params: idParamSchema }),
  async (req, res, next) => {
    try {
      const id = req.params.id as string;
      const manga = await orchestrator.getMangaById(id);

      if (!manga) {
        throw new NotFoundError(`Manga with id '${id}'`);
      }

      res.setHeader(
        'Cache-Control',
        `public, max-age=${config.cache.detailsTtlSeconds}, stale-while-revalidate=${config.cache.staleTtlSeconds}`
      );

      res.json({
        success: true,
        data: manga,
      });
    } catch (err) {
      next(err);
    }
  }
);
