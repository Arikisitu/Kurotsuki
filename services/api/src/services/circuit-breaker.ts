/**
 * @file circuit-breaker.ts
 * @description Circuit breaker implementation protecting against failing upstream providers.
 */

import { config } from '../config';
import { ProviderError } from '../errors';

export type CircuitState = 'CLOSED' | 'OPEN' | 'HALF_OPEN';

export interface CircuitBreakerOptions {
  failureThreshold?: number;
  recoveryTimeMs?: number;
  successThreshold?: number;
}

interface ProviderCircuitRecord {
  state: CircuitState;
  failures: number;
  consecutiveSuccesses: number;
  lastStateChange: number;
  lastFailureTime?: number;
}

export class CircuitBreaker {
  private static instance: CircuitBreaker;
  private readonly records: Map<string, ProviderCircuitRecord> = new Map();
  private readonly failureThreshold: number;
  private readonly recoveryTimeMs: number;
  private readonly successThreshold: number;

  constructor(options?: CircuitBreakerOptions) {
    this.failureThreshold = options?.failureThreshold ?? config.circuitBreaker.failureThreshold;
    this.recoveryTimeMs = options?.recoveryTimeMs ?? config.circuitBreaker.recoveryTimeMs;
    this.successThreshold = options?.successThreshold ?? config.circuitBreaker.successThreshold;
  }

  public static getInstance(): CircuitBreaker {
    if (!CircuitBreaker.instance) {
      CircuitBreaker.instance = new CircuitBreaker();
    }
    return CircuitBreaker.instance;
  }

  private getRecord(key: string): ProviderCircuitRecord {
    const normalizedKey = key.toLowerCase();
    let record = this.records.get(normalizedKey);
    if (!record) {
      record = {
        state: 'CLOSED',
        failures: 0,
        consecutiveSuccesses: 0,
        lastStateChange: Date.now(),
      };
      this.records.set(normalizedKey, record);
    }
    return record;
  }

  public getState(key: string): CircuitState {
    const record = this.getRecord(key);
    const now = Date.now();

    if (record.state === 'OPEN') {
      if (now - record.lastStateChange >= this.recoveryTimeMs) {
        record.state = 'HALF_OPEN';
        record.lastStateChange = now;
        record.consecutiveSuccesses = 0;
      }
    }

    return record.state;
  }

  public isOpen(key: string): boolean {
    return this.getState(key) === 'OPEN';
  }

  public recordSuccess(key: string): void {
    const record = this.getRecord(key);
    const state = this.getState(key);

    if (state === 'HALF_OPEN') {
      record.consecutiveSuccesses++;
      if (record.consecutiveSuccesses >= this.successThreshold) {
        record.state = 'CLOSED';
        record.failures = 0;
        record.consecutiveSuccesses = 0;
        record.lastStateChange = Date.now();
      }
    } else if (state === 'CLOSED') {
      record.failures = 0;
    }
  }

  public recordFailure(key: string): void {
    const record = this.getRecord(key);
    const now = Date.now();
    record.lastFailureTime = now;

    if (record.state === 'HALF_OPEN') {
      record.state = 'OPEN';
      record.lastStateChange = now;
      record.consecutiveSuccesses = 0;
    } else if (record.state === 'CLOSED') {
      record.failures++;
      if (record.failures >= this.failureThreshold) {
        record.state = 'OPEN';
        record.lastStateChange = now;
      }
    }
  }

  public reset(key?: string): void {
    if (key) {
      this.records.delete(key.toLowerCase());
    } else {
      this.records.clear();
    }
  }

  public async execute<T>(providerName: string, action: () => Promise<T>): Promise<T> {
    const state = this.getState(providerName);

    if (state === 'OPEN') {
      const record = this.getRecord(providerName);
      const remainingMs = Math.max(0, this.recoveryTimeMs - (Date.now() - record.lastStateChange));
      throw new ProviderError(
        providerName,
        `Circuit breaker is OPEN for provider '${providerName}'. Cooling down (${Math.round(remainingMs / 1000)}s remaining).`,
        { statusCode: 503, code: 'CIRCUIT_BREAKER_OPEN' }
      );
    }

    try {
      const result = await action();
      this.recordSuccess(providerName);
      return result;
    } catch (err) {
      this.recordFailure(providerName);
      throw err;
    }
  }

  public getAllStates(): Record<string, ProviderCircuitRecord> {
    const states: Record<string, ProviderCircuitRecord> = {};
    for (const [key, record] of this.records.entries()) {
      states[key] = { ...record, state: this.getState(key) };
    }
    return states;
  }
}

export const circuitBreaker = CircuitBreaker.getInstance();
