import { useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '@/stores/useAuthStore';

/**
 * Logout that also clears the TanStack Query cache. Without this, switching account
 * (logout + login another user) would briefly serve the previous user's `['me']` cache
 * to the Profile page before the fresh request lands.
 */
export function useLogout() {
  const storeLogout = useAuthStore((s) => s.logout);
  const queryClient = useQueryClient();
  return () => {
    queryClient.clear();
    storeLogout();
  };
}
