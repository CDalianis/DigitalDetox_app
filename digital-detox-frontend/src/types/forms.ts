import type { z } from 'zod';
import type {
  checkInSchema,
  goalSchema,
  loginSchema,
  memberRegisterSchema,
  planCreateSchema,
  reviewSchema,
} from '../schemas/auth';

export type LoginForm = z.infer<typeof loginSchema>;
export type MemberRegisterForm = z.infer<typeof memberRegisterSchema>;
export type CheckInForm = z.infer<typeof checkInSchema>;
export type PlanCreateForm = z.infer<typeof planCreateSchema>;
export type GoalForm = z.infer<typeof goalSchema>;
export type ReviewForm = z.infer<typeof reviewSchema>;
