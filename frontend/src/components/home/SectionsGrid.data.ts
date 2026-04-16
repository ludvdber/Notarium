import { TOKENS } from '@/theme/tokens';

export interface SectionItem {
  id: string;
  name: string;
  icon: string;
  color: string;
  count: number;
}

// ISFCE formations (verified 2026-04-11 from isfce.org)
export const FALLBACK_SECTIONS: Omit<SectionItem, 'count'>[] = [
  { id: 'informatique', name: 'Informatique', icon: '💻', color: TOKENS.sections.informatique },
  { id: 'comptabilite', name: 'Comptabilité', icon: '📊', color: TOKENS.sections.comptabilite },
  { id: 'marketing', name: 'Marketing', icon: '📈', color: TOKENS.sections.marketing },
  { id: 'assistant', name: 'Assistant dir.', icon: '📋', color: TOKENS.sections.assistant },
  { id: 'fiscalite', name: 'Fiscalité', icon: '💶', color: TOKENS.sections.fiscalite },
  { id: 'langues', name: 'Langues', icon: '🌍', color: TOKENS.sections.langues },
  { id: 'cq6', name: 'CQ6 Prog.', icon: '⚙️', color: TOKENS.sections.cq6 },
];
