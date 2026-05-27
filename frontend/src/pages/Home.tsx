import { lazy, Suspense } from 'react';
import { Container, Skeleton, Box } from '@mui/material';
import { Helmet } from 'react-helmet-async';
import { useAuthStore } from '@/stores/useAuthStore';
import HeroSection from '@/components/home/HeroSection';
import Divider from '@/components/ui/Divider';
import AdSlot from '@/components/ui/AdSlot';

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

  // The "please log in" snackbar is now global — rendered by <AuthPromptSnackbar/>
  // at the App root, fired either by NAV link clicks (no URL flash) or by
  // <ProtectedRoute> on direct URL entry (fallback).

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
