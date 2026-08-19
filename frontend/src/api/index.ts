import axios from "axios";
import type { ApiResult, Cat, CatMedia, Adopter, Notification, CatHealthRecord, CatComment, User } from "../types";
import { uploadToOss } from "../utils/oss";

const http = axios.create({
  baseURL: "/miaohome/api",
  withCredentials: true,
});

// === Auth ===
export const login = (data: { phone: string; password: string }) =>
  http.post<ApiResult<User>>("/users/login", data).then((r) => r.data);

export const register = (data: { phone: string; password: string; nickname: string }) =>
  http.post<ApiResult<User>>("/users/register", data).then((r) => r.data);

export const getMe = () =>
  http.get<ApiResult<User>>("/users/me").then((r) => r.data);

export const updateMyAvatar = (objectKey: string) =>
  http.put<ApiResult<User>>("/users/me/avatar", { objectKey }).then((r) => r.data);

export const uploadUserAvatar = async (file: File) => {
  const objectKey = await uploadToOss(file);
  return updateMyAvatar(objectKey);
};

export const logout = () =>
  http.post<ApiResult<null>>("/users/logout").then((r) => r.data);

// === Cat Likes ===
export const toggleCatLike = (catId: number) =>
  http.post<ApiResult<{ liked: boolean; likeCount: number }>>(`/cats/${catId}/like`).then((r) => r.data);

export const getCatLikeStatus = (catId: number) =>
  http.get<ApiResult<{ liked: boolean }>>(`/cats/${catId}/like/status`).then((r) => r.data);

// === Comments ===
export const getComments = (catId: number, sortBy?: string, order?: string) =>
  http.get<ApiResult<CatComment[]>>(`/cats/${catId}/comments/list`, { params: { sortBy, order } }).then((r) => r.data);

export const createComment = (catId: number, data: { content: string; parentId?: number }) =>
  http.post<ApiResult<CatComment>>(`/cats/${catId}/comments`, data).then((r) => r.data);

export const deleteComment = (commentId: number) =>
  http.delete<ApiResult<null>>(`/comments/${commentId}`).then((r) => r.data);

export const toggleCommentLike = (commentId: number) =>
  http.post<ApiResult<{ liked: boolean; likeCount: number }>>(`/comments/${commentId}/like`).then((r) => r.data);

// === Cats ===
export const getCats = () =>
  http.get<ApiResult<Cat[]>>("/cats/list").then((r) => r.data);

export const getCatDetail = (id: number) =>
  http.get<ApiResult<Cat>>(`/cats/${id}`).then((r) => r.data);

export const createCat = (data: Partial<Cat>) =>
  http.post<ApiResult<Cat>>("/cats", data).then((r) => r.data);

export const updateCat = (id: number, data: Partial<Cat>) =>
  http.put<ApiResult<Cat>>(`/cats/${id}`, data).then((r) => r.data);

export const deleteCat = (id: number) =>
  http.delete<ApiResult<null>>(`/cats/${id}`).then((r) => r.data);

export const confirmCatMediaObject = (
  catId: number,
  data: {
    objectKey: string;
    fileName: string;
    fileSize: number;
    mediaType: "PHOTO" | "VIDEO";
    ageStage?: string;
    isAvatar?: boolean;
  }
) =>
  http.post<ApiResult<CatMedia>>(`/cats/${catId}/media/confirm`, data).then((r) => r.data);

export const uploadCatMedia = async (catId: number, file: File, ageStage?: string, isAvatar?: boolean) => {
  const objectKey = await uploadToOss(file);
  return confirmCatMediaObject(catId, {
    objectKey,
    fileName: file.name,
    fileSize: file.size,
    mediaType: file.type.startsWith("video") ? "VIDEO" : "PHOTO",
    ageStage,
    isAvatar: isAvatar ?? false,
  });
};

export const deleteCatMedia = (mediaId: number) =>
  http.delete<ApiResult<null>>(`/cats/media/${mediaId}`).then((r) => r.data);

export const setAvatar = (catId: number, mediaId: number) =>
  http.post<ApiResult<Cat>>(`/cats/${catId}/avatar/${mediaId}`).then((r) => r.data);

// === Cat Health Records ===
export const getHealthRecords = (catId: number) =>
  http.get<ApiResult<CatHealthRecord[]>>(`/cats/${catId}/health/records/list`).then((r) => r.data);

export const addHealthRecord = (catId: number, data: Partial<CatHealthRecord>) =>
  http.post<ApiResult<CatHealthRecord>>(`/cats/${catId}/health/records`, data).then((r) => r.data);

export const updateHealthRecord = (catId: number, recordId: number, data: Partial<CatHealthRecord>) =>
  http.put<ApiResult<CatHealthRecord>>(`/cats/${catId}/health/records/${recordId}`, data).then((r) => r.data);

export const deleteHealthRecord = (catId: number, recordId: number) =>
  http.delete<ApiResult<null>>(`/cats/${catId}/health/records/${recordId}`).then((r) => r.data);

// === Adopters ===
export const getAdopters = () =>
  http.get<ApiResult<Adopter[]>>("/adopters/list").then((r) => r.data);

export const getAdoptersByCat = (catId: number) =>
  http.get<ApiResult<Adopter[]>>(`/adopters/cat/${catId}/list`).then((r) => r.data);

export const adoptCat = (catId: number, data: Partial<Adopter>) =>
  http.post<ApiResult<Adopter>>(`/adopters/adopt/${catId}`, data).then((r) => r.data);

export const cancelAdoption = (id: number) =>
  http.delete<ApiResult<null>>(`/adopters/${id}`).then((r) => r.data);

export const searchAdopters = (keyword: string) =>
  http.get<ApiResult<Adopter[]>>("/adopters/search", { params: { keyword } }).then((r) => r.data);

// === Notifications ===
export const getNotifications = () =>
  http.get<ApiResult<Notification[]>>("/notifications/list").then((r) => r.data);

export const getNotificationsByAdopter = (adopterId: number) =>
  http.get<ApiResult<Notification[]>>(`/notifications/adopter/${adopterId}/list`).then((r) => r.data);

export const getUnreadCount = (adopterId: number) =>
  http.get<ApiResult<{ count: number }>>(`/notifications/adopter/${adopterId}/unread/count`).then((r) => r.data);

export const markNotificationRead = (id: number) =>
  http.put<ApiResult<null>>(`/notifications/${id}/read`).then((r) => r.data);

export const markAllNotificationsRead = (adopterId: number) =>
  http.put<ApiResult<null>>(`/notifications/adopter/${adopterId}/read/all`).then((r) => r.data);

export const sendNotification = (data: { adopterId: number; catId?: number; title: string; content: string }) =>
  http.post<ApiResult<Notification>>("/notifications/send", data).then((r) => r.data);

export const sendNotificationToCatAdopters = (catId: number, title: string, content: string) =>
  http.post<ApiResult<null>>(`/notifications/cat/${catId}/adopters/send`, { title, content }).then((r) => r.data);

// === Tenants ===
export const getTenants = () =>
  http.get<ApiResult<any[]>>("/tenants/list").then((r) => r.data);

export const createTenant = (data: { name: string; code: string; building: string }) =>
  http.post<ApiResult<any>>("/tenants", data).then((r) => r.data);
