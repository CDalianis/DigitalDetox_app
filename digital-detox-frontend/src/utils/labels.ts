const PLAN_STATUS: Record<string, string> = {
  DRAFT: 'Getting started',
  ACTIVE: 'On track',
  PAUSED: 'Taking a break',
  COMPLETED: 'Crushed it',
  ARCHIVED: 'In the vault',
};

const GOAL_STATUS: Record<string, string> = {
  PENDING: 'Up next',
  IN_PROGRESS: 'In progress',
  COMPLETED: 'Done',
  FAILED: 'Tough week',
};

const RISK_LEVEL: Record<string, string> = {
  LOW: 'Chill',
  MEDIUM: 'Watch it',
  HIGH: 'Needs care',
};

const ROLE_LABEL: Record<string, string> = {
  MEMBER: 'Member',
  COACH: 'Coach',
  ADMIN: 'Admin',
};

export function planStatusLabel(status: string) {
  return PLAN_STATUS[status] ?? status;
}

export function goalStatusLabel(status: string) {
  return GOAL_STATUS[status] ?? status.replace('_', ' ');
}

export function riskLabel(level: string) {
  return RISK_LEVEL[level] ?? level;
}

export function roleLabel(role: string) {
  return ROLE_LABEL[role] ?? role;
}
