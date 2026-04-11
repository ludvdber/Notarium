import { Box, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';

interface AdBannerProps {
  width?: number;
  height?: number;
}

export default function AdBanner({ width = 728, height = 90 }: AdBannerProps) {
  const { t } = useTranslation();

  return (
    <Box sx={{ textAlign: 'center' }}>
      <Box
        sx={{
          width: { xs: '100%', md: width },
          height,
          mx: 'auto',
          borderRadius: 2,
          background: 'rgba(123, 47, 247, 0.08)',
          border: '1px dashed rgba(123, 47, 247, 0.25)',
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
  );
}
