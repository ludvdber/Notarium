import { Navigate } from 'react-router-dom';
import { Typography, Box, Button } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '@/stores/useAuthStore';
import PageWrapper from '@/components/layout/PageWrapper';

interface Props {
  requireVerified?: boolean;
  requireAdmin?: boolean;
  children: React.ReactNode;
}

export default function ProtectedRoute({ requireVerified, requireAdmin, children }: Props) {
  const { t } = useTranslation();
  const { token, isVerified, isAdmin } = useAuthStore();

  if (!token) {
    return <Navigate to="/" replace />;
  }

  if (requireAdmin && !isAdmin) {
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
