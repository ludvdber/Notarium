import { lazy, Suspense, useEffect } from 'react';
import { Container, Skeleton, Box } from '@mui/material';
import { Helmet } from 'react-helmet-async';
import { useSearchParams } from 'react-router-dom';
import { useAuthStore } from '@/stores/useAuthStore';
import { useAuthPromptStore } from '@/stores/useAuthPromptStore';
import HeroSection from '@/components/home/HeroSection';
import Divider from '@/components/ui/Divider';
import AdSlot from '@/components/ui/AdSlot';

// Maps the ?authError=<code> set by OAuth2LoginFailureHandler to an i18n key.
const AUTH_ERROR_KEYS: Record<string, string> = {
  banned: 'auth.oauthBanned',
  unverified_email: 'auth.oauthUnverifiedEmail',
  oauth_failed: 'auth.oauthFailed',
};

const StatsSection = lazy(() => import('@/components/home/StatsSection'));
const NewsAndLinks = lazy(() => import('@/components/home/NewsAndLinks'));
const PopularDocs = lazy(() => import('@/components/home/PopularDocs'));
const RecentAndShortcuts = lazy(() => import('@/components/home/RecentAndShortcuts'));
const DelegatesDiscord = lazy(() => import('@/components/home/DelegatesDiscord'));

function SectionFallback() {
  return (
    <Box sx={{ py: 4 }}>
      <Skeleton variant="rounded" height={140} sx={{ borderRadius: 3, bgcolor: 'rgba(255,255,255,0.04)' }} />
    </Box>
  );
}

export default function Home() {
  const { token } = useAuthStore();
  const [searchParams, setSearchParams] = useSearchParams();
  const showPrompt = useAuthPromptStore((s) => s.show);

  // The "please log in" snackbar is now global — rendered by <AuthPromptSnackbar/>
  // at the App root, fired either by NAV link clicks (no URL flash) or by
  // <ProtectedRoute> on direct URL entry (fallback).

  // A failed OAuth login (banned Discord, unverified provider email) redirects here with
  // ?authError=<code>. Surface it via the global snackbar, then strip the param so a refresh
  // doesn't re-show it.
  useEffect(() => {
    const code = searchParams.get('authError');
    if (!code) return;
    showPrompt(AUTH_ERROR_KEYS[code] ?? 'auth.oauthFailed');
    searchParams.delete('authError');
    setSearchParams(searchParams, { replace: true });
  }, [searchParams, setSearchParams, showPrompt]);

  return (
    <>
      <Helmet><title>Freenote — Éclaire ta promo</title></Helmet>
      <HeroSection />
      <Container maxWidth="lg">
        {token && (
          <>
            <Suspense fallback={<SectionFallback />}>
              <StatsSection />
            </Suspense>
            <Divider />
          </>
        )}
        {!token && <AdSlot width={728} height={90} sx={{ my: 4 }} />}
        <Suspense fallback={<SectionFallback />}>
          <NewsAndLinks />
        </Suspense>
        {token && (
          <>
            <Divider />
            <Suspense fallback={<SectionFallback />}>
              <PopularDocs />
            </Suspense>
            <Divider />
            <Suspense fallback={<SectionFallback />}>
              <RecentAndShortcuts />
            </Suspense>
            <Divider />
            <Suspense fallback={<SectionFallback />}>
              <DelegatesDiscord />
            </Suspense>
          </>
        )}
      </Container>
    </>
  );
}
