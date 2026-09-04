import { chromium } from 'playwright';
import { mkdir } from 'node:fs/promises';
import { dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const API = 'http://localhost:8080/api/v1';
const APP = 'http://localhost:5173';
const OUT = process.env.SCREENSHOT_DIR
  ?? dirname(fileURLToPath(import.meta.url));

const MEMBER = {
  username: 'alex',
  password: 'Member123!',
  email: 'alex@unplug.test',
  displayName: 'Alex',
};
const COACH = {
  username: 'jordan',
  password: 'Coach123!',
  email: 'jordan@unplug.test',
  displayName: 'Jordan',
};
const PENDING = [
  {
    username: 'sam.rivera',
    password: 'Coach123!',
    email: 'sam@unplug.test',
    displayName: 'Sam Rivera',
    specialty: 'Teen wellness',
  },
  {
    username: 'lena.park',
    password: 'Coach123!',
    email: 'lena@unplug.test',
    displayName: 'Lena Park',
    specialty: 'Digital balance',
  },
];

async function request(method, path, { token, body } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;
  const response = await fetch(`${API}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = { raw: text };
  }
  return { status: response.status, data };
}

async function login(username, password) {
  const { status, data } = await request('POST', '/auth/authenticate', {
    body: { username, password },
  });
  if (status !== 200 || !data?.token) {
    throw new Error(`Login failed for ${username}: ${status} ${JSON.stringify(data)}`);
  }
  return data.token;
}

async function registerMember() {
  const { status, data } = await request('POST', '/members/register', {
    body: {
      user: { username: MEMBER.username, email: MEMBER.email, password: MEMBER.password },
      displayName: MEMBER.displayName,
      timezone: 'Europe/Athens',
      mainGoal: 'less TikTok before bed',
    },
  });
  if (![201, 400, 409].includes(status)) {
    throw new Error(`Member register failed: ${status} ${JSON.stringify(data)}`);
  }
}

async function registerCoach(coach) {
  const { status, data } = await request('POST', '/coaches/register', {
    body: {
      user: { username: coach.username, email: coach.email, password: coach.password },
      displayName: coach.displayName,
      specialty: coach.specialty ?? 'Teen screen habits',
      bio: 'Helps teens unplug without the guilt.',
      yearsExperience: 4,
    },
  });
  if (![201, 400, 409].includes(status)) {
    throw new Error(`Coach register failed (${coach.username}): ${status} ${JSON.stringify(data)}`);
  }
}

async function ensureDemoData() {
  await registerMember();
  await registerCoach(COACH);
  for (const coach of PENDING) {
    await registerCoach(coach);
  }

  const adminToken = await login('admin', 'Admin123!');
  const pending = await request('GET', '/coaches/pending', { token: adminToken });
  const jordan = (pending.data ?? []).find((c) => c.username === COACH.username);
  if (jordan && !jordan.approved) {
    await request('PATCH', `/coaches/${jordan.uuid}/approve`, { token: adminToken });
  } else {
    const coaches = await request('GET', '/coaches', { token: adminToken });
    const approvedJordan = (coaches.data ?? []).find((c) => c.username === COACH.username);
    if (!approvedJordan) {
      const stillPending = await request('GET', '/coaches/pending', { token: adminToken });
      const match = (stillPending.data ?? []).find((c) => c.username === COACH.username);
      if (match) {
        await request('PATCH', `/coaches/${match.uuid}/approve`, { token: adminToken });
      }
    }
  }

  const coachToken = await login(COACH.username, COACH.password);
  const members = await request('GET', '/members', { token: coachToken });
  const alex = (members.data ?? []).find((m) => m.username === MEMBER.username);
  if (!alex) throw new Error('Alex member profile not found');

  const plansPage = await request('GET', '/plans?size=20&sort=startDate,desc', { token: coachToken });
  let plans = plansPage.data?.content ?? [];
  if (plans.length === 0) {
    const specs = [
      {
        title: 'Social media reset week',
        description: 'Less doomscrolling after 9pm. More sleep, more IRL.',
        status: 'ACTIVE',
        targetScreenMinutes: 120,
      },
      {
        title: 'Sleep-first challenge',
        description: 'Protect the last hour of the night.',
        status: 'DRAFT',
        targetScreenMinutes: 90,
      },
      {
        title: 'Weekend unplug hours',
        description: 'Phone in another room on Saturday mornings.',
        status: 'PAUSED',
        targetScreenMinutes: 150,
      },
    ];
    for (const spec of specs) {
      await request('POST', '/plans', {
        token: coachToken,
        body: {
          memberProfileUuid: alex.uuid,
          title: spec.title,
          description: spec.description,
          startDate: '2026-09-01',
          endDate: '2026-09-30',
          status: spec.status,
          targetScreenMinutes: spec.targetScreenMinutes,
          targetSocialMinutes: 45,
          focusArea: 'Social media',
        },
      });
    }
    const refreshed = await request('GET', '/plans?size=20&sort=startDate,desc', { token: coachToken });
    plans = refreshed.data?.content ?? [];
  }

  const mainPlan = plans.find((p) => p.title.includes('Social media')) ?? plans[0];
  if (!mainPlan) throw new Error('No plan to screenshot');

  const goals = await request('GET', `/plans/${mainPlan.uuid}/goals`, { token: coachToken });
  if ((goals.data ?? []).length === 0) {
    await request('POST', `/plans/${mainPlan.uuid}/goals`, {
      token: coachToken,
      body: {
        title: 'Under 2h social media',
        description: 'Weeknight target',
        metricType: 'SOCIAL_MINUTES',
        targetValue: 120,
        status: 'IN_PROGRESS',
      },
    });
    await request('POST', `/plans/${mainPlan.uuid}/goals`, {
      token: coachToken,
      body: {
        title: 'Sleep 8 hours',
        description: 'Phone charger stays in the kitchen',
        metricType: 'SLEEP_HOURS',
        targetValue: 8,
        status: 'PENDING',
      },
    });
  }

  const reviews = await request('GET', `/plans/${mainPlan.uuid}/reviews`, { token: coachToken });
  if ((reviews.data ?? []).length === 0) {
    await request('POST', `/plans/${mainPlan.uuid}/reviews`, {
      token: coachToken,
      body: {
        weekStart: '2026-08-31',
        summary: 'Solid progress on evenings. Keep phones out of bed.',
        recommendation: 'Try a 30-minute wind-down playlist.',
        riskLevel: 'LOW',
      },
    });
  }

  const memberToken = await login(MEMBER.username, MEMBER.password);
  const checkIns = await request('GET', `/plans/${mainPlan.uuid}/check-ins?size=10`, { token: memberToken });
  if ((checkIns.data?.content ?? []).length === 0) {
    await request('POST', `/plans/${mainPlan.uuid}/check-ins`, {
      token: memberToken,
      body: {
        entryDate: '2026-09-02',
        totalScreenMinutes: 165,
        socialMediaMinutes: 70,
        sleepHours: 7.5,
        focusScore: 7,
        stressLevel: 4,
        notes: 'Logged off after dinner.',
      },
    });
    await request('POST', `/plans/${mainPlan.uuid}/check-ins`, {
      token: memberToken,
      body: {
        entryDate: '2026-09-03',
        totalScreenMinutes: 140,
        socialMediaMinutes: 55,
        sleepHours: 8,
        focusScore: 8,
        stressLevel: 3,
        notes: 'Phone stayed in the kitchen overnight.',
      },
    });
  }

  return mainPlan.uuid;
}

async function shot(page, name, options = {}) {
  const file = `${OUT}/${name}.png`;
  await page.evaluate(() => document.fonts.ready);
  await page.waitForTimeout(250);
  await page.screenshot({ path: file, fullPage: options.fullPage ?? true });
  console.log('wrote', file);
}

async function loginUi(page, username, password) {
  await page.goto(`${APP}/login`, { waitUntil: 'networkidle' });
  await page.evaluate(() => localStorage.clear());
  await page.reload({ waitUntil: 'networkidle' });
  await page.locator('input[name="username"]').fill(username);
  await page.locator('input[name="password"]').fill(password);
  await page.getByRole('button', { name: /let's go/i }).click();
  await page.waitForURL(/\/plans/, { timeout: 15000 });
  const overlay = page.locator('.onboarding-overlay');
  if (await overlay.isVisible().catch(() => false)) {
    await overlay.getByRole('button', { name: /skip/i }).click();
    await overlay.waitFor({ state: 'hidden' });
  }
}

async function main() {
  await mkdir(OUT, { recursive: true });
  const planUuid = await ensureDemoData();
  console.log('demo plan', planUuid);

  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({
    viewport: { width: 1440, height: 900 },
    deviceScaleFactor: 1,
  });

  await page.goto(`${APP}/login`, { waitUntil: 'networkidle' });
  await shot(page, 'login-page', { fullPage: false });

  await page.goto(`${APP}/register`, { waitUntil: 'networkidle' });
  await shot(page, 'register-page', { fullPage: true });

  await loginUi(page, 'admin', 'Admin123!');
  await page.waitForSelector('.plan-card, .empty-state');
  await shot(page, 'plans-dashboard', { fullPage: false });

  await page.goto(`${APP}/admin`, { waitUntil: 'networkidle' });
  await page.waitForSelector('.plan-card, .empty-state');
  await shot(page, 'admin-approvals', { fullPage: false });

  await loginUi(page, MEMBER.username, MEMBER.password);
  await page.goto(`${APP}/plans/${planUuid}`, { waitUntil: 'networkidle' });
  await page.waitForSelector('h1');
  await shot(page, 'plan-detail-member', { fullPage: true });

  await loginUi(page, COACH.username, COACH.password);
  await page.goto(`${APP}/plans/${planUuid}`, { waitUntil: 'networkidle' });
  await page.waitForSelector('h1');
  await shot(page, 'plan-detail-coach', { fullPage: true });

  const tourUser = `tour_${Date.now()}`;
  await page.goto(`${APP}/register`, { waitUntil: 'networkidle' });
  await page.evaluate(() => localStorage.clear());
  await page.reload({ waitUntil: 'networkidle' });
  await page.locator('input[name="user.username"]').fill(tourUser);
  await page.locator('input[name="user.email"]').fill(`${tourUser}@unplug.test`);
  await page.locator('input[name="user.password"]').fill('Member123!');
  await page.locator('input[name="displayName"]').fill('Riley');
  await page.getByRole('button', { name: /create account/i }).click();
  await page.waitForSelector('.onboarding-overlay', { timeout: 15000 });
  await page.waitForTimeout(300);
  await shot(page, 'onboarding-wizard', { fullPage: false });

  await browser.close();
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
