import { useMemo, useState } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  IconButton,
  Chip,
  Tooltip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
} from '@mui/material';
import { Add, Edit, Delete, CheckCircle } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  adminListCourses,
  adminCreateCourse,
  adminRenameCourse,
  adminDeleteCourse,
  approveCourse,
  getSections,
} from '@/api/endpoints';
import { STALE_15M } from '@/lib/constants';
import GlassCard from '@/components/ui/GlassCard';
import type { Course } from '@/types';

export default function AdminCourses() {
  const { t } = useTranslation();
  const qc = useQueryClient();

  const { data: courses, isLoading } = useQuery({
    queryKey: ['admin-courses'],
    queryFn: adminListCourses,
  });
  const { data: sections } = useQuery({
    queryKey: ['sections'],
    queryFn: getSections,
    staleTime: STALE_15M,
  });

  const [createName, setCreateName] = useState('');
  const [createSectionId, setCreateSectionId] = useState<number | ''>('');
  const [filterSectionId, setFilterSectionId] = useState<number | 'all'>('all');
  const [filterStatus, setFilterStatus] = useState<'all' | 'pending' | 'approved'>('all');
  const [editTarget, setEditTarget] = useState<Course | null>(null);
  const [editName, setEditName] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<Course | null>(null);
  const [error, setError] = useState('');

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['admin-courses'] });
    qc.invalidateQueries({ queryKey: ['courses'] });
  };

  const filtered = useMemo(() => {
    if (!courses) return [];
    return courses.filter((c) => {
      if (filterSectionId !== 'all' && c.sectionId !== filterSectionId) return false;
      if (filterStatus === 'pending' && c.approved) return false;
      if (filterStatus === 'approved' && !c.approved) return false;
      return true;
    });
  }, [courses, filterSectionId, filterStatus]);

  const createMut = useMutation({
    mutationFn: () => adminCreateCourse({ name: createName.trim(), sectionId: createSectionId as number }),
    onSuccess: () => {
      setCreateName('');
      setError('');
      invalidate();
    },
    onError: (e: unknown) => setError(extractError(e)),
  });

  const renameMut = useMutation({
    mutationFn: () => adminRenameCourse(editTarget!.id, editName.trim()),
    onSuccess: () => {
      setEditTarget(null);
      setError('');
      invalidate();
    },
    onError: (e: unknown) => setError(extractError(e)),
  });

  const deleteMut = useMutation({
    mutationFn: () => adminDeleteCourse(deleteTarget!.id),
    onSuccess: () => {
      setDeleteTarget(null);
      invalidate();
    },
  });

  const approveMut = useMutation({
    mutationFn: approveCourse,
    onSuccess: invalidate,
  });

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h6" sx={{ fontWeight: 700 }}>
        {t('admin.courses.title')} ({filtered.length})
      </Typography>

      {error && <Alert severity="error" onClose={() => setError('')}>{error}</Alert>}

      <GlassCard sx={{ p: 2, display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField
          size="small"
          label={t('admin.courses.name')}
          value={createName}
          onChange={(e) => setCreateName(e.target.value)}
          sx={{ flex: '1 1 200px' }}
        />
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>{t('admin.courses.section')}</InputLabel>
          <Select
            value={createSectionId}
            label={t('admin.courses.section')}
            onChange={(e) => setCreateSectionId(e.target.value as number)}
          >
            {sections?.map((sec) => (
              <MenuItem key={sec.id} value={sec.id}>{sec.name}</MenuItem>
            ))}
          </Select>
        </FormControl>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => createMut.mutate()}
          disabled={!createName.trim() || createSectionId === '' || createMut.isPending}
        >
          {t('admin.courses.create')}
        </Button>
      </GlassCard>

      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>{t('admin.courses.filterSection')}</InputLabel>
          <Select
            value={filterSectionId}
            label={t('admin.courses.filterSection')}
            onChange={(e) => setFilterSectionId(e.target.value as number | 'all')}
          >
            <MenuItem value="all">{t('admin.courses.allSections')}</MenuItem>
            {sections?.map((sec) => (
              <MenuItem key={sec.id} value={sec.id}>{sec.name}</MenuItem>
            ))}
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 150 }}>
          <InputLabel>{t('admin.courses.filterStatus')}</InputLabel>
          <Select
            value={filterStatus}
            label={t('admin.courses.filterStatus')}
            onChange={(e) => setFilterStatus(e.target.value as 'all' | 'pending' | 'approved')}
          >
            <MenuItem value="all">{t('admin.courses.all')}</MenuItem>
            <MenuItem value="pending">{t('admin.courses.pending')}</MenuItem>
            <MenuItem value="approved">{t('admin.courses.approved')}</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {isLoading && <Typography color="text.secondary">{t('common.loading')}</Typography>}

      {filtered.map((c) => (
        <GlassCard key={c.id} sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box sx={{ flex: 1 }}>
            <Typography variant="body2" sx={{ fontWeight: 700 }}>{c.name}</Typography>
            <Typography variant="caption" color="text.secondary">
              {c.sectionName} · {t('admin.courses.docCount', { count: c.documentCount })}
            </Typography>
          </Box>
          {!c.approved && <Chip size="small" color="warning" label={t('admin.courses.pending')} />}
          {!c.approved && (
            <Tooltip title={t('admin.courses.approve')}>
              <IconButton size="small" color="success" onClick={() => approveMut.mutate(c.id)}>
                <CheckCircle fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          <Tooltip title={t('admin.courses.rename')}>
            <IconButton size="small" onClick={() => { setEditTarget(c); setEditName(c.name); }}>
              <Edit fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title={t('admin.courses.delete')}>
            <IconButton size="small" color="error" onClick={() => setDeleteTarget(c)}>
              <Delete fontSize="small" />
            </IconButton>
          </Tooltip>
        </GlassCard>
      ))}

      <Dialog open={Boolean(editTarget)} onClose={() => setEditTarget(null)}>
        <DialogTitle>{t('admin.courses.renameTitle')}</DialogTitle>
        <DialogContent sx={{ minWidth: 320, pt: 1 }}>
          <TextField
            autoFocus
            fullWidth
            label={t('admin.courses.name')}
            value={editName}
            onChange={(e) => setEditName(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditTarget(null)}>{t('common.cancel')}</Button>
          <Button variant="contained" onClick={() => renameMut.mutate()} disabled={!editName.trim() || renameMut.isPending}>
            {t('common.save')}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={Boolean(deleteTarget)} onClose={() => setDeleteTarget(null)}>
        <DialogTitle>{t('admin.courses.deleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>
            {t('admin.courses.deleteConfirm', { name: deleteTarget?.name, count: deleteTarget?.documentCount })}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteTarget(null)}>{t('common.cancel')}</Button>
          <Button color="error" variant="contained" onClick={() => deleteMut.mutate()} disabled={deleteMut.isPending}>
            {t('admin.courses.delete')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

function extractError(e: unknown): string {
  if (typeof e === 'object' && e !== null) {
    const err = e as { response?: { data?: { message?: string } } };
    return err.response?.data?.message ?? 'Error';
  }
  return 'Error';
}
