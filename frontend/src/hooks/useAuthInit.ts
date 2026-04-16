import { useEffect, useRef } from 'react';
import { useAuthStore } from '@/stores/useAuthStore';
import { getCurrentUser } from '@/api/endpoints';

/**
 * On app mount, attempt to fetch the current user via the HttpOnly JWT cookie.
 * If the cookie exists and is valid, hydrate the auth store.
 * If not, clear any stale state.
 */
export function useAuthInit() {
  const { token, loginFromUser, logout } = useAuthStore();
  const didInit = useRef(false);

  useEffect(() => {
    if (didInit.current) return;
    didInit.current = true;

    // Only verify cookie if localStorage says we had a session — avoids a noisy 401/500 on cold start
    if (!token) return;

    getCurrentUser()
      .then((u) => loginFromUser(u))
      .catch(() => {
        // Cookie expired — clear stale store
        logout();
      });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps
}
