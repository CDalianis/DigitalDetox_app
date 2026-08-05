import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useParams } from 'react-router-dom';
import { apiDownload, apiFetch, type PageResponse } from '../api/client';
import type { Attachment, CheckIn, DetoxPlan, Goal, WeeklyReview } from '../api/types';
import { getErrorMessage, useAuth } from '../context/AuthContext';
import { checkInSchema, goalSchema, reviewSchema } from '../schemas/auth';
import type { CheckInForm, GoalForm, ReviewForm } from '../types/forms';
import { goalStatusLabel, planStatusLabel, riskLabel } from '../utils/labels';

export function PlanDetailPage() {
  const { uuid } = useParams<{ uuid: string }>();
  const { role } = useAuth();
  const [plan, setPlan] = useState<DetoxPlan | null>(null);
  const [goals, setGoals] = useState<Goal[]>([]);
  const [reviews, setReviews] = useState<WeeklyReview[]>([]);
  const [checkIns, setCheckIns] = useState<CheckIn[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [error, setError] = useState('');
  const [uploadingFor, setUploadingFor] = useState<string | null>(null);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);

  const canAddCheckIn = role === 'MEMBER' || role === 'ADMIN';
  const canManageGoals = role === 'COACH' || role === 'ADMIN';
  const canAddReview = role === 'COACH' || role === 'ADMIN';

  const checkInForm = useForm<CheckInForm>({ resolver: zodResolver(checkInSchema) });
  const goalForm = useForm<GoalForm>({
    resolver: zodResolver(goalSchema),
    defaultValues: {
      metricType: 'SCREEN_MINUTES',
      status: 'PENDING',
      targetValue: 120,
    },
  });
  const reviewForm = useForm<ReviewForm>({
    resolver: zodResolver(reviewSchema),
    defaultValues: { riskLevel: 'LOW' },
  });

  const loadPlan = async () => {
    if (!uuid) return;
    const data = await apiFetch<DetoxPlan>(`/plans/${uuid}`);
    setPlan(data);
  };

  const loadGoals = async () => {
    if (!uuid) return;
    const data = await apiFetch<Goal[]>(`/plans/${uuid}/goals`);
    setGoals(data);
  };

  const loadReviews = async () => {
    if (!uuid) return;
    const data = await apiFetch<WeeklyReview[]>(`/plans/${uuid}/reviews`);
    setReviews(data);
  };

  const loadCheckIns = async () => {
    if (!uuid) return;
    const params = new URLSearchParams({ page: String(page), size: '5', sort: 'entryDate,desc' });
    if (fromDate) params.set('fromDate', fromDate);
    if (toDate) params.set('toDate', toDate);
    const data = await apiFetch<PageResponse<CheckIn>>(`/plans/${uuid}/check-ins?${params}`);
    setCheckIns(data.content);
    setTotalPages(data.totalPages);
  };

  useEffect(() => {
    void loadPlan().catch((err) => setError(getErrorMessage(err)));
    void loadGoals().catch((err) => setError(getErrorMessage(err)));
    void loadReviews().catch((err) => setError(getErrorMessage(err)));
  }, [uuid]);

  useEffect(() => {
    void loadCheckIns().catch((err) => setError(getErrorMessage(err)));
  }, [uuid, page, fromDate, toDate]);

  const onSubmitCheckIn = async (data: CheckInForm) => {
    if (!uuid) return;
    setError('');
    try {
      await apiFetch(`/plans/${uuid}/check-ins`, { method: 'POST', body: JSON.stringify(data) });
      checkInForm.reset();
      await loadCheckIns();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const onSubmitGoal = async (data: GoalForm) => {
    if (!uuid) return;
    setError('');
    try {
      await apiFetch(`/plans/${uuid}/goals`, { method: 'POST', body: JSON.stringify(data) });
      goalForm.reset({
        metricType: 'SCREEN_MINUTES',
        status: 'PENDING',
        targetValue: 120,
        title: '',
        description: '',
      });
      await loadGoals();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const onSubmitReview = async (data: ReviewForm) => {
    if (!uuid) return;
    setError('');
    try {
      await apiFetch(`/plans/${uuid}/reviews`, { method: 'POST', body: JSON.stringify(data) });
      reviewForm.reset({ riskLevel: 'LOW', weekStart: '', summary: '', recommendation: '' });
      await loadReviews();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const uploadFile = async (checkInUuid: string, file: File) => {
    setUploadingFor(checkInUuid);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', file);
      await apiFetch(`/check-ins/${checkInUuid}/attachments`, { method: 'POST', body: formData });
      await loadCheckIns();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setUploadingFor(null);
    }
  };

  const downloadFile = async (checkInUuid: string, attachment: Attachment) => {
    setDownloadingId(attachment.uuid);
    setError('');
    try {
      const blob = await apiDownload(`/check-ins/${checkInUuid}/attachments/${attachment.uuid}`);
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = attachment.originalFilename;
      anchor.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setDownloadingId(null);
    }
  };

  if (!plan) {
    return <p className="loading-pulse">Loading your plan...</p>;
  }

  const statusClass = plan.status.toLowerCase().replace('_', '-');

  return (
    <section className="stack-lg">
      <Link to="/plans" className="back-link">
        ← Back to plans
      </Link>

      <div className="card card--pop">
        <h1>{plan.title}</h1>
        {plan.description && <p className="muted">{plan.description}</p>}
        <p className="muted">
          {plan.memberDisplayName} · Coach {plan.coachDisplayName}
        </p>
        <p>
          <span className={`badge ${statusClass}`}>{planStatusLabel(plan.status)}</span>
        </p>
      </div>

      <div className="card stack">
        <div className="section-head">
          <span className="section-head__icon" aria-hidden="true">
            🏆
          </span>
          <h2>Your goals</h2>
        </div>
        {goals.length === 0 ? (
          <p className="hint">No goals yet — your coach can add some targets to aim for.</p>
        ) : (
          <div className="grid">
            {goals.map((goal) => (
              <article key={goal.uuid} className="goal-item">
                <strong>{goal.title}</strong>
                {goal.description && <p className="muted">{goal.description}</p>}
                <p>
                  {goal.metricType.replace(/_/g, ' ').toLowerCase()}: {goal.currentValue ?? 0} / {goal.targetValue}
                </p>
                <span className={`badge ${goal.status.toLowerCase().replace('_', '-')}`}>
                  {goalStatusLabel(goal.status)}
                </span>
              </article>
            ))}
          </div>
        )}

        {canManageGoals && (
          <form className="stack" onSubmit={goalForm.handleSubmit(onSubmitGoal)}>
            <h3>Add a goal</h3>
            <label>
              Goal name
              <input {...goalForm.register('title')} placeholder="e.g. Under 2h social media" />
              {goalForm.formState.errors.title && (
                <span className="error">{goalForm.formState.errors.title.message}</span>
              )}
            </label>
            <label>
              Why this matters
              <textarea {...goalForm.register('description')} rows={2} placeholder="Optional note..." />
            </label>
            <div className="row">
              <label>
                What to track
                <select {...goalForm.register('metricType')}>
                  <option value="SCREEN_MINUTES">Screen time</option>
                  <option value="SOCIAL_MINUTES">Social media</option>
                  <option value="SLEEP_HOURS">Sleep</option>
                  <option value="CUSTOM">Something else</option>
                </select>
              </label>
              <label>
                Target
                <input type="number" {...goalForm.register('targetValue', { valueAsNumber: true })} />
              </label>
              <label>
                Status
                <select {...goalForm.register('status')}>
                  <option value="PENDING">Up next</option>
                  <option value="IN_PROGRESS">In progress</option>
                  <option value="COMPLETED">Done</option>
                  <option value="FAILED">Tough week</option>
                </select>
              </label>
            </div>
            <button type="submit" disabled={goalForm.formState.isSubmitting}>
              Add goal
            </button>
          </form>
        )}
      </div>

      <div className="card stack card--coral">
        <div className="section-head">
          <span className="section-head__icon" aria-hidden="true">
            💬
          </span>
          <h2>Coach check-ins</h2>
        </div>
        <p className="hint">Weekly notes from your coach — honest feedback, zero judgment.</p>
        {reviews.length === 0 ? (
          <p className="muted">Nothing here yet. Your coach will drop a note soon.</p>
        ) : (
          reviews.map((review) => (
            <article key={review.uuid} className="review-item">
              <div className="row">
                <strong>Week of {review.weekStart}</strong>
                {review.riskLevel && (
                  <span className={`badge risk-${review.riskLevel.toLowerCase()}`}>
                    {riskLabel(review.riskLevel)}
                  </span>
                )}
              </div>
              <p className="muted">From {review.coachDisplayName}</p>
              {review.summary && <p>{review.summary}</p>}
              {review.recommendation && <p className="hint">💡 {review.recommendation}</p>}
            </article>
          ))
        )}

        {canAddReview && (
          <form className="stack" onSubmit={reviewForm.handleSubmit(onSubmitReview)}>
            <h3>Leave a weekly note</h3>
            <label>
              Week starting
              <input type="date" {...reviewForm.register('weekStart')} />
              {reviewForm.formState.errors.weekStart && (
                <span className="error">{reviewForm.formState.errors.weekStart.message}</span>
              )}
            </label>
            <label>
              How did the week go?
              <textarea {...reviewForm.register('summary')} rows={3} placeholder="Keep it real but kind..." />
            </label>
            <label>
              Tip for next week
              <textarea {...reviewForm.register('recommendation')} rows={2} placeholder="One thing to try..." />
            </label>
            <label>
              How are they doing?
              <select {...reviewForm.register('riskLevel')}>
                <option value="LOW">Chill — on track</option>
                <option value="MEDIUM">Watch it — needs attention</option>
                <option value="HIGH">Needs extra support</option>
              </select>
            </label>
            <button type="submit" disabled={reviewForm.formState.isSubmitting}>
              Send review
            </button>
          </form>
        )}
      </div>

      {canAddCheckIn && (
        <form className="card stack card--mint" onSubmit={checkInForm.handleSubmit(onSubmitCheckIn)}>
          <div className="section-head">
            <span className="section-head__icon" aria-hidden="true">
              📊
            </span>
            <h2>Today's check-in</h2>
          </div>
          <p className="hint">Be honest — this is for you, not a report card. Missed a day? That's okay.</p>
          <label>
            Date
            <input type="date" {...checkInForm.register('entryDate')} />
            {checkInForm.formState.errors.entryDate && (
              <span className="error">{checkInForm.formState.errors.entryDate.message}</span>
            )}
          </label>
          <label>
            Total screen time (minutes)
            <input type="number" {...checkInForm.register('totalScreenMinutes', { valueAsNumber: true })} placeholder="e.g. 180" />
          </label>
          <label>
            Social media (minutes)
            <input type="number" {...checkInForm.register('socialMediaMinutes', { valueAsNumber: true })} placeholder="e.g. 90" />
          </label>
          <label>
            Sleep (hours)
            <input type="number" step="0.5" {...checkInForm.register('sleepHours', { valueAsNumber: true })} placeholder="e.g. 7.5" />
          </label>
          <label>
            Focus level (1–10)
            <input type="number" {...checkInForm.register('focusScore', { valueAsNumber: true })} placeholder="How locked-in did you feel?" />
          </label>
          <label>
            Anything on your mind?
            <textarea {...checkInForm.register('notes')} rows={2} placeholder="Optional — vent, celebrate, whatever." />
          </label>
          <button type="submit" disabled={checkInForm.formState.isSubmitting}>
            {checkInForm.formState.isSubmitting ? 'Saving...' : 'Log my day'}
          </button>
        </form>
      )}

      <div className="card stack">
        <div className="section-head">
          <span className="section-head__icon" aria-hidden="true">
            📅
          </span>
          <h2>Your history</h2>
        </div>
        <div className="filters row">
          <input type="date" value={fromDate} onChange={(e) => { setPage(0); setFromDate(e.target.value); }} />
          <input type="date" value={toDate} onChange={(e) => { setPage(0); setToDate(e.target.value); }} />
        </div>
        {checkIns.length === 0 ? (
          <div className="empty-state">
            <span className="empty-state__emoji" aria-hidden="true">
              📭
            </span>
            <p>No check-ins yet. Your first one takes less than a minute.</p>
          </div>
        ) : (
          checkIns.map((checkIn) => (
            <article key={checkIn.uuid} className="checkin-item">
              <span className="checkin-item__date">{checkIn.entryDate}</span>
              <div className="checkin-stats">
                <span className="stat-pill">📱 {checkIn.totalScreenMinutes} min screen</span>
                <span className="stat-pill">💬 {checkIn.socialMediaMinutes ?? '—'} min social</span>
                <span className="stat-pill">🎯 Focus {checkIn.focusScore ?? '—'}/10</span>
                {checkIn.stressLevel != null && (
                  <span className="stat-pill">😮‍💨 Stress {checkIn.stressLevel}/10</span>
                )}
              </div>
              {checkIn.notes && <p className="muted">{checkIn.notes}</p>}
              {checkIn.attachments?.length > 0 && (
                <ul className="attachment-list">
                  {checkIn.attachments.map((attachment) => (
                    <li key={attachment.uuid}>
                      <button
                        type="button"
                        className="link-button"
                        disabled={downloadingId === attachment.uuid}
                        onClick={() => void downloadFile(checkIn.uuid, attachment)}
                      >
                        📎 {attachment.originalFilename}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
              {canAddCheckIn && (
                <label className="file-upload">
                  📸 Drop a screen-time screenshot
                  <input
                    type="file"
                    accept="image/*,.pdf"
                    disabled={uploadingFor === checkIn.uuid}
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) void uploadFile(checkIn.uuid, file);
                    }}
                  />
                </label>
              )}
            </article>
          ))
        )}
        <div className="pagination row">
          <button type="button" className="secondary" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            ← Back
          </button>
          <span>
            {page + 1} / {Math.max(totalPages, 1)}
          </span>
          <button type="button" className="secondary" disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>
            Next →
          </button>
        </div>
      </div>

      {error && <p className="error-banner">{error}</p>}
    </section>
  );
}
