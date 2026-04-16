import { useEffect, useRef, useSyncExternalStore } from 'react';

interface Options {
  duration?: number;
  start?: number;
  enabled?: boolean;
}

export function useCountUp(target: number, { duration = 1400, start = 0, enabled = true }: Options = {}) {
  const valueRef = useRef(start);
  const subscribersRef = useRef(new Set<() => void>());
  const rafRef = useRef<number | null>(null);
  const startTimeRef = useRef<number | null>(null);

  const subscribe = (cb: () => void) => {
    subscribersRef.current.add(cb);
    return () => { subscribersRef.current.delete(cb); };
  };

  const notify = () => subscribersRef.current.forEach((cb) => cb());

  useEffect(() => {
    if (!enabled || target === start) {
      valueRef.current = target;
      notify();
      return;
    }

    const tick = (now: number) => {
      if (startTimeRef.current === null) startTimeRef.current = now;
      const elapsed = now - startTimeRef.current;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      valueRef.current = Math.round(start + (target - start) * eased);
      notify();
      if (progress < 1) {
        rafRef.current = requestAnimationFrame(tick);
      }
    };

    rafRef.current = requestAnimationFrame(tick);
    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
      startTimeRef.current = null;
    };
  }, [target, duration, start, enabled]);

  return useSyncExternalStore(subscribe, () => valueRef.current);
}
