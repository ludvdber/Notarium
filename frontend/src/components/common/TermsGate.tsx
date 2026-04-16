import { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Checkbox,
  FormControlLabel,
  Alert,
} from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { acceptTerms, getCurrentUser } from '@/api/endpoints';
import { useAuthStore } from '@/stores/useAuthStore';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';

export default function TermsGate({ children }: { children: React.ReactNode }) {
  const { t } = useTranslation();
  const { user, token, setUser } = useAuthStore();
  const queryClient = useQueryClient();
  const [checked, setChecked] = useState(false);

  const mutation = useMutation({
    mutationFn: async () => {
      await acceptTerms();
      const updated = await getCurrentUser();
      setUser(updated);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['me'] });
    },
  });

  if (!token || !user || user.termsAccepted) {
    return <>{children}</>;
  }

  return (
    <PageWrapper maxWidth="sm">
      <Box sx={{ py: { xs: 4, md: 8 } }}>
        <GlassCard sx={{ p: { xs: 3, md: 4 } }}>
          <Typography variant="h5" sx={{ fontWeight: 800, mb: 2 }}>
            {t('terms.gateTitle')}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3, lineHeight: 1.7 }}>
            {t('terms.gateSubtitle')}
          </Typography>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, mb: 3 }}>
            <Typography
              component={RouterLink}
              to="/terms"
              target="_blank"
              variant="body2"
              sx={{ color: 'primary.main', textDecoration: 'underline' }}
            >
              {t('footer.terms')}
            </Typography>
            <Typography
              component={RouterLink}
              to="/privacy"
              target="_blank"
              variant="body2"
              sx={{ color: 'primary.main', textDecoration: 'underline' }}
            >
              {t('footer.privacy')}
            </Typography>
          </Box>

          <Alert severity="warning" sx={{ mb: 2 }}>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {t('terms.copyrightWarning')}
            </Typography>
          </Alert>

          <FormControlLabel
            control={
              <Checkbox
                checked={checked}
                onChange={(e) => setChecked(e.target.checked)}
                color="primary"
              />
            }
            label={t('terms.gateCheckbox')}
            sx={{ mb: 2 }}
          />

          {mutation.isError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {t('common.error')}
            </Alert>
          )}

          <Button
            variant="contained"
            fullWidth
            size="large"
            disabled={!checked || mutation.isPending}
            onClick={() => mutation.mutate()}
          >
            {mutation.isPending ? t('common.loading') : t('terms.gateAccept')}
          </Button>
        </GlassCard>
      </Box>
    </PageWrapper>
  );
}
