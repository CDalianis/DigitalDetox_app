export type AuthResponse = {
  token: string;
  role: string;
  displayName: string;
};

export type DetoxPlan = {
  uuid: string;
  title: string;
  description: string;
  startDate: string;
  endDate: string | null;
  status: string;
  targetScreenMinutes: number | null;
  targetSocialMinutes: number | null;
  focusArea: string | null;
  memberProfileUuid: string;
  memberDisplayName: string;
  coachProfileUuid: string;
  coachDisplayName: string;
};

export type CheckIn = {
  uuid: string;
  detoxPlanUuid: string;
  entryDate: string;
  totalScreenMinutes: number;
  socialMediaMinutes: number | null;
  sleepHours: number | null;
  focusScore: number | null;
  stressLevel: number | null;
  cravingLevel: number | null;
  notes: string | null;
  attachments: Attachment[];
};

export type Attachment = {
  uuid: string;
  originalFilename: string;
  contentType: string;
  fileExtension: string;
  sizeBytes: number;
};

export type Member = {
  uuid: string;
  displayName: string;
  timezone: string | null;
  mainGoal: string | null;
  username: string;
  email: string;
};

export type Coach = {
  uuid: string;
  displayName: string;
  specialty: string | null;
  bio: string | null;
  approved: boolean;
  yearsExperience: number | null;
  username: string;
  email: string;
};

export type Goal = {
  uuid: string;
  title: string;
  description: string | null;
  metricType: string;
  targetValue: number;
  currentValue: number | null;
  status: string;
  detoxPlanUuid: string;
};

export type WeeklyReview = {
  uuid: string;
  detoxPlanUuid: string;
  coachProfileUuid: string;
  coachDisplayName: string;
  weekStart: string;
  summary: string | null;
  recommendation: string | null;
  riskLevel: string | null;
};
