import { useState } from 'react';
import { Typography, Grid, Box, FormControl, InputLabel, Select, MenuItem, Pagination } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { searchDocuments, getSections, getCourses } from '@/api/endpoints';
import { useDebounce } from '@/hooks/useDebounce';
import { CATEGORIES } from '@/lib/constants';
import PageWrapper from '@/components/layout/PageWrapper';
import SearchBar from '@/components/ui/SearchBar';
import DocumentCard from '@/components/common/DocumentCard';
import Shimmer from '@/components/ui/Shimmer';
import AdBanner from '@/components/ui/AdBanner';
import * as s from './Browse.styles';

export default function Browse() {
  const { t } = useTranslation();
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('');
  const [sectionId, setSectionId] = useState<number | ''>('');
  const [courseId, setCourseId] = useState<number | ''>('');
  const [page, setPage] = useState(0);
  const debouncedQuery = useDebounce(query, 400);

  const { data: sections } = useQuery({ queryKey: ['sections'], queryFn: getSections });
  const { data: courses } = useQuery({
    queryKey: ['courses', sectionId],
    queryFn: () => getCourses(sectionId as number),
    enabled: sectionId !== '',
  });

  const { data, isLoading } = useQuery({
    queryKey: ['search', debouncedQuery, courseId, category, page],
    queryFn: () =>
      searchDocuments({
        q: debouncedQuery || undefined,
        courseId: courseId || undefined,
        category: category || undefined,
        page,
        size: 18,
      }),
  });

  const handleFilterChange = () => setPage(0);

  return (
    <PageWrapper>
      <Helmet><title>{t('nav.browse')} — Freenote</title></Helmet>
      <Typography variant="h4" sx={s.title}>
        {t('nav.browse')}
      </Typography>

      <Box sx={s.filtersRow}>
        <Box sx={s.searchCol}>
          <SearchBar
            value={query}
            onChange={(v) => {
              setQuery(v);
              handleFilterChange();
            }}
          />
        </Box>
        <FormControl size="small" sx={s.filterControl}>
          <InputLabel>{t('document.section')}</InputLabel>
          <Select
            value={sectionId}
            label={t('document.section')}
            onChange={(e) => {
              setSectionId(e.target.value as number);
              setCourseId('');
              handleFilterChange();
            }}
          >
            <MenuItem value="">{t('common.seeAll')}</MenuItem>
            {sections?.map((sec) => (
              <MenuItem key={sec.id} value={sec.id}>
                {sec.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        {sectionId !== '' && (
          <FormControl size="small" sx={s.filterControl}>
            <InputLabel>{t('document.course')}</InputLabel>
            <Select
              value={courseId}
              label={t('document.course')}
              onChange={(e) => {
                setCourseId(e.target.value as number);
                handleFilterChange();
              }}
            >
              <MenuItem value="">{t('common.seeAll')}</MenuItem>
              {courses?.map((c) => (
                <MenuItem key={c.id} value={c.id}>
                  {c.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}
        <FormControl size="small" sx={s.filterControl}>
          <InputLabel>{t('document.category')}</InputLabel>
          <Select
            value={category}
            label={t('document.category')}
            onChange={(e) => {
              setCategory(e.target.value);
              handleFilterChange();
            }}
          >
            <MenuItem value="">{t('common.seeAll')}</MenuItem>
            {CATEGORIES.map((c) => (
              <MenuItem key={c} value={c}>
                {t(`categories.${c}`)}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
        <AdBanner width={728} height={90} />
      </Box>

      {isLoading ? (
        <Shimmer count={6} />
      ) : data?.content.length ? (
        <>
          <Grid container spacing={2}>
            {data.content.map((doc) => (
              <Grid key={doc.id} size={{ xs: 12, sm: 6, md: 4 }}>
                <DocumentCard document={doc} />
              </Grid>
            ))}
          </Grid>

          {data.totalPages > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
              <Pagination
                count={data.totalPages}
                page={page + 1}
                onChange={(_, p) => {
                  setPage(p - 1);
                  window.scrollTo({ top: 0, behavior: 'smooth' });
                }}
                color="primary"
                shape="rounded"
              />
            </Box>
          )}
        </>
      ) : (
        <Typography color="text.secondary" sx={s.emptyText}>
          {t('document.noResults')}
        </Typography>
      )}
    </PageWrapper>
  );
}
