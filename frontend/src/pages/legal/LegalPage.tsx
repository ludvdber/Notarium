import { Typography, Box, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';
import type { LegalPage as LegalPageContent, Locale } from './legalContent';
import { LEGAL_UPDATED_AT } from './legalContent';

interface Props {
  getContent: (locale: Locale) => LegalPageContent;
}

/**
 * Generic renderer for /legal, /privacy and /terms. Each concrete page passes
 * its own content getter from legalContent.ts.
 */
export default function LegalPage({ getContent }: Props) {
  const { i18n } = useTranslation();
  const locale: Locale = i18n.language.startsWith('fr') ? 'fr' : 'en';
  const content = getContent(locale);

  return (
    <PageWrapper maxWidth="md">
      <Typography variant="h3" sx={{ fontWeight: 800, mb: 1 }}>
        {content.title}
      </Typography>
      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 4 }}>
        {content.updatedLabel} : {LEGAL_UPDATED_AT}
      </Typography>

      <Stack spacing={2}>
        {content.sections.map((section) => (
          <GlassCard key={section.heading} sx={{ p: { xs: 2.5, md: 3.5 } }}>
            <Typography variant="h5" sx={{ fontWeight: 700, mb: 1.5, color: 'primary.main' }}>
              {section.heading}
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              {section.body.map((p, i) => (
                <Typography key={i} variant="body2" sx={{ lineHeight: 1.7 }}>
                  {p}
                </Typography>
              ))}
            </Box>
          </GlassCard>
        ))}
      </Stack>
    </PageWrapper>
  );
}
