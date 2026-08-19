export interface Cat {
  id: number;
  tenantId: number;
  name: string;
  color: string;
  gender: string;
  birthYear: number;
  description: string;
  avatarUrl: string;
  isAdopted: boolean;
  isNeutered: boolean;
  likeCount: number;
  createdAt: string;
  updatedAt: string;
  mediaList?: CatMedia[];
  healthRecordList?: CatHealthRecord[];
  locationList?: FrequentCommunityItem[];
}

export interface CatMedia {
  id: number;
  catId: number;
  mediaType: "PHOTO" | "VIDEO";
  url: string;
  ageStage: string;
  isAvatar: boolean;
  fileName: string;
  fileSize: number;
  createdAt: string;
}

export interface CatHealthRecord {
  id: number;
  catId: number;
  isSick: boolean;
  diseaseName: string;
  description: string;
  treatment: string;
  recordDate: string;
  createdAt: string;
}

export interface CatComment {
  id: number;
  catId: number;
  tenantId: number;
  userId: number;
  nickname: string;
  avatarUrl: string;
  parentId: number | null;
  content: string;
  likeCount: number;
  likedByCurrentUser: boolean;
  createdAt: string;
  updatedAt: string;
  replies: CatComment[];
}

export interface User {
  id: number;
  nickname: string;
  avatarUrl: string;
  userType: string;
  createdAt: string;
}

export interface Adopter {
  id: number;
  catId: number;
  tenantId: number;
  householdNumber: string;
  adopterName: string;
  phone: string;
  building: string;
  unitNumber: string;
  adoptedAt: string;
  isActive: boolean;
  catName?: string;
}

export interface Notification {
  id: number;
  tenantId: number;
  adopterId: number;
  catId: number;
  title: string;
  content: string;
  isRead: boolean;
  createdAt: string;
}

export interface FrequentCommunityItem {
  tenantId: number;
  building: string;
  tenantName?: string;
}

export interface Tenant {
  id: number;
  name: string;
  code: string;
  building: string;
  createdAt: string;
}

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}
