import { z } from 'zod';

export const loginSchema = z.object({
  username: z.string().min(3, 'Username is required'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

export const registerUserSchema = z.object({
  username: z.string().min(3).max(50),
  email: z.string().email(),
  password: z.string().min(8).max(100),
});

export const memberRegisterSchema = z.object({
  user: registerUserSchema,
  displayName: z.string().min(2, 'Display name is required'),
  timezone: z.string().optional(),
  mainGoal: z.string().optional(),
});

export const coachRegisterSchema = z.object({
  user: registerUserSchema,
  displayName: z.string().min(2, 'Display name is required'),
  specialty: z.string().optional(),
  bio: z.string().optional(),
  yearsExperience: z.number().int().min(0).optional(),
});

export const checkInSchema = z.object({
  entryDate: z.string().min(1, 'Date is required'),
  totalScreenMinutes: z.number().int().min(0),
  socialMediaMinutes: z.number().int().min(0).optional(),
  sleepHours: z.number().min(0).max(24).optional(),
  focusScore: z.number().int().min(1).max(10).optional(),
  stressLevel: z.number().int().min(1).max(10).optional(),
  cravingLevel: z.number().int().min(1).max(10).optional(),
  notes: z.string().optional(),
});

export const planCreateSchema = z.object({
  memberProfileUuid: z.string().uuid('Select a member'),
  title: z.string().min(3),
  description: z.string().optional(),
  startDate: z.string().min(1),
  endDate: z.string().optional(),
  status: z.enum(['DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED', 'ARCHIVED']),
  targetScreenMinutes: z.number().int().min(0).optional(),
  targetSocialMinutes: z.number().int().min(0).optional(),
  focusArea: z.string().optional(),
});

export const goalSchema = z.object({
  title: z.string().min(2, 'Title is required'),
  description: z.string().optional(),
  metricType: z.enum(['SCREEN_MINUTES', 'SOCIAL_MINUTES', 'SLEEP_HOURS', 'CUSTOM']),
  targetValue: z.number().int().min(0),
  status: z.enum(['PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED']),
});

export const reviewSchema = z.object({
  weekStart: z.string().min(1, 'Week start date is required'),
  summary: z.string().optional(),
  recommendation: z.string().optional(),
  riskLevel: z.enum(['LOW', 'MEDIUM', 'HIGH']).optional(),
});
