import { Typography, List, ListItemButton, ListItemText, Chip, Box } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { getNews } from '@/api/endpoints';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';

export default function News() {
  const { t } = useTranslation();
  const { data: news } = useQuery({ queryKey: ['news'], queryFn: getNews });

  return (
    <PageWrapper>
      <Helmet><title>{t('nav.news')} — Freenote</title></Helmet>
      <Typography variant="h4" sx={{ fontWeight: 700, mb: 3 }}>{t('news.title')}</Typography>
      <GlassCard>
        <List>
          {news?.map((item, i) => (
            <ListItemButton key={i} component="a" href={item.url ?? '#'} target="_blank" rel="noopener noreferrer">
              <ListItemText primary={item.title} secondary={item.date} />
              <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                {item.labels.map((label) => <Chip key={label} label={label} size="small" variant="outlined" />)}
              </Box>
            </ListItemButton>
          ))}
        </List>
      </GlassCard>
    </PageWrapper>
  );
}
