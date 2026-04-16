import { Box, Typography } from '@mui/material';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { getSections } from '@/api/endpoints';
import GlassCard from '@/components/ui/GlassCard';
import { FALLBACK_SECTIONS, type SectionItem } from './SectionsGrid.data';
import * as s from './SectionsGrid.styles';

export default function SectionsGrid() {
  const { t } = useTranslation();
  const { data: sections } = useQuery({ queryKey: ['sections'], queryFn: getSections });

  const items: SectionItem[] = sections?.length
    ? sections.map((sec, i) => {
        const fb = FALLBACK_SECTIONS[i % FALLBACK_SECTIONS.length];
        return {
          id: String(sec.id),
          name: sec.name,
          // Always use emoji from fallback — prototype requires emoji icons, not MUI icon names
          icon: fb.icon,
          color: fb.color,
          count: sec.documentCount,
        };
      })
    : FALLBACK_SECTIONS.map((sec) => ({ ...sec, count: 0 }));

  return (
    <motion.section initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <Box sx={s.section}>
        <Box sx={s.header}>
          <Typography variant="h5" sx={s.title}>
            <span role="img" aria-label="">🎓</span> {t('sections.title')}
          </Typography>
          <Box component={Link} to="/browse" sx={s.viewAllLink}>
            {t('sections.viewAll')} →
          </Box>
        </Box>

        <Box sx={s.grid}>
          {items.map((section, i) => (
            <motion.div
              key={section.id}
              initial={{ opacity: 0, scale: 0.95 }}
              whileInView={{ opacity: 1, scale: 1 }}
              whileHover={{ scale: 1.03, y: -4 }}
              transition={{ delay: i * 0.05, duration: 0.3 }}
              viewport={{ once: true }}
              style={{ height: '100%' }}
            >
              <GlassCard
                component={Link}
                to={`/browse?section=${section.id}`}
                sx={s.card(section.color)}
              >
                <Typography sx={s.icon} role="img" aria-label="">
                  {section.icon}
                </Typography>
                <Typography variant="subtitle2" sx={s.name}>
                  {section.name}
                </Typography>
                <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                  <Box component="span" className="mono" sx={s.count(section.color)}>
                    {section.count}
                  </Box>{' '}
                  {t('sections.docsCount')}
                </Typography>
              </GlassCard>
            </motion.div>
          ))}
        </Box>
      </Box>
    </motion.section>
  );
}
