import { Typography, Grid } from '@mui/material';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { searchDocuments } from '@/api/endpoints';
import PageWrapper from '@/components/layout/PageWrapper';
import DocumentCard from '@/components/common/DocumentCard';
import Shimmer from '@/components/ui/Shimmer';

export default function CoursePage() {
  const { t } = useTranslation();
  const { courseId } = useParams<{ courseId: string }>();

  const { data, isLoading } = useQuery({
    queryKey: ['course-docs', courseId],
    queryFn: () => searchDocuments({ courseId: Number(courseId), page: 0, size: 50 }),
    enabled: !!courseId,
  });

  return (
    <PageWrapper>
      <Typography variant="h4" sx={{ fontWeight: 700, mb: 3 }}>
        {t('document.courseDocuments')}
      </Typography>

      {isLoading ? (
        <Shimmer count={6} />
      ) : (
        <Grid container spacing={2}>
          {data?.content.map((doc) => (
            <Grid key={doc.id} size={{ xs: 12, sm: 6, md: 4 }}>
              <DocumentCard document={doc} />
            </Grid>
          ))}
        </Grid>
      )}
    </PageWrapper>
  );
}
