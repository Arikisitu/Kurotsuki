/**
 * @file auth.ts
 * @description Authentication and user identity endpoints (ready for OAuth / Supabase / Cloudflare integration).
 */

import { Router } from 'express';
import { z } from 'zod';
import { AuthenticationError } from '../errors';
import { validate } from '../middleware/validation';

export const authRouter = Router();

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
});

const registerSchema = z.object({
  username: z.string().min(3),
  email: z.string().email(),
  password: z.string().min(6),
});

/**
 * POST /login
 * Authenticates user credentials and returns session tokens
 */
authRouter.post('/login', validate({ body: loginSchema }), async (req, res, next) => {
  try {
    const { email } = req.body;

    // Standard SAHASRA auth response placeholder
    res.json({
      success: true,
      data: {
        token: `sahasra_token_${Buffer.from(email).toString('base64')}`,
        user: {
          id: 'user_dev_01',
          username: email.split('@')[0],
          email,
          avatar: null,
          createdAt: new Date().toISOString(),
        },
      },
    });
  } catch (err) {
    next(err);
  }
});

/**
 * POST /register
 * Registers a new user account
 */
authRouter.post('/register', validate({ body: registerSchema }), async (req, res, next) => {
  try {
    const { username, email } = req.body;

    res.status(201).json({
      success: true,
      data: {
        token: `sahasra_token_${Buffer.from(email).toString('base64')}`,
        user: {
          id: `user_${Date.now()}`,
          username,
          email,
          avatar: null,
          createdAt: new Date().toISOString(),
        },
      },
    });
  } catch (err) {
    next(err);
  }
});

/**
 * GET /me
 * Returns profile for the currently authenticated session
 */
authRouter.get('/me', async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw new AuthenticationError('Valid Bearer token required to access user profile');
    }

    const token = authHeader.substring(7);
    res.json({
      success: true,
      data: {
        id: 'user_dev_01',
        username: 'sahasra_user',
        email: 'user@sahasra.io',
        tokenPreview: `${token.substring(0, 10)}...`,
        preferences: {
          titleLanguage: 'userPreferred',
          adultFilter: true,
          theme: 'dark',
        },
      },
    });
  } catch (err) {
    next(err);
  }
});
