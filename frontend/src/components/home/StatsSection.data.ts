import type { StatsResponse } from '@/types';
import type { TFunction } from 'i18next';

export interface StatItem {
  label: string;
  value: number;
  icon: string;
  color: string;
}

export function buildStatItems(stats: StatsResponse, t: TFunction): StatItem[] {
  return [
    { label: t('stats.docs'), value: stats.totalDocs, icon: '📄', color: '#00d2ff' },
    { label: t('stats.downloads'), value: stats.totalDownloads, icon: '⬇️', color: '#7b2ff7' },
    { label: t('stats.contributors'), value: stats.totalContributors, icon: '👥', color: '#ff6b9d' },
    { label: t('stats.courses'), value: stats.totalCourses, icon: '📚', color: '#10b981' },
  ];
}
