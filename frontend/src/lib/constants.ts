export const CATEGORIES = ['SYNTHESE', 'EXAMEN', 'NOTES', 'EXERCICES', 'DIVERS'] as const;

export const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

export const DISCORD_OAUTH_URL = '/oauth2/authorization/discord';
export const GOOGLE_OAUTH_URL = '/oauth2/authorization/google';

export const DISCORD_INVITE_URL =
  import.meta.env.VITE_DISCORD_INVITE_URL ?? 'https://discord.gg/5mYdsDSKk9';
