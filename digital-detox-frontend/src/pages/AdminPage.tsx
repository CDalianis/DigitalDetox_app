import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { apiFetch } from '../api/client';
import type { Coach } from '../api/types';
import { getErrorMessage, useAuth } from '../context/AuthContext';
import { PageHero } from '../components/PageHero';

export function AdminPage() {
  const { role } = useAuth();
  const [pending, setPending] = useState<Coach[]>([]);
  const [error, setError] = useState('');

  if (role !== 'ADMIN') {
    return <Navigate to="/plans" replace />;
  }

  const loadPending = async () => {
    setError('');
    try {
      const data = await apiFetch<Coach[]>('/coaches/pending');
      setPending(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  useEffect(() => {
    void loadPending();
  }, []);

  const approve = async (uuid: string) => {
    try {
      await apiFetch(`/coaches/${uuid}/approve`, { method: 'PATCH' });
      await loadPending();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <section className="stack-lg">
      <PageHero
        emoji="🛡️"
        title="Coach approvals"
        subtitle="Make sure new coaches are legit before they start working with members."
      />
      {error && <p className="error-banner">{error}</p>}
      {pending.length === 0 ? (
        <div className="card empty-state">
          <span className="empty-state__emoji" aria-hidden="true">
            ✅
          </span>
          <p>All caught up — no coaches waiting for approval.</p>
        </div>
      ) : (
        <div className="bento-grid">
          {pending.map((coach) => (
            <article key={coach.uuid} className="card plan-card">
              <h3>{coach.displayName}</h3>
              <p>{coach.specialty ?? 'No specialty listed'}</p>
              <p className="muted">{coach.email}</p>
              <button type="button" className="success" onClick={() => void approve(coach.uuid)}>
                Approve coach
              </button>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
