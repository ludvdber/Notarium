import api from './axiosInstance';
import type {
  StatsResponse,
  DocumentResponse,
  PageResponse,
  Section,
  Course,
  LeaderboardEntry,
  DelegateResponse,
  NewsItem,
  User,
  ProfileCardResponse,
  Professor,
  CreateDocumentRequest,
  CreateCourseRequest,
  UpdateProfileRequest,
  RateRequest,
  ReportRequest,
} from '@/types';

// --- Stats ---
export const getStats = () =>
  api.get<StatsResponse>('/stats').then((r) => r.data);

// --- Documents ---
export const getDocumentById = (id: number) =>
  api.get<DocumentResponse>(`/documents/${id}`).then((r) => r.data);

export const searchDocuments = (params: {
  q?: string;
  courseId?: number;
  category?: string;
  sort?: string;
  page?: number;
  size?: number;
}) =>
  api.get<PageResponse<DocumentResponse>>('/documents/search', { params }).then((r) => r.data);

export const getPopularDocuments = () =>
  api.get<DocumentResponse[]>('/documents/popular').then((r) => r.data);

export const uploadDocument = (data: CreateDocumentRequest, file: File) => {
  const formData = new FormData();
  formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
  formData.append('file', file);
  return api.post<DocumentResponse>('/documents', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }).then((r) => r.data);
};

export const deleteDocument = (id: number) =>
  api.delete(`/documents/${id}`);

export const downloadDocument = (id: number) =>
  api.get<Blob>(`/documents/${id}/file`, { responseType: 'blob' }).then((r) => r.data);

// --- Ratings ---
export const rateDocument = (docId: number, data: RateRequest) =>
  api.post(`/documents/${docId}/ratings`, data);

export const getAverageRating = (docId: number) =>
  api.get<number>(`/documents/${docId}/ratings/average`).then((r) => r.data);

// --- Favorites ---
export const toggleFavorite = (docId: number) =>
  api.post<{ isFavorite: boolean }>(`/favorites/${docId}`).then((r) => r.data);

export const getFavorites = (page = 0, size = 20) =>
  api.get<PageResponse<DocumentResponse>>('/favorites', { params: { page, size } }).then((r) => r.data);

// --- Sections ---
export const getSections = () =>
  api.get<Section[]>('/sections').then((r) => r.data);

// --- Courses ---
export const getCourses = (sectionId: number) =>
  api.get<Course[]>('/courses', { params: { sectionId } }).then((r) => r.data);

export const createCourse = (data: CreateCourseRequest) =>
  api.post<Course>('/courses', data).then((r) => r.data);

// --- Professors ---
export const getProfessors = () =>
  api.get<Professor[]>('/professors').then((r) => r.data);

export const createProfessor = (name: string) =>
  api.post<Professor>('/professors', { name }).then((r) => r.data);

// --- Users ---
export const getCurrentUser = () =>
  api.get<User>('/users/me').then((r) => r.data);

export const updateProfile = (data: UpdateProfileRequest) =>
  api.put<User>('/users/me', data).then((r) => r.data);

export const getUserById = (id: number) =>
  api.get<User>(`/users/${id}`).then((r) => r.data);

export const getFeaturedProfiles = () =>
  api.get<ProfileCardResponse[]>('/users/featured').then((r) => r.data);

export const deleteAccount = () =>
  api.delete('/users/me');

// --- Leaderboard ---
export const getLeaderboard = () =>
  api.get<LeaderboardEntry[]>('/leaderboard').then((r) => r.data);

// --- Delegates ---
export const getDelegates = () =>
  api.get<DelegateResponse[]>('/delegates').then((r) => r.data);

// --- News ---
export const getNews = () =>
  api.get<NewsItem[]>('/news').then((r) => r.data);

// --- Reports ---
export const reportDocument = (docId: number, data: ReportRequest) =>
  api.post(`/documents/${docId}/reports`, data);

// --- Auth ---
export const requestVerification = (email: string) =>
  api.post('/auth/request-verification', { email });

export const confirmVerification = (code: string) =>
  api.post<{ token: string }>('/auth/confirm-verification', { code }).then((r) => r.data);
