import { Box, Typography, Button } from '@mui/material';
import { CheckCircle } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { getPendingCourses, approveCourse } from '@/api/endpoints';
import GlassCard from '@/components/ui/GlassCard';

export default function AdminCourses() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const { data: courses, isLoading } = useQuery({ queryKey: ['admin-pending-courses'], queryFn: getPendingCourses });

  const approveMut = useMutation({
    mutationFn: approveCourse,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-pending-courses'] }),
  });

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h6" sx={{ fontWeight: 700 }}>
        {t('admin.courses.pending')} ({courses?.length ?? 0})
      </Typography>

      {isLoading && <Typography color="text.secondary">{t('common.loading')}</Typography>}
      {!isLoading && !courses?.length && (
        <GlassCard sx={{ p: 3, textAlign: 'center' }}>
          <Typography color="text.secondary">{t('admin.courses.noPending')}</Typography>
        </GlassCard>
      )}

      {courses?.map((course) => (
        <GlassCard key={course.id} sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box sx={{ flex: 1 }}>
            <Typography variant="body2" sx={{ fontWeight: 700 }}>{course.name}</Typography>
            <Typography variant="caption" color="text.secondary">{course.sectionName}</Typography>
          </Box>
          <Button
            variant="contained"
            size="small"
            color="success"
            startIcon={<CheckCircle />}
            onClick={() => approveMut.mutate(course.id)}
            disabled={approveMut.isPending}
          >
            {t('admin.courses.approve')}
          </Button>
        </GlassCard>
      ))}
    </Box>
  );
}
