import { useState, type ChangeEvent } from 'react';
import {
  Typography,
  TextField,
  Button,
  Box,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Alert,
} from '@mui/material';
import { CloudUpload } from '@mui/icons-material';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { uploadDocument, getSections, getCourses, getProfessors } from '@/api/endpoints';
import { CATEGORIES, MAX_FILE_SIZE } from '@/lib/constants';
import PageWrapper from '@/components/layout/PageWrapper';
import * as s from './Upload.styles';

export default function Upload() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [sectionId, setSectionId] = useState<number | ''>('');
  const [courseId, setCourseId] = useState<number | ''>('');
  const [category, setCategory] = useState('');
  const [year, setYear] = useState('');
  const [professorId, setProfessorId] = useState<number | ''>('');
  const [language, setLanguage] = useState('FR');
  const [anonymous, setAnonymous] = useState(false);
  const [aiGenerated, setAiGenerated] = useState(false);
  const [tags, setTags] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState('');

  const { data: sections } = useQuery({ queryKey: ['sections'], queryFn: getSections });
  const { data: courses } = useQuery({
    queryKey: ['courses', sectionId],
    queryFn: () => getCourses(sectionId as number),
    enabled: sectionId !== '',
  });
  const { data: professors } = useQuery({ queryKey: ['professors'], queryFn: getProfessors });

  const mutation = useMutation({
    mutationFn: () =>
      uploadDocument(
        {
          title,
          courseId: courseId as number,
          category,
          year: year || undefined,
          professorId: professorId || undefined,
          language,
          aiGenerated,
          anonymous,
          tags: tags ? tags.split(',').map((x) => x.trim()) : undefined,
        },
        file!
      ),
    onSuccess: (doc) => navigate(`/documents/${doc.id}`),
    onError: () => setError(t('common.error')),
  });

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (!f) return;
    if (f.type !== 'application/pdf') {
      setError(t('upload.pdfOnly'));
      return;
    }
    if (f.size > MAX_FILE_SIZE) {
      setError(t('upload.maxSize'));
      return;
    }
    setFile(f);
    setError('');
  };

  const canSubmit = title && courseId && category && file;

  return (
    <PageWrapper maxWidth="sm">
      <Typography variant="h4" sx={s.title}>
        {t('upload.title')}
      </Typography>

      {error && (
        <Alert severity="error" sx={s.errorAlert}>
          {error}
        </Alert>
      )}

      <Box sx={s.form}>
        <TextField label={t('document.title')} value={title} onChange={(e) => setTitle(e.target.value)} required />

        <FormControl required>
          <InputLabel>{t('document.section')}</InputLabel>
          <Select
            value={sectionId}
            label={t('document.section')}
            onChange={(e) => {
              setSectionId(e.target.value as number);
              setCourseId('');
            }}
          >
            {sections?.map((sec) => (
              <MenuItem key={sec.id} value={sec.id}>
                {sec.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {sectionId !== '' && (
          <FormControl required>
            <InputLabel>{t('document.course')}</InputLabel>
            <Select
              value={courseId}
              label={t('document.course')}
              onChange={(e) => setCourseId(e.target.value as number)}
            >
              {courses?.map((c) => (
                <MenuItem key={c.id} value={c.id}>
                  {c.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}

        <FormControl required>
          <InputLabel>{t('document.category')}</InputLabel>
          <Select value={category} label={t('document.category')} onChange={(e) => setCategory(e.target.value)}>
            {CATEGORIES.map((c) => (
              <MenuItem key={c} value={c}>
                {t(`categories.${c}`)}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <TextField label={t('document.year')} value={year} onChange={(e) => setYear(e.target.value)} />

        <FormControl>
          <InputLabel>{t('document.professor')}</InputLabel>
          <Select
            value={professorId}
            label={t('document.professor')}
            onChange={(e) => setProfessorId(e.target.value as number)}
          >
            <MenuItem value="">—</MenuItem>
            {professors?.map((p) => (
              <MenuItem key={p.id} value={p.id}>
                {p.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <TextField label={t('document.language')} value={language} onChange={(e) => setLanguage(e.target.value)} />
        <TextField
          label={t('document.tags')}
          value={tags}
          onChange={(e) => setTags(e.target.value)}
          helperText="tag1, tag2, tag3"
        />

        <FormControlLabel
          control={<Checkbox checked={anonymous} onChange={(e) => setAnonymous(e.target.checked)} />}
          label={t('document.anonymous')}
        />
        <FormControlLabel
          control={<Checkbox checked={aiGenerated} onChange={(e) => setAiGenerated(e.target.checked)} />}
          label={t('document.aiGenerated')}
        />

        <Button variant="outlined" component="label" startIcon={<CloudUpload />}>
          {file ? file.name : t('upload.dragDrop')}
          <input type="file" accept="application/pdf" hidden onChange={handleFileChange} />
        </Button>

        <Button
          variant="contained"
          size="large"
          onClick={() => mutation.mutate()}
          disabled={!canSubmit || mutation.isPending}
        >
          {mutation.isPending ? t('common.loading') : t('upload.submit')}
        </Button>
      </Box>
    </PageWrapper>
  );
}
