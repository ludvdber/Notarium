import { Box, Typography, Button } from '@mui/material';
import { CheckCircle } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { getPendingProfessors, approveProfessor } from '@/api/endpoints';
import GlassCard from '@/components/ui/GlassCard';

export default function AdminProfessors() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const { data: profs, isLoading } = useQuery({ queryKey: ['admin-pending-profs'], queryFn: getPendingProfessors });

  const approveMut = useMutation({
    mutationFn: approveProfessor,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-pending-profs'] }),
  });

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h6" sx={{ fontWeight: 700 }}>
        {t('admin.profs.pending')} ({profs?.length ?? 0})
      </Typography>

      {isLoading && <Typography color="text.secondary">{t('common.loading')}</Typography>}
      {!isLoading && !profs?.length && (
        <GlassCard sx={{ p: 3, textAlign: 'center' }}>
          <Typography color="text.secondary">{t('admin.profs.noPending')}</Typography>
        </GlassCard>
      )}

      {profs?.map((prof) => (
        <GlassCard key={prof.id} sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography variant="body2" sx={{ fontWeight: 700, flex: 1 }}>{prof.name}</Typography>
          <Button
            variant="contained"
            size="small"
            color="success"
            startIcon={<CheckCircle />}
            onClick={() => approveMut.mutate(prof.id)}
            disabled={approveMut.isPending}
          >
            {t('admin.profs.approve')}
          </Button>
        </GlassCard>
      ))}
    </Box>
  );
}
