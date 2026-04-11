export interface User {
  id: number;
  username: string;
  xp: number;
  bio: string | null;
  website: string | null;
  github: string | null;
  linkedin: string | null;
  discord: string | null;
  badges: string[];
  documentCount: number;
  profilePublic: boolean;
  supporter: boolean;
}

export interface DocumentResponse {
  id: number;
  title: string;
  courseName: string;
  sectionName: string;
  category: string;
  authorName: string;
  verified: boolean;
  aiGenerated: boolean;
  language: string;
  year: string | null;
  professorName: string | null;
  averageRating: number;
  downloadCount: number;
  tags: string[];
  summaryAi: string | null;
  createdAt: string;
}

export interface Section {
  id: number;
  name: string;
  icon: string | null;
  documentCount: number;
}

export interface Course {
  id: number;
  name: string;
  sectionName: string;
  documentCount: number;
}

export interface StatsResponse {
  totalDocs: number;
  totalDownloads: number;
  totalContributors: number;
  totalCourses: number;
  weekUploads: number;
}

export interface LeaderboardEntry {
  rank: number;
  username: string;
  xp: number;
  documentCount: number;
  badges: string[];
  supporter: boolean;
}

export interface DelegateMember {
  username: string;
  discord: string | null;
}

export interface DelegateResponse {
  sectionName: string;
  sectionColor: string | null;
  members: DelegateMember[];
}

export interface NewsItem {
  title: string;
  date: string | null;
  labels: string[];
  url: string | null;
}

export interface ProfileCardResponse {
  username: string;
  role: string;
  discord: string | null;
  github: string | null;
  linkedin: string | null;
  badges: string[];
  supporter: boolean;
}

export interface ErrorResponse {
  status: number;
  message: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateDocumentRequest {
  title: string;
  courseId: number;
  category: string;
  year?: string;
  professorId?: number;
  language: string;
  aiGenerated: boolean;
  anonymous: boolean;
  tags?: string[];
}

export interface UpdateProfileRequest {
  bio?: string;
  website?: string;
  github?: string;
  linkedin?: string;
  discord?: string;
  profilePublic: boolean;
  showInCarousel: boolean;
  themePref: string;
}

export interface RateRequest {
  score: number;
}

export interface ReportRequest {
  reason: string;
}

export interface CreateCourseRequest {
  name: string;
  sectionId: number;
}

export interface Professor {
  id: number;
  name: string;
}

export type Category = 'SYNTHESE' | 'EXAMEN' | 'NOTES' | 'EXERCICES' | 'DIVERS';
