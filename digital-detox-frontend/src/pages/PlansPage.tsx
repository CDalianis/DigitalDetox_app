import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiFetch, type PageResponse } from '../api/client';
import type { DetoxPlan, Member } from '../api/types';
import { getErrorMessage, useAuth } from '../context/AuthContext';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { PageHero } from '../components/PageHero';
import { planCreateSchema } from '../schemas/auth';
import type { PlanCreateForm } from '../types/forms';
import { planStatusLabel } from '../utils/labels';

export function PlansPage() {
  const { role } = useAuth();
  const [plans, setPlans] = useState<DetoxPlan[]>([]);
  const [members, setMembers] = useState<Member[]>([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [titleFilter, setTitleFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState('');
  const [showCreate, setShowCreate] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PlanCreateForm>({
    resolver: zodResolver(planCreateSchema),
    defaultValues: { status: 'ACTIVE' },
  });

  const loadPlans = async () => {
    setError('');
    try {
      const params = new URLSearchParams({ page: String(page), size: '8', sort: 'startDate,desc' });
      if (statusFilter) params.set('status', statusFilter);
      if (titleFilter) params.set('title', titleFilter);
      const data = await apiFetch<PageResponse<DetoxPlan>>(`/plans?${params}`);
      setPlans(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  useEffect(() => {
    void loadPlans();
  }, [page, statusFilter, titleFilter]);

  useEffect(() => {
    if (role === 'COACH' || role === 'ADMIN') {
      void apiFetch<Member[]>('/members').then(setMembers).catch(() => undefined);
    }
  }, [role]);

  const onCreatePlan = async (data: PlanCreateForm) => {
    setError('');
    try {
      await apiFetch('/plans', { method: 'POST', body: JSON.stringify(data) });
      reset();
      setShowCreate(false);
      await loadPlans();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const statusClass = (status: string) => status.toLowerCase().replace('_', '-');

  return (
    <section className="stack-lg">
      <div className="page-header">
        <PageHero
          emoji="🎯"
          title="Your detox plans"
          subtitle="Small steps beat perfect streaks. Pick a plan and check in when you can — no guilt trips."
        />
        {(role === 'COACH' || role === 'ADMIN') && (
          <button type="button" className={showCreate ? 'secondary' : ''} onClick={() => setShowCreate((v) => !v)}>
            {showCreate ? 'Cancel' : '+ New plan'}
          </button>
        )}
      </div>

      <div className="filters row card" style={{ padding: '0.85rem 1rem' }}>
        <input
          placeholder="Search plans..."
          value={titleFilter}
          onChange={(e) => {
            setPage(0);
            setTitleFilter(e.target.value);
          }}
        />
        <select
          value={statusFilter}
          onChange={(e) => {
            setPage(0);
            setStatusFilter(e.target.value);
          }}
        >
          <option value="">All vibes</option>
          {['DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED', 'ARCHIVED'].map((s) => (
            <option key={s} value={s}>
              {planStatusLabel(s)}
            </option>
          ))}
        </select>
      </div>

      {showCreate && (
        <form className="card stack card--pop" onSubmit={handleSubmit(onCreatePlan)}>
          <div className="section-head">
            <span className="section-head__icon" aria-hidden="true">
              📝
            </span>
            <h2>Set up a new plan</h2>
          </div>
          <label>
            Member
            <select {...register('memberProfileUuid')}>
              <option value="">Choose a member</option>
              {members.map((m) => (
                <option key={m.uuid} value={m.uuid}>
                  {m.displayName} ({m.username})
                </option>
              ))}
            </select>
            {errors.memberProfileUuid && <span className="error">{errors.memberProfileUuid.message}</span>}
          </label>
          <label>
            Plan name
            <input {...register('title')} placeholder="e.g. Social media reset week" />
          </label>
          <label>
            Start date
            <input type="date" {...register('startDate')} />
          </label>
          <label>
            Status
            <select {...register('status')}>
              {['DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED', 'ARCHIVED'].map((s) => (
                <option key={s} value={s}>
                  {planStatusLabel(s)}
                </option>
              ))}
            </select>
          </label>
          <label>
            Daily screen-time target (minutes)
            <input type="number" {...register('targetScreenMinutes', { valueAsNumber: true })} placeholder="120" />
          </label>
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Saving...' : 'Create plan'}
          </button>
        </form>
      )}

      {error && <p className="error-banner">{error}</p>}

      {plans.length === 0 ? (
        <div className="card empty-state">
          <span className="empty-state__emoji" aria-hidden="true">
            🌱
          </span>
          <p>No plans yet — your journey starts with one small step.</p>
        </div>
      ) : (
        <div className="bento-grid">
          {plans.map((plan) => (
            <article key={plan.uuid} className="card plan-card">
              <h3>{plan.title}</h3>
              <p className="plan-card__meta muted">
                {plan.memberDisplayName} · Coach {plan.coachDisplayName}
              </p>
              <p>
                <span className={`badge ${statusClass(plan.status)}`}>{planStatusLabel(plan.status)}</span>
              </p>
              <p className="plan-card__stat">
                <span aria-hidden="true">📱</span>
                Target: {plan.targetScreenMinutes ?? '—'} min / day
              </p>
              <Link to={`/plans/${plan.uuid}`} className="plan-card__link">
                Open plan →
              </Link>
            </article>
          ))}
        </div>
      )}

      <div className="pagination row">
        <button type="button" className="secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
          ← Back
        </button>
        <span>
          {page + 1} / {Math.max(totalPages, 1)}
        </span>
        <button
          type="button"
          className="secondary"
          disabled={page + 1 >= totalPages}
          onClick={() => setPage((p) => p + 1)}
        >
          Next →
        </button>
      </div>
    </section>
  );
}
