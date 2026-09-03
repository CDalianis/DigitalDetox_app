import { useEffect, useId, useRef, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getOnboardingSteps } from '../onboarding';

export function OnboardingWizard() {
  const { isAuthenticated, needsOnboarding, completeOnboarding, role, displayName } = useAuth();
  const [stepIndex, setStepIndex] = useState(0);
  const dialogRef = useRef<HTMLDivElement>(null);
  const titleId = useId();
  const descId = useId();

  const open = isAuthenticated && needsOnboarding;
  const steps = getOnboardingSteps(role);
  const step = steps[stepIndex] ?? steps[0];
  const isLast = stepIndex === steps.length - 1;

  useEffect(() => {
    if (open) {
      setStepIndex(0);
      dialogRef.current?.focus();
    }
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const previous = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = previous;
    };
  }, [open]);

  useEffect(() => {
    if (!open) return;

    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        completeOnboarding();
      } else if (event.key === 'ArrowRight') {
        setStepIndex((index) => Math.min(index + 1, steps.length - 1));
      } else if (event.key === 'ArrowLeft') {
        setStepIndex((index) => Math.max(index - 1, 0));
      }
    };

    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, completeOnboarding, steps.length]);

  if (!open || !step) {
    return null;
  }

  const goNext = () => {
    if (isLast) {
      completeOnboarding();
      return;
    }
    setStepIndex((index) => index + 1);
  };

  return (
    <div className="onboarding-overlay" role="presentation">
      <div
        className="onboarding-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        aria-describedby={descId}
        tabIndex={-1}
        ref={dialogRef}
      >
        <p className="onboarding-kicker">
          {displayName ? `Hey ${displayName}` : 'Quick tour'} · {stepIndex + 1} / {steps.length}
        </p>
        <span className="onboarding-emoji" aria-hidden="true">
          {step.emoji}
        </span>
        <h2 id={titleId}>{step.title}</h2>
        <p id={descId} className="onboarding-body">
          {step.body}
        </p>

        <div className="onboarding-dots" aria-hidden="true">
          {steps.map((item, index) => (
            <span
              key={item.title}
              className={`onboarding-dot${index === stepIndex ? ' is-active' : ''}`}
            />
          ))}
        </div>

        <div className="onboarding-actions">
          <button type="button" className="ghost" onClick={completeOnboarding}>
            Skip
          </button>
          <div className="onboarding-actions__nav">
            {stepIndex > 0 && (
              <button type="button" className="secondary" onClick={() => setStepIndex((index) => index - 1)}>
                Back
              </button>
            )}
            <button type="button" onClick={goNext}>
              {isLast ? "Let's go" : 'Next'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
