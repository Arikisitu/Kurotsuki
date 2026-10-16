import request from 'supertest';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { app } from '../../app';
import { orchestrator } from '../../services/orchestrator';

describe('Anime Routes', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('GET /api/v1/anime/trending returns 200 and list of anime', async () => {
    const mockData = {
      results: {
        items: [
          {
            id: 1,
            title: { english: 'Solo Leveling', userPreferred: 'Solo Leveling' },
            format: 'TV',
            averageScore: 87,
          },
        ],
        pageInfo: {
          total: 1,
          perPage: 20,
          currentPage: 1,
          lastPage: 1,
          hasNextPage: false,
        },
      },
    };

    vi.spyOn(orchestrator, 'getTrending').mockResolvedValueOnce(mockData as any);

    const res = await request(app).get('/api/v1/anime/trending?page=1');

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data).toHaveLength(1);
    expect(res.body.data[0].title.english).toBe('Solo Leveling');
    expect(res.headers['cache-control']).toContain('public');
  });

  it('GET /api/v1/anime/search validates query and returns results', async () => {
    const mockData = {
      results: {
        items: [{ id: 2, title: { userPreferred: 'Jujutsu Kaisen' } }],
        pageInfo: { total: 1, perPage: 20, currentPage: 1, lastPage: 1, hasNextPage: false },
      },
    };

    vi.spyOn(orchestrator, 'searchAnime').mockResolvedValueOnce(mockData as any);

    const res = await request(app).get('/api/v1/anime/search?q=Jujutsu&page=1');

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data[0].id).toBe(2);
  });

  it('GET /api/v1/anime/:id returns 200 with details or 404 if not found', async () => {
    vi.spyOn(orchestrator, 'getAnimeById').mockImplementation(async (id: string) => {
      if (id === '1') {
        return { id: 1, title: { userPreferred: 'One Piece' } } as any;
      }
      return null;
    });

    // Found
    const resFound = await request(app).get('/api/v1/anime/1');
    expect(resFound.status).toBe(200);
    expect(resFound.body.data.id).toBe(1);

    // Not found
    const resNotFound = await request(app).get('/api/v1/anime/9999999');
    expect(resNotFound.status).toBe(404);
    expect(resNotFound.body.success).toBe(false);
    expect(resNotFound.body.error.code).toBe('NOT_FOUND');
  });

  it('GET /api/v1/health returns health status', async () => {
    const res = await request(app).get('/api/v1/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('healthy');
    expect(res.body.service).toBe('sahasra-api');
    expect(typeof res.body.uptimeSeconds).toBe('number');
  });
});
