import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { apiFetch } from '../api/client';
import { PasswordInput } from '../components/PasswordInput';
import { getErrorMessage, useAuth } from '../context/AuthContext';
import { markNeedsOnboarding } from '../onboarding';
import { coachRegisterSchema, memberRegisterSchema } from '../schemas/auth';
import { z } from 'zod';

type RegisterType = 'member' | 'coach';

export function RegisterPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [type, setType] = useState<RegisterType>('member');
  const [error, setError] = useState('');

  const schema = type === 'member' ? memberRegisterSchema : coachRegisterSchema;
  type FormData = z.infer<typeof schema>;

  const {
    register,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const onSubmit = async (data: FormData) => {
    setError('');
    try {
      const path = type === 'member' ? '/members/register' : '/coaches/register';
      await apiFetch(path, { method: 'POST', body: JSON.stringify(data) }, false);
      markNeedsOnboarding(data.user.username);
      try {
        await login({ username: data.user.username, password: data.user.password });
        navigate('/plans');
      } catch {
        navigate('/login');
      }
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <section className="card narrow auth-card card--mint">
      <span className="page-hero__emoji" aria-hidden="true">
        ✨
      </span>
      <h1>Join Unplug</h1>
      <p className="auth-card__lead">
        {type === 'member'
          ? 'Build healthier screen habits at your own pace. Your coach has your back.'
          : 'Help teens take control of their screen time — supportive, not preachy.'}
      </p>
      <div className="tabs">
        <button type="button" className={type === 'member' ? 'active' : ''} onClick={() => setType('member')}>
          I'm a teen / member
        </button>
        <button type="button" className={type === 'coach' ? 'active' : ''} onClick={() => setType('coach')}>
          I'm a coach
        </button>
      </div>
      <form key={type} onSubmit={handleSubmit(onSubmit)} className="stack">
        <label>
          Username
          <input {...register('user.username')} placeholder="pick something cool" />
        </label>
        <label>
          Email
          <input type="email" {...register('user.email')} placeholder="you@email.com" />
        </label>
        <label>
          Password
          <PasswordInput {...register('user.password')} autoComplete="new-password" placeholder="min. 8 characters" />
        </label>
        <label>
          What should we call you?
          <input {...register('displayName')} placeholder="Your display name" />
        </label>
        {type === 'member' ? (
          <>
            <label>
              Timezone
              <input {...register('timezone')} placeholder="Europe/Athens" />
            </label>
            <label>
              What's your main goal?
              <input {...register('mainGoal')} placeholder="e.g. less TikTok before bed" />
            </label>
          </>
        ) : (
          <>
            <label>
              Your specialty
              <input {...register('specialty')} placeholder="e.g. teen wellness" />
            </label>
            <label>
              Years of experience
              <input type="number" {...register('yearsExperience', { valueAsNumber: true })} />
            </label>
            <label>
              Short bio
              <textarea {...register('bio')} rows={3} placeholder="Tell members a bit about you..." />
            </label>
          </>
        )}
        {error && <p className="error-banner">{error}</p>}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>
      <p className="auth-footer">
        Already in? <Link to="/login">Sign in</Link>
      </p>
    </section>
  );
}
