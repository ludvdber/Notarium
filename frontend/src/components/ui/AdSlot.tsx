import { Box } from '@mui/material';
import type { SxProps, Theme } from '@mui/material';
import { useAuthStore } from '@/stores/useAuthStore';
import AdBanner from './AdBanner';

interface AdSlotProps {
  width?: number;
  height?: number;
  /** Margin / layout styles applied to the wrapper. Ignored entirely when ad-free so the
   *  surrounding layout collapses instead of keeping spacer margins. */
  sx?: SxProps<Theme>;
}

/**
 * Full container + banner. For Ko-fi supporters the whole slot (including its margins)
 * vanishes, so no reserved empty space remains in the page flow.
 */
export default function AdSlot({ width, height, sx }: AdSlotProps) {
  const { user } = useAuthStore();
  if (user?.supporter) return null;

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', ...sx }}>
      <AdBanner width={width} height={height} />
    </Box>
  );
}
