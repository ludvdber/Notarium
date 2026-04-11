export function formatNumber(n: number): string {
  if (n >= 1_000_000) return `${(n / 1_000_000).toFixed(1)}M`;
  if (n >= 1_000) return `${(n / 1_000).toFixed(1)}k`;
  return n.toString();
}

export function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('fr-BE', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

export function categoryColor(category: string): string {
  const colors: Record<string, string> = {
    SYNTHESE: '#00d2ff',
    EXAMEN: '#ff6b6b',
    NOTES: '#ffd93d',
    EXERCICES: '#6bcb77',
    DIVERS: '#a66cff',
  };
  return colors[category] ?? '#888';
}
