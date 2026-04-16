import { lazy, Suspense, useState, useEffect } from 'react';
import { Container, Skeleton, Box, Snackbar, Alert } from '@mui/material';
import { Helmet } from 'react-helmet-async';
import { useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import HeroSection from '@/components/home/HeroSection';
import Divider from '@/components/ui/Divider';

const StatsSection = lazy(() => import('@/components/home/StatsSection'));
const NewsAndLinks = lazy(() => import('@/components/home/NewsAndLinks'));
const PopularDocs = lazy(() => import('@/components/home/PopularDocs'));
const LeaderboardDelegates = lazy(() => import('@/components/home/LeaderboardDelegates'));
const SignupBanner = lazy(() => import('@/components/home/SignupBanner'));

function SectionFallback() {
  return (
    <Box sx={{ py: 4 }}>
      <Skeleton variant="rounded" height={140} sx={{ borderRadius: 3, bgcolor: 'rgba(255,255,255,0.04)' }} />
    </Box>
  );
}

export default function Home() {
  const { t } = useTranslation();
  const location = useLocation();
  const [showLoginAlert, setShowLoginAlert] = useState(false);

  useEffect(() => {
    if (location.state?.loginRequired) {
      setShowLoginAlert(true);
      // Clear state so refresh doesn't re-show
      window.history.replaceState({}, '');
    }
  }, [location.state]);

  return (
    <>
      <Helmet><title>Freenote — Éclaire ta promo</title></Helmet>
      <Snackbar
        open={showLoginAlert}
        autoHideDuration={5000}
        onClose={() => setShowLoginAlert(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert onClose={() => setShowLoginAlert(false)} severity="info" variant="filled">
          {t('auth.loginRequired')}
        </Alert>
      </Snackbar>
      <HeroSection />
      <Container maxWidth="lg">
        <Suspense fallback={<SectionFallback />}>
          <StatsSection />
        </Suspense>
        <Divider />
        <Suspense fallback={<SectionFallback />}>
          <NewsAndLinks />
        </Suspense>
        <Divider />
        <Suspense fallback={<SectionFallback />}>
          <PopularDocs />
        </Suspense>
        <Divider />
        <Suspense fallback={<SectionFallback />}>
          <LeaderboardDelegates />
        </Suspense>
        <Divider />
        <Suspense fallback={<SectionFallback />}>
          <SignupBanner />
        </Suspense>
      </Container>
    </>
  );
}
