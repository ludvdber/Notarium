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
  termsAccepted: boolean;
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
  userId: number;
  rank: number;
  username: string;
  xp: number;
  documentCount: number;
  badges: string[];
  supporter: boolean;
}

export interface DelegateMember {
  id: number;
  userId: number | null;
  displayName: string | null;
  username: string;
  discord: string | null;
  startDate: string;
  endDate: string | null;
}

export interface DelegateResponse {
  sectionName: string;
  sectionColor: string | null;
  members: DelegateMember[];
}

export interface DelegateHistoryResponse {
  id: number;
  sectionName: string;
  startDate: string;
  endDate: string | null;
  active: boolean;
}

export interface AssignDelegateRequest {
  userId: number;
  sectionId: number;
  startDate: string;
}

export interface EndDelegateRequest {
  endDate: string;
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

export interface UpdateDocumentRequest {
  title?: string;
  category?: string;
  language?: string;
  year?: string;
  professorId?: number;
  verified?: boolean;
  tags?: string[];
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

export interface ReportResponse {
  id: number;
  documentId: number;
  documentTitle: string;
  reporterUsername: string;
  reason: string;
  status: string;
  createdAt: string;
}

export type Category = 'SYNTHESE' | 'EXAMEN' | 'NOTES' | 'EXERCICES' | 'DIVERS';

