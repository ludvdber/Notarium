import { Box, Skeleton } from '@mui/material';

interface ShimmerProps {
  count?: number;
  height?: number;
}

export default function Shimmer({ count = 3, height = 120 }: ShimmerProps) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton
          key={i}
          variant="rounded"
          height={height}
          sx={{ borderRadius: 2, bgcolor: 'rgba(255,255,255,0.05)' }}
        />
      ))}
    </Box>
  );
}
