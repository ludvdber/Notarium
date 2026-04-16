import { create } from 'zustand';

export interface Notification {
  id: string;
  type: 'info' | 'success' | 'warning';
  messageKey: string;
  params?: Record<string, string | number>;
  createdAt: number;
  read: boolean;
}

interface NotificationState {
  notifications: Notification[];
  push: (n: Omit<Notification, 'id' | 'createdAt' | 'read'>) => void;
  markAllRead: () => void;
  clear: () => void;
  unreadCount: () => number;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  notifications: [],

  push: (n) =>
    set((state) => ({
      notifications: [
        {
          ...n,
          id: `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
          createdAt: Date.now(),
          read: false,
        },
        ...state.notifications,
      ].slice(0, 50), // keep max 50
    })),

  markAllRead: () =>
    set((state) => ({
      notifications: state.notifications.map((n) => ({ ...n, read: true })),
    })),

  clear: () => set({ notifications: [] }),

  unreadCount: () => get().notifications.filter((n) => !n.read).length,
}));
