import { Box } from '@mui/material';

export default function Divider() {
  return (
    <Box
      sx={{
        height: '1px',
        background: (t) =>
          t.palette.mode === 'dark'
            ? 'linear-gradient(90deg, transparent, rgba(255,255,255,0.08), transparent)'
            : 'linear-gradient(90deg, transparent, rgba(0,0,0,0.08), transparent)',
        mx: { xs: 2, md: '60px' },
        my: 0,
      }}
    />
  );
}
