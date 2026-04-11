import { Box, Typography, List, ListItemButton, ListItemText, Chip } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { getNews } from '@/api/endpoints';
import GlassCard from '@/components/ui/GlassCard';
import { MAIN_LINKS, SECONDARY_LINKS } from './NewsAndLinks.data';
import * as s from './NewsAndLinks.styles';

export default function NewsAndLinks() {
  const { t } = useTranslation();
  const { data: news } = useQuery({ queryKey: ['news'], queryFn: getNews });

  return (
    <motion.section initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <Box sx={s.section}>
        <Box sx={s.flexRow}>
          {/* News column */}
          <Box sx={s.newsCol}>
            <Typography variant="h5" sx={s.columnTitle}>
              📰 {t('news.title')}
            </Typography>
            <GlassCard sx={s.newsCard}>
              <List dense sx={s.newsList}>
                {(news ?? []).slice(0, 6).map((item, i) => (
                  <ListItemButton
                    key={i}
                    component="a"
                    href={item.url ?? '#'}
                    target="_blank"
                    rel="noopener noreferrer"
                    sx={s.newsItem}
                  >
                    <ListItemText
                      primary={
                        <Typography variant="body2" sx={s.newsTitle} noWrap>
                          {item.title}
                        </Typography>
                      }
                      secondary={item.date?.split('T')[0]}
                    />
                    <Box sx={s.newsLabelsRow}>
                      {item.labels.slice(0, 2).map((label) => (
                        <Chip key={label} label={label} size="small" variant="outlined" sx={s.newsLabelChip} />
                      ))}
                    </Box>
                  </ListItemButton>
                ))}
              </List>
            </GlassCard>
          </Box>

          {/* Links column */}
          <Box sx={s.linksCol}>
            <Typography variant="h5" sx={s.columnTitle}>
              🔗 {t('links.title')}
            </Typography>
            <GlassCard sx={s.linksCard}>
              <Box sx={s.mainLinksCol}>
                {MAIN_LINKS.map((link) => (
                  <Box
                    key={link.key}
                    component="a"
                    href={link.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    sx={s.mainLinkRow(link.color)}
                  >
                    <Box sx={s.mainLinkIcon(link.color)}>{link.icon}</Box>
                    <Typography variant="body2" sx={s.mainLinkLabel}>
                      {t(`links.${link.key}`)}
                    </Typography>
                  </Box>
                ))}
              </Box>

              <Box sx={s.secondaryGrid}>
                {SECONDARY_LINKS.map((link) => (
                  <Box
                    key={link.key}
                    component="a"
                    href={link.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    sx={s.secondaryLinkRow}
                  >
                    {link.icon}
                    <Typography variant="caption" sx={s.secondaryLinkLabel}>
                      {t(`links.${link.key}`)}
                    </Typography>
                  </Box>
                ))}
              </Box>
            </GlassCard>
          </Box>
        </Box>
      </Box>
    </motion.section>
  );
}
