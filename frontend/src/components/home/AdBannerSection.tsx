import { Box } from '@mui/material';
import { motion } from 'framer-motion';
import AdBanner from '@/components/ui/AdBanner';

export default function AdBannerSection() {
  return (
    <motion.section initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <Box sx={{ py: 4 }}>
        <AdBanner width={728} height={90} />
      </Box>
    </motion.section>
  );
}
