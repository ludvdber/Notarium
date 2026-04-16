import api from './axiosInstance';
import type {
  StatsResponse,
  DocumentResponse,
  PageResponse,
  Section,
  Course,
  LeaderboardEntry,
  DelegateResponse,
  DelegateHistoryResponse,
  DelegateMember,
  UpdateDocumentRequest,
  AssignDelegateRequest,
  EndDelegateRequest,
  NewsItem,
  User,
  ProfileCardResponse,
  Professor,
  CreateDocumentRequest,
  CreateCourseRequest,
  UpdateProfileRequest,
  RateRequest,
  ReportRequest,
  ReportResponse,
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

export const getDocumentsByUser = (userId: number, page = 0, size = 6) =>
  api.get<PageResponse<DocumentResponse>>(`/documents/user/${userId}`, { params: { page, size } }).then((r) => r.data);

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

export const acceptTerms = () =>
  api.post('/users/me/accept-terms');

export const deleteAccount = () =>
  api.delete('/users/me');

// --- Leaderboard ---
export const getLeaderboard = () =>
  api.get<LeaderboardEntry[]>('/leaderboard').then((r) => r.data);

// --- Delegates ---
export const getDelegates = () =>
  api.get<DelegateResponse[]>('/delegates').then((r) => r.data);

export const getDelegateHistory = (userId: number) =>
  api.get<DelegateHistoryResponse[]>(`/delegates/user/${userId}`).then((r) => r.data);

export const getAllMandates = () =>
  api.get<DelegateMember[]>('/admin/delegates').then((r) => r.data);

export const assignDelegate = (data: AssignDelegateRequest) =>
  api.post<DelegateMember>('/admin/delegates', data).then((r) => r.data);

export const endDelegate = (id: number, data: EndDelegateRequest) =>
  api.patch<DelegateMember>(`/admin/delegates/${id}`, data).then((r) => r.data);

export const deleteMandate = (id: number) =>
  api.delete(`/admin/delegates/${id}`);

// --- Admin: Documents ---
export const getPendingDocuments = () =>
  api.get<DocumentResponse[]>('/admin/documents/pending').then((r) => r.data);

export const verifyDocument = (id: number) =>
  api.put<DocumentResponse>(`/admin/documents/${id}/verify`).then((r) => r.data);

export const adminUpdateDocument = (id: number, data: UpdateDocumentRequest) =>
  api.put<DocumentResponse>(`/admin/documents/${id}`, data).then((r) => r.data);

export const adminDeleteDocument = (id: number) =>
  api.delete(`/admin/documents/${id}`);

// --- Admin: Courses ---
export const getPendingCourses = () =>
  api.get<Course[]>('/admin/courses/pending').then((r) => r.data);

export const approveCourse = (id: number) =>
  api.put<Course>(`/admin/courses/${id}/approve`).then((r) => r.data);

// --- Admin: Professors ---
export const getPendingProfessors = () =>
  api.get<Professor[]>('/admin/professors/pending').then((r) => r.data);

export const approveProfessor = (id: number) =>
  api.put<Professor>(`/admin/professors/${id}/approve`).then((r) => r.data);

// --- Admin: Reports ---
export const getPendingReports = (page = 0, size = 20) =>
  api.get<PageResponse<ReportResponse>>('/admin/reports/pending', { params: { page, size } }).then((r) => r.data);

export const resolveReport = (id: number) =>
  api.put(`/admin/reports/${id}/resolve`);

export const dismissReport = (id: number) =>
  api.put(`/admin/reports/${id}/dismiss`);

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
  api.post<void>('/auth/confirm-verification', { code });

export const logoutApi = () =>
  api.post('/auth/logout');

// --- Dev-only ---
export const devLogin = (username: string) =>
  api.post<{ username: string; role: string; verified: string }>(`/dev/login/${username}`).then((r) => r.data);
