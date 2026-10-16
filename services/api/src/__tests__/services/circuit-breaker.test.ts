import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CircuitBreaker } from '../../services/circuit-breaker';

describe('CircuitBreaker', () => {
  let cb: CircuitBreaker;

  beforeEach(() => {
    // failureThreshold: 3, recoveryTimeMs: 1000, successThreshold: 2
    cb = new CircuitBreaker({
      failureThreshold: 3,
      recoveryTimeMs: 1000,
      successThreshold: 2,
    });
  });

  it('starts in CLOSED state and executes successful calls', async () => {
    expect(cb.getState('test-provider')).toBe('CLOSED');

    const result = await cb.execute('test-provider', async () => 'ok');
    expect(result).toBe('ok');
    expect(cb.getState('test-provider')).toBe('CLOSED');
  });

  it('trips to OPEN state after exceeding failureThreshold', async () => {
    const failingFn = vi.fn().mockRejectedValue(new Error('upstream error'));

    // 1st failure
    await expect(cb.execute('test-provider', failingFn)).rejects.toThrow();
    expect(cb.getState('test-provider')).toBe('CLOSED');

    // 2nd failure
    await expect(cb.execute('test-provider', failingFn)).rejects.toThrow();
    expect(cb.getState('test-provider')).toBe('CLOSED');

    // 3rd failure -> trips to OPEN
    await expect(cb.execute('test-provider', failingFn)).rejects.toThrow();
    expect(cb.getState('test-provider')).toBe('OPEN');

    // 4th call is blocked immediately without calling the action
    const notCalledFn = vi.fn();
    await expect(cb.execute('test-provider', notCalledFn)).rejects.toThrow(
      /Circuit breaker is OPEN/
    );
    expect(notCalledFn).not.toHaveBeenCalled();
  });

  it('transitions to HALF_OPEN after recoveryTimeMs and recovers to CLOSED on successes', async () => {
    const failingFn = vi.fn().mockRejectedValue(new Error('fail'));

    // Trip circuit
    for (let i = 0; i < 3; i++) {
      await expect(cb.execute('test-provider', failingFn)).rejects.toThrow();
    }
    expect(cb.getState('test-provider')).toBe('OPEN');

    // Advance time past recoveryTimeMs
    vi.setSystemTime(Date.now() + 1500);

    // Should now be HALF_OPEN
    expect(cb.getState('test-provider')).toBe('HALF_OPEN');

    // 1st success in HALF_OPEN
    await cb.execute('test-provider', async () => 'trial 1');
    expect(cb.getState('test-provider')).toBe('HALF_OPEN');

    // 2nd success in HALF_OPEN -> closes circuit!
    await cb.execute('test-provider', async () => 'trial 2');
    expect(cb.getState('test-provider')).toBe('CLOSED');

    vi.useRealTimers();
  });

  it('transitions back to OPEN if a call fails in HALF_OPEN state', async () => {
    const failingFn = vi.fn().mockRejectedValue(new Error('fail'));

    for (let i = 0; i < 3; i++) {
      await expect(cb.execute('test-provider', failingFn)).rejects.toThrow();
    }
    expect(cb.getState('test-provider')).toBe('OPEN');

    // Advance time past recovery
    vi.setSystemTime(Date.now() + 1500);
    expect(cb.getState('test-provider')).toBe('HALF_OPEN');

    // Failure in HALF_OPEN -> immediately OPEN
    await expect(cb.execute('test-provider', failingFn)).rejects.toThrow();
    expect(cb.getState('test-provider')).toBe('OPEN');

    vi.useRealTimers();
  });
});
