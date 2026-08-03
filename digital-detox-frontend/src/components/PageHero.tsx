type PageHeroProps = {
  emoji: string;
  title: string;
  subtitle?: string;
};

export function PageHero({ emoji, title, subtitle }: PageHeroProps) {
  return (
    <header className="page-hero">
      <span className="page-hero__emoji" aria-hidden="true">
        {emoji}
      </span>
      <div>
        <h1>{title}</h1>
        {subtitle && <p className="page-hero__subtitle">{subtitle}</p>}
      </div>
    </header>
  );
}
