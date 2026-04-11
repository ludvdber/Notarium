export interface SectionItem {
  id: string;
  name: string;
  icon: string;
  color: string;
  count: number;
}

export const FALLBACK_SECTIONS: Omit<SectionItem, 'count'>[] = [
  { id: 'informatique', name: 'Informatique', icon: '💻', color: '#00d2ff' },
  { id: 'comptabilite', name: 'Comptabilité', icon: '📊', color: '#7b2ff7' },
  { id: 'marketing', name: 'Marketing', icon: '📈', color: '#ff6b9d' },
  { id: 'assistant', name: 'Assistant dir.', icon: '📋', color: '#10b981' },
  { id: 'langues', name: 'Langues', icon: '🌍', color: '#fbbf24' },
  { id: 'technicien', name: 'Tech. prog.', icon: '⚙️', color: '#f97316' },
];
