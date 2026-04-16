import { TOKENS } from '@/theme/tokens';

export function formatNumber(n: number): string {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}k`;
  return n.toString();
}

export function formatDate(dateStr: string, locale: string = 'fr'): string {
  return new Date(dateStr).toLocaleDateString(locale === 'fr' ? 'fr-BE' : 'en-GB', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

export function formatRelativeDate(dateStr: string, locale: string = 'fr'): string {
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffDay >= 7) {
    return date.toLocaleDateString(locale === 'fr' ? 'fr-BE' : 'en-GB', {
      month: 'short',
      day: 'numeric',
    });
  }
  if (diffDay >= 1) return locale === 'fr' ? `Il y a ${diffDay}j` : `${diffDay}d ago`;
  if (diffHour >= 1) return locale === 'fr' ? `Il y a ${diffHour}h` : `${diffHour}h ago`;
  if (diffMin >= 1) return locale === 'fr' ? `Il y a ${diffMin}min` : `${diffMin}min ago`;
  return locale === 'fr' ? 'À l\'instant' : 'Just now';
}

export function categoryColor(category: string): string {
  const key = category as keyof typeof TOKENS.categories;
  return TOKENS.categories[key] ?? '#888';
}
