import { Box, Typography, Fade } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '@/stores/useAuthStore';

interface AdBannerProps {
  width?: number;
  height?: number;
}

export default function AdBanner({ width = 728, height = 90 }: AdBannerProps) {
  const { t } = useTranslation();
  const user = useAuthStore((s) => s.user);

  // Reserve space to avoid CLS even when hidden for supporters
  const minH = height + 24; // banner + disclaimer

  if (user?.supporter) return <Box sx={{ minHeight: minH }} />;

  return (
    <Fade in timeout={600}>
      <Box sx={{ textAlign: 'center', minHeight: minH }}>
        <Box
          sx={{
            width: { xs: '100%', md: width },
            height,
            mx: 'auto',
            borderRadius: 2,
            background: (th) => th.palette.mode === 'dark'
              ? 'rgba(123, 47, 247, 0.08)'
              : 'rgba(123, 47, 247, 0.05)',
            border: (th) => `1px dashed ${th.palette.mode === 'dark' ? 'rgba(123, 47, 247, 0.25)' : 'rgba(123, 47, 247, 0.2)'}`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Typography variant="body2" color="text.secondary" sx={{ opacity: 0.6 }}>
            Ad {width}x{height}
          </Typography>
        </Box>
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block', opacity: 0.5, fontSize: 10 }}>
          {t('ad.disclaimer')}
        </Typography>
      </Box>
    </Fade>
  );
}
