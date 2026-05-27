import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { logout } from '@/api/endpoints';
import type { User } from '@/types';

interface AuthState {
  user: User | null;
  token: string | null; // non-null sentinel = logged in (real JWT lives in HttpOnly cookie)
  isVerified: boolean;
  isAdmin: boolean;
  login: (token: string) => void;
  loginFromUser: (user: User) => void;
  logout: () => void;
  setUser: (user: User) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isVerified: false,
      isAdmin: false,

      login: (token: string) => {
        set({ token });
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          set({
            isVerified: payload.verified ?? false,
            isAdmin: payload.role === 'ADMIN',
          });
        } catch {
          set({ isVerified: false, isAdmin: false });
        }
      },

      loginFromUser: (user: User) => {
        set({
          user,
          token: 'cookie',
          isVerified: user.verified,
          isAdmin: user.role === 'ADMIN',
        });
      },

      logout: () => {
        set({ user: null, token: null, isVerified: false, isAdmin: false });
        logout().catch(() => {});
      },

      setUser: (user: User) =>
        set({
          user,
          isVerified: user.verified,
          isAdmin: user.role === 'ADMIN',
        }),
    }),
    {
      name: 'freenote-auth',
    }
  )
);
