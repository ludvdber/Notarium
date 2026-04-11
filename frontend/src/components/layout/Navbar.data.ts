export interface NavLink {
  key: string;
  to: string;
}

export const NAV_LINKS: NavLink[] = [
  { key: 'browse', to: '/browse' },
  { key: 'leaderboard', to: '/leaderboard' },
  { key: 'news', to: '/news' },
  { key: 'tools', to: '/tools' },
];
