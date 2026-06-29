import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useParams } from 'react-router-dom';
import { apiDownload, apiFetch, type PageResponse } from '../api/client';
import type { Attachment, CheckIn, DetoxPlan, Goal, WeeklyReview } from '../api/types';
import { getErrorMessage, useAuth } from '../context/AuthContext';
import { checkInSchema, goalSchema, reviewSchema } from '../schemas/auth';
import type { CheckInForm, GoalForm, ReviewForm } from '../types/forms';

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
    return <p>Loading plan...</p>;
  }

  return (
    <section className="stack-lg">
      <Link to="/plans">← Back to plans</Link>
      <div className="card">
        <h1>{plan.title}</h1>
        <p>{plan.description}</p>
        <p className="muted">
          Member: {plan.memberDisplayName} · Coach: {plan.coachDisplayName}
        </p>
        <p>
          Status: <span className={`badge ${plan.status.toLowerCase()}`}>{plan.status}</span>
        </p>
      </div>

      <div className="card stack">
        <h2>Goals</h2>
        {goals.length === 0 && <p className="muted">No goals yet.</p>}
        <div className="grid">
          {goals.map((goal) => (
            <article key={goal.uuid} className="goal-item">
              <strong>{goal.title}</strong>
              {goal.description && <p className="muted">{goal.description}</p>}
              <p>
                {goal.metricType.replace('_', ' ')}: {goal.currentValue ?? 0} / {goal.targetValue}
              </p>
              <span className={`badge ${goal.status.toLowerCase().replace('_', '-')}`}>{goal.status}</span>
            </article>
          ))}
        </div>

        {canManageGoals && (
          <form className="stack" onSubmit={goalForm.handleSubmit(onSubmitGoal)}>
            <h3>Add goal</h3>
            <label>
              Title
              <input {...goalForm.register('title')} />
              {goalForm.formState.errors.title && (
                <span className="error">{goalForm.formState.errors.title.message}</span>
              )}
            </label>
            <label>
              Description
              <textarea {...goalForm.register('description')} rows={2} />
            </label>
            <div className="row">
              <label>
                Metric
                <select {...goalForm.register('metricType')}>
                  <option value="SCREEN_MINUTES">Screen minutes</option>
                  <option value="SOCIAL_MINUTES">Social minutes</option>
                  <option value="SLEEP_HOURS">Sleep hours</option>
                  <option value="CUSTOM">Custom</option>
                </select>
              </label>
              <label>
                Target
                <input type="number" {...goalForm.register('targetValue', { valueAsNumber: true })} />
              </label>
              <label>
                Status
                <select {...goalForm.register('status')}>
                  <option value="PENDING">Pending</option>
                  <option value="IN_PROGRESS">In progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="FAILED">Failed</option>
                </select>
              </label>
            </div>
            <button type="submit" disabled={goalForm.formState.isSubmitting}>
              Add goal
            </button>
          </form>
        )}
      </div>

      <div className="card stack">
        <h2>Weekly reviews</h2>
        {reviews.length === 0 && <p className="muted">No weekly reviews yet.</p>}
        {reviews.map((review) => (
          <article key={review.uuid} className="review-item">
            <div className="row">
              <strong>Week of {review.weekStart}</strong>
              {review.riskLevel && (
                <span className={`badge risk-${review.riskLevel.toLowerCase()}`}>{review.riskLevel}</span>
              )}
            </div>
            <p className="muted">Coach: {review.coachDisplayName}</p>
            {review.summary && <p>{review.summary}</p>}
            {review.recommendation && <p className="muted">Recommendation: {review.recommendation}</p>}
          </article>
        ))}

        {canAddReview && (
          <form className="stack" onSubmit={reviewForm.handleSubmit(onSubmitReview)}>
            <h3>Add weekly review</h3>
            <label>
              Week start
              <input type="date" {...reviewForm.register('weekStart')} />
              {reviewForm.formState.errors.weekStart && (
                <span className="error">{reviewForm.formState.errors.weekStart.message}</span>
              )}
            </label>
            <label>
              Summary
              <textarea {...reviewForm.register('summary')} rows={3} />
            </label>
            <label>
              Recommendation
              <textarea {...reviewForm.register('recommendation')} rows={2} />
            </label>
            <label>
              Risk level
              <select {...reviewForm.register('riskLevel')}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </select>
            </label>
            <button type="submit" disabled={reviewForm.formState.isSubmitting}>
              Publish review
            </button>
          </form>
        )}
      </div>

      {canAddCheckIn && (
        <form className="card stack" onSubmit={checkInForm.handleSubmit(onSubmitCheckIn)}>
          <h2>Daily check-in</h2>
          <label>
            Date
            <input type="date" {...checkInForm.register('entryDate')} />
            {checkInForm.formState.errors.entryDate && (
              <span className="error">{checkInForm.formState.errors.entryDate.message}</span>
            )}
          </label>
          <label>
            Total screen minutes
            <input type="number" {...checkInForm.register('totalScreenMinutes', { valueAsNumber: true })} />
          </label>
          <label>
            Social media minutes
            <input type="number" {...checkInForm.register('socialMediaMinutes', { valueAsNumber: true })} />
          </label>
          <label>
            Sleep hours
            <input type="number" step="0.5" {...checkInForm.register('sleepHours', { valueAsNumber: true })} />
          </label>
          <label>
            Focus (1-10)
            <input type="number" {...checkInForm.register('focusScore', { valueAsNumber: true })} />
          </label>
          <label>
            Notes
            <textarea {...checkInForm.register('notes')} rows={2} />
          </label>
          <button type="submit" disabled={checkInForm.formState.isSubmitting}>
            Submit check-in
          </button>
        </form>
      )}

      <div className="card stack">
        <h2>Check-in history</h2>
        <div className="filters row">
          <input type="date" value={fromDate} onChange={(e) => { setPage(0); setFromDate(e.target.value); }} />
          <input type="date" value={toDate} onChange={(e) => { setPage(0); setToDate(e.target.value); }} />
        </div>
        {checkIns.map((checkIn) => (
          <article key={checkIn.uuid} className="checkin-item">
            <strong>{checkIn.entryDate}</strong>
            <p>Screen: {checkIn.totalScreenMinutes} min · Social: {checkIn.socialMediaMinutes ?? '—'} min</p>
            <p>Focus: {checkIn.focusScore ?? '—'} · Stress: {checkIn.stressLevel ?? '—'}</p>
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
                      {attachment.originalFilename} ({attachment.contentType})
                    </button>
                  </li>
                ))}
              </ul>
            )}
            {canAddCheckIn && (
              <label className="file-upload">
                Upload screenshot
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
        ))}
        <div className="pagination row">
          <button type="button" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <span>
            Page {page + 1} of {Math.max(totalPages, 1)}
          </span>
          <button type="button" disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>
            Next
          </button>
        </div>
      </div>

      {error && <p className="error">{error}</p>}
    </section>
  );
}
