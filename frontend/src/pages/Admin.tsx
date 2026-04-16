import { Typography, Tabs, Tab, Box } from '@mui/material';
import { useState, lazy, Suspense } from 'react';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import PageWrapper from '@/components/layout/PageWrapper';
import OrbitalLoader from '@/components/ui/OrbitalLoader';

const AdminDocuments = lazy(() => import('@/components/admin/AdminDocuments'));
const AdminCourses = lazy(() => import('@/components/admin/AdminCourses'));
const AdminProfessors = lazy(() => import('@/components/admin/AdminProfessors'));
const AdminReports = lazy(() => import('@/components/admin/AdminReports'));
const AdminDelegates = lazy(() => import('@/components/admin/AdminDelegates'));

const PANELS = [AdminDocuments, AdminCourses, AdminProfessors, AdminReports, AdminDelegates];

function TabFallback() {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
      <OrbitalLoader size={40} />
    </Box>
  );
}

export default function Admin() {
  const { t } = useTranslation();
  const [tab, setTab] = useState(0);

  const Panel = PANELS[tab];

  return (
    <PageWrapper>
      <Helmet><title>{t('nav.admin')} — Freenote</title></Helmet>
      <Typography variant="h4" sx={{ fontWeight: 700, mb: 3 }}>{t('nav.admin')}</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }} variant="scrollable" scrollButtons="auto">
        <Tab label={t('admin.tabs.documents')} />
        <Tab label={t('admin.tabs.courses')} />
        <Tab label={t('admin.tabs.professors')} />
        <Tab label={t('admin.tabs.reports')} />
        <Tab label={t('admin.tabs.delegates')} />
      </Tabs>

      <Suspense fallback={<TabFallback />}>
        <Panel />
      </Suspense>
    </PageWrapper>
  );
}
