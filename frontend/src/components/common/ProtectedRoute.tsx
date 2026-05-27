import { useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { Typography, Box, Button } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '@/stores/useAuthStore';
import { useAuthPromptStore } from '@/stores/useAuthPromptStore';
import PageWrapper from '@/components/layout/PageWrapper';

interface Props {
  requireVerified?: boolean;
  requireAdmin?: boolean;
  children: React.ReactNode;
}

/**
 * Guards routes that require authentication (or verified / admin status).
 *
 * <p>The primary UX path is via {@link AuthPromptSnackbar}: navbar/mobile-menu links
 * intercept clicks before navigation and show the snackbar without any URL change.
 * This component stays as a defence-in-depth layer for direct URL entry (bookmark,
 * shared link, back/forward button) — it fires the same snackbar + redirects home.
 */
export default function ProtectedRoute({ requireVerified, requireAdmin, children }: Props) {
  const { t } = useTranslation();
  const { token, isVerified, isAdmin } = useAuthStore();
  const promptLogin = useAuthPromptStore((s) => s.show);

  const needsLogin = !token;
  const needsAdmin = !needsLogin && requireAdmin && !isAdmin;

  useEffect(() => {
    if (needsLogin) promptLogin('auth.loginRequired');
    else if (needsAdmin) promptLogin('auth.adminRequired');
  }, [needsLogin, needsAdmin, promptLogin]);

  if (needsLogin || needsAdmin) {
    return <Navigate to="/" replace />;
  }

  if (requireVerified && !isVerified) {
    return (
      <PageWrapper>
        <Box sx={{ textAlign: 'center', py: 8 }}>
          <Typography variant="h5" sx={{ fontWeight: 700, mb: 2 }}>
            {t('auth.verificationRequired')}
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            {t('auth.verifyEmailMessage')}
          </Typography>
          <Button variant="contained" href="/profile">
            {t('nav.profile')}
          </Button>
        </Box>
      </PageWrapper>
    );
  }

  return <>{children}</>;
}
