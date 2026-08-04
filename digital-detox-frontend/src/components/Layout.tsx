import type { ReactNode } from 'react';

type LayoutProps = {
  header?: ReactNode;
  footer?: ReactNode;
  children: ReactNode;
};

export function Layout({ header, children, footer }: LayoutProps) {
  return (
    <div className="app-shell">
      {header}
      <main className="content">{children}</main>
      {footer}
    </div>
  );
}
