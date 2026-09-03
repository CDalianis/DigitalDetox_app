export type OnboardingStep = {
  emoji: string;
  title: string;
  body: string;
};

const MEMBER_STEPS: OnboardingStep[] = [
  {
    emoji: '📵',
    title: 'Welcome to Unplug',
    body: 'This is a calm space to build healthier screen habits — at your pace, with a coach in your corner. No streaks, no guilt.',
  },
  {
    emoji: '🎯',
    title: 'Your plans live here',
    body: 'Open My plans to see the detox plans a coach created for you. Each plan has a screen-time target and a focus, like “less scrolling before bed”.',
  },
  {
    emoji: '📝',
    title: 'Check in when you can',
    body: 'Inside a plan, tap Log my day. Note screen time, sleep, and how you felt. Honest check-ins help more than perfect ones.',
  },
  {
    emoji: '🌟',
    title: 'Goals and weekly reviews',
    body: 'Your coach can add goals and a short weekly review with tips. Use them as a nudge, not a scoreboard.',
  },
  {
    emoji: '📎',
    title: 'Screenshots, if you want',
    body: 'You can attach a screenshot or PDF to a check-in. Your coach sees it on the same plan — that’s it. Ready when you are.',
  },
];

const COACH_STEPS: OnboardingStep[] = [
  {
    emoji: '👋',
    title: 'Welcome, coach',
    body: 'Unplug is built for supportive coaching — help teens take back screen time without the lecture.',
  },
  {
    emoji: '✅',
    title: 'Wait for a quick approval',
    body: 'An admin reviews new coach accounts. Once you’re approved, you can create plans. Until then you can look around, but plan actions stay locked.',
  },
  {
    emoji: '📋',
    title: 'Create a plan for a member',
    body: 'After approval, go to My plans → New plan. Pick a member, a start date, and a daily screen-time target.',
  },
  {
    emoji: '🎯',
    title: 'Add goals and weekly reviews',
    body: 'Open a plan to add goals and a weekly review (summary, recommendation, risk level). Keep it short and kind.',
  },
  {
    emoji: '👀',
    title: 'Follow their check-ins',
    body: 'Members log days and can upload screenshots. Use that to adjust goals — small steps beat perfect streaks.',
  },
];

const ADMIN_STEPS: OnboardingStep[] = [
  {
    emoji: '🛡️',
    title: 'You keep the space safe',
    body: 'Admins approve coaches and can see plans across the platform.',
  },
  {
    emoji: '✅',
    title: 'Approve new coaches',
    body: 'Open Admin to review pending coaches. They cannot create plans until you approve them.',
  },
  {
    emoji: '👀',
    title: 'Look after plans',
    body: 'My plans shows the full picture. Step in when a member or coach needs a hand.',
  },
];

const STORAGE_PREFIX = 'unplug:needsOnboarding:';

function storageKey(username: string): string {
  return `${STORAGE_PREFIX}${username.trim().toLowerCase()}`;
}

export function markNeedsOnboarding(username: string): void {
  localStorage.setItem(storageKey(username), '1');
}

export function hasPendingOnboarding(username: string): boolean {
  return localStorage.getItem(storageKey(username)) === '1';
}

export function clearPendingOnboarding(username: string): void {
  localStorage.removeItem(storageKey(username));
}

export function getOnboardingSteps(role: string | null): OnboardingStep[] {
  if (role === 'COACH') return COACH_STEPS;
  if (role === 'ADMIN') return ADMIN_STEPS;
  return MEMBER_STEPS;
}
