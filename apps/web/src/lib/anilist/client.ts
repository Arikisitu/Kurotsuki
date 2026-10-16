/**
 * AniList Public GraphQL Client
 * Robust, dependency-free client with retry logic and error normalization.
 */

import { AniListGraphQLResponse } from './types';

const ANILIST_ENDPOINT = 'https://graphql.anilist.co';

export interface GraphQLRequestOptions {
  variables?: Record<string, unknown>;
  signal?: AbortSignal;
}

export class AniListApiError extends Error {
  public status?: number;
  public errors?: unknown[];

  constructor(message: string, status?: number, errors?: unknown[]) {
    super(message);
    this.name = 'AniListApiError';
    this.status = status;
    this.errors = errors;
  }
}

/**
 * Execute a GraphQL query against AniList API with automatic retry on 429 rate limit.
 */
export async function anilistFetch<T>(
  query: string,
  variables: Record<string, unknown> = {},
  options: { signal?: AbortSignal; maxRetries?: number } = {}
): Promise<T> {
  const { signal, maxRetries = 2 } = options;
  let attempt = 0;

  while (attempt <= maxRetries) {
    try {
      const response = await fetch(ANILIST_ENDPOINT, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({
          query,
          variables,
        }),
        signal,
      });

      // Handle Rate Limiting (429)
      if (response.status === 429) {
        attempt++;
        if (attempt > maxRetries) {
          throw new AniListApiError('AniList API rate limit reached. Please wait a moment and try again.', 429);
        }
        const retryAfter = parseInt(response.headers.get('Retry-After') || '2', 10);
        await new Promise((resolve) => setTimeout(resolve, (retryAfter || 2) * 1000));
        continue;
      }

      if (!response.ok) {
        throw new AniListApiError(`AniList request failed with status ${response.status}`, response.status);
      }

      const json: AniListGraphQLResponse<T> = await response.json();

      if (json.errors && json.errors.length > 0) {
        const errorMessages = json.errors.map((e) => e.message).join(', ');
        throw new AniListApiError(errorMessages, 400, json.errors);
      }

      if (!json.data) {
        throw new AniListApiError('AniList response contained no data payload.');
      }

      return json.data;
    } catch (err: unknown) {
      if (err instanceof AniListApiError) {
        throw err;
      }
      if ((err as Error)?.name === 'AbortError') {
        throw err;
      }
      if (attempt < maxRetries) {
        attempt++;
        await new Promise((resolve) => setTimeout(resolve, 1000 * attempt));
        continue;
      }
      throw new AniListApiError((err as Error)?.message || 'Network error fetching data from AniList');
    }
  }

  throw new AniListApiError('Failed to complete request after maximum retries');
}
