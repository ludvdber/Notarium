import { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Chip,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Tooltip,
  Alert,
} from '@mui/material';
import { CheckCircle, Edit, Delete, Save, Close } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  getPendingDocuments,
  verifyDocument,
  adminUpdateDocument,
  adminDeleteDocument,
  searchDocuments,
} from '@/api/endpoints';
import { formatDate } from '@/lib/utils';
import { CATEGORIES } from '@/lib/constants';
import GlassCard from '@/components/ui/GlassCard';
import type { DocumentResponse, UpdateDocumentRequest } from '@/types';

export default function AdminDocuments() {
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState<UpdateDocumentRequest>({});

  const { data: pendingDocs } = useQuery({
    queryKey: ['admin-pending-docs'],
    queryFn: getPendingDocuments,
  });

  const { data: allDocs, isLoading } = useQuery({
    queryKey: ['admin-all-docs', searchQuery],
    queryFn: () => searchDocuments({ q: searchQuery || undefined, page: 0, size: 50 }),
  });

  const invalidateAll = () => {
    queryClient.invalidateQueries({ queryKey: ['admin-pending-docs'] });
    queryClient.invalidateQueries({ queryKey: ['admin-all-docs'] });
  };

  const verifyMut = useMutation({ mutationFn: verifyDocument, onSuccess: invalidateAll });

  const updateMut = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateDocumentRequest }) =>
      adminUpdateDocument(id, data),
    onSuccess: () => {
      invalidateAll();
      setEditingId(null);
    },
  });

  const deleteMut = useMutation({
    mutationFn: adminDeleteDocument,
    onSuccess: invalidateAll,
  });

  const startEdit = (doc: DocumentResponse) => {
    setEditingId(doc.id);
    setEditForm({
      title: doc.title,
      category: doc.category,
      language: doc.language,
      year: doc.year ?? '',
      verified: doc.verified,
      tags: [...doc.tags],
    });
  };

  const pendingCount = pendingDocs?.length ?? 0;

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      {/* Pending section */}
      {pendingCount > 0 && (
        <GlassCard sx={{ p: 2.5 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2 }}>
            {t('admin.docs.pending')} ({pendingCount})
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {pendingDocs!.map((doc) => (
              <Box
                key={doc.id}
                sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 1.5, borderRadius: 1.5, bgcolor: 'rgba(255,255,255,0.02)', flexWrap: 'wrap' }}
              >
                <Box sx={{ flex: 1, minWidth: 200 }}>
                  <Typography variant="body2" sx={{ fontWeight: 700 }}>{doc.title}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {doc.courseName} — {doc.authorName} — {formatDate(doc.createdAt, i18n.language)}
                  </Typography>
                </Box>
                <Button size="small" variant="contained" color="success" startIcon={<CheckCircle />}
                  onClick={() => verifyMut.mutate(doc.id)} disabled={verifyMut.isPending}
                >
                  {t('admin.docs.verify')}
                </Button>
              </Box>
            ))}
          </Box>
        </GlassCard>
      )}

      {/* All documents search */}
      <Box>
        <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
          {t('admin.docs.all')}
        </Typography>
        <TextField
          size="small"
          fullWidth
          placeholder={t('search.placeholder')}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          sx={{ mb: 2 }}
        />
      </Box>

      {isLoading && <Typography color="text.secondary">{t('common.loading')}</Typography>}

      {updateMut.isError && (
        <Alert severity="error">{(updateMut.error as Error).message || t('common.error')}</Alert>
      )}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {allDocs?.content.map((doc) => (
          <GlassCard key={doc.id} sx={{ p: 2 }}>
            {editingId === doc.id ? (
              <EditRow
                form={editForm}
                onChange={setEditForm}
                onSave={() => updateMut.mutate({ id: doc.id, data: editForm })}
                onCancel={() => setEditingId(null)}
                isPending={updateMut.isPending}
                t={t}
              />
            ) : (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
                <Box sx={{ flex: 1, minWidth: 200 }}>
                  <Typography variant="body2" sx={{ fontWeight: 700 }}>{doc.title}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    #{doc.id} — {doc.courseName} — {doc.authorName} — {formatDate(doc.createdAt, i18n.language)}
                  </Typography>
                </Box>
                <Chip label={t(`categories.${doc.category}`)} size="small" variant="outlined" />
                {doc.verified && <Chip label={t('document.verified')} size="small" color="primary" variant="outlined" />}
                <Box sx={{ display: 'flex', gap: 0.5 }}>
                  <Tooltip title={t('admin.docs.edit')}>
                    <IconButton size="small" onClick={() => startEdit(doc)}>
                      <Edit fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title={t('document.delete')}>
                    <IconButton size="small" color="error"
                      onClick={() => { if (confirm(t('admin.docs.deleteConfirm'))) deleteMut.mutate(doc.id); }}
                    >
                      <Delete fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </Box>
              </Box>
            )}
          </GlassCard>
        ))}
      </Box>
    </Box>
  );
}

interface EditRowProps {
  form: UpdateDocumentRequest;
  onChange: (f: UpdateDocumentRequest) => void;
  onSave: () => void;
  onCancel: () => void;
  isPending: boolean;
  t: (key: string) => string;
}

function EditRow({ form, onChange, onSave, onCancel, isPending, t }: EditRowProps) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <TextField
          label={t('document.title')}
          size="small"
          value={form.title ?? ''}
          onChange={(e) => onChange({ ...form, title: e.target.value })}
          sx={{ flex: 2, minWidth: 200 }}
        />
        <FormControl size="small" sx={{ flex: 1, minWidth: 120 }}>
          <InputLabel>{t('document.category')}</InputLabel>
          <Select
            value={form.category ?? ''}
            label={t('document.category')}
            onChange={(e) => onChange({ ...form, category: e.target.value })}
          >
            {CATEGORIES.map((c) => (
              <MenuItem key={c} value={c}>{t(`categories.${c}`)}</MenuItem>
            ))}
          </Select>
        </FormControl>
        <TextField
          label={t('document.language')}
          size="small"
          value={form.language ?? ''}
          onChange={(e) => onChange({ ...form, language: e.target.value })}
          sx={{ width: 80 }}
        />
        <TextField
          label={t('document.year')}
          size="small"
          value={form.year ?? ''}
          onChange={(e) => onChange({ ...form, year: e.target.value })}
          sx={{ width: 100 }}
        />
      </Box>
      <TextField
        label={t('document.tags')}
        size="small"
        fullWidth
        value={form.tags?.join(', ') ?? ''}
        onChange={(e) =>
          onChange({ ...form, tags: e.target.value.split(',').map((s) => s.trim()).filter(Boolean) })
        }
        helperText={t('upload.tagsHelper')}
      />
      <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
        <Button size="small" onClick={onCancel} startIcon={<Close />}>{t('common.cancel')}</Button>
        <Button size="small" variant="contained" onClick={onSave} startIcon={<Save />} disabled={isPending}>
          {isPending ? t('common.loading') : t('common.save')}
        </Button>
      </Box>
    </Box>
  );
}
