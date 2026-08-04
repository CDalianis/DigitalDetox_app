import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { PasswordInput } from '../components/PasswordInput';
import { getErrorMessage, useAuth } from '../context/AuthContext';
import { loginSchema } from '../schemas/auth';
import type { LoginForm } from '../types/forms';
import { useState } from 'react';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });

  const onSubmit = async (data: LoginForm) => {
    setError('');
    try {
      await login(data);
      navigate('/plans');
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <section className="card narrow auth-card card--pop">
      <span className="page-hero__emoji" aria-hidden="true">
        👋
      </span>
      <h1>Welcome back</h1>
      <p className="auth-card__lead">
        Pick up where you left off. No pressure — just honest check-ins and small wins.
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="stack">
        <label>
          Username
          <input {...register('username')} autoComplete="username" placeholder="your_username" />
          {errors.username && <span className="error">{errors.username.message}</span>}
        </label>
        <label>
          Password
          <PasswordInput {...register('password')} autoComplete="current-password" placeholder="••••••••" />
          {errors.password && <span className="error">{errors.password.message}</span>}
        </label>
        {error && <p className="error-banner">{error}</p>}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Signing you in...' : "Let's go"}
        </button>
      </form>
      <p className="auth-footer">
        New here? <Link to="/register">Create your account</Link>
      </p>
    </section>
  );
}
