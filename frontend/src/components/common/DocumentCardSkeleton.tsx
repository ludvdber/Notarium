import { Skeleton, Box, CardContent } from '@mui/material';
import GlassCard from '@/components/ui/GlassCard';

export default function DocumentCardSkeleton() {
  return (
    <GlassCard sx={{ height: '100%' }}>
      <CardContent sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, p: 2 }}>
        <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
          <Skeleton variant="rounded" width={28} height={32} sx={{ bgcolor: 'rgba(255,255,255,0.06)' }} />
          <Box sx={{ flex: 1 }}>
            <Skeleton variant="text" width="85%" height={18} sx={{ bgcolor: 'rgba(255,255,255,0.06)' }} />
            <Skeleton variant="text" width="60%" height={14} sx={{ bgcolor: 'rgba(255,255,255,0.04)' }} />
          </Box>
          <Skeleton variant="circular" width={24} height={24} sx={{ bgcolor: 'rgba(255,255,255,0.05)' }} />
        </Box>
        <Box sx={{ display: 'flex', gap: 0.75 }}>
          <Skeleton variant="rounded" width={60} height={20} sx={{ borderRadius: 10, bgcolor: 'rgba(255,255,255,0.05)' }} />
          <Skeleton variant="rounded" width={72} height={20} sx={{ borderRadius: 10, bgcolor: 'rgba(255,255,255,0.05)' }} />
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 0.5 }}>
          <Skeleton variant="text" width={80} height={16} sx={{ bgcolor: 'rgba(255,255,255,0.05)' }} />
          <Skeleton variant="text" width={40} height={16} sx={{ bgcolor: 'rgba(255,255,255,0.05)' }} />
        </Box>
      </CardContent>
    </GlassCard>
  );
}
