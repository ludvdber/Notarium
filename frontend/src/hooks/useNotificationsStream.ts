import { useEffect } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '@/stores/useAuthStore';
import { useNotificationStore } from '@/stores/useNotificationStore';

interface ServerNotification {
  id: number;
  type: string;
  payload: Record<string, unknown>;
  read: boolean;
  createdAt: string;
}

/**
 * Opens a Server-Sent Events connection to `/api/notifications/stream` while the user
 * is authenticated. Every pushed event is added to the Zustand store so `NotificationBell`
 * updates instantly without polling. On network error the browser's `EventSource`
 * reconnects automatically after ~3s — we also refetch the unread count on reconnect.
 */
export function useNotificationsStream() {
  const token = useAuthStore((s) => s.token);
  const push = useNotificationStore((s) => s.push);
  const queryClient = useQueryClient();

  // Unread count — polled as a safety net in case SSE drops for a long time.
  useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: async () => {
      const res = await fetch('/api/notifications/unread-count', { credentials: 'include' });
      if (!res.ok) throw new Error('Failed to fetch unread count');
      return (await res.json()) as number;
    },
    enabled: !!token,
    refetchInterval: 60_000, // 60s safety poll; SSE is the primary channel
  });

  useEffect(() => {
    if (!token) return;

    const source = new EventSource('/api/notifications/stream', { withCredentials: true });

    source.addEventListener('notification', (event) => {
      try {
        const data = JSON.parse((event as MessageEvent).data) as ServerNotification;
        push({
          type: 'info',
          messageKey: `notifications.${data.type}`,
          params: data.payload as Record<string, string | number>,
        });
        queryClient.invalidateQueries({ queryKey: ['notifications'] });
      } catch {
        // Malformed payload — ignore, fall back to next poll.
      }
    });

    source.onerror = () => {
      // EventSource auto-reconnects; we just log once so dev tools show the break.
      // Silent in prod (browser retries under the hood).
    };

    return () => source.close();
  }, [token, push, queryClient]);
}
