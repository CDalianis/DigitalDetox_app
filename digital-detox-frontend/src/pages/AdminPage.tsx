import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { apiFetch } from '../api/client';
import type { Coach } from '../api/types';
import { getErrorMessage, useAuth } from '../context/AuthContext';

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
      <h1>Admin · Coach approvals</h1>
      {error && <p className="error">{error}</p>}
      {pending.length === 0 ? (
        <p className="muted">No pending coaches.</p>
      ) : (
        <div className="grid">
          {pending.map((coach) => (
            <article key={coach.uuid} className="card">
              <h3>{coach.displayName}</h3>
              <p>{coach.specialty ?? 'No specialty'}</p>
              <p className="muted">{coach.email}</p>
              <button type="button" onClick={() => void approve(coach.uuid)}>
                Approve
              </button>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
