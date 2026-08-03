import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { roleLabel } from '../utils/labels';

export function Header() {
  const { displayName, role, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="topbar">
      <Link to="/" className="brand">
        <span className="brand__icon" aria-hidden="true">
          📵
        </span>
        <span>
          Un<span className="brand__accent">plug</span>
        </span>
      </Link>

      {isAuthenticated && (
        <nav>
          <Link to="/plans" className="nav-link">
            My plans
          </Link>
          {role === 'ADMIN' && (
            <Link to="/admin" className="nav-link">
              Admin
            </Link>
          )}
          <span className="user-chip">
            {displayName}
            <span className="user-chip__role">{roleLabel(role ?? '')}</span>
          </span>
          <button type="button" className="secondary" onClick={handleLogout}>
            Log out
          </button>
        </nav>
      )}
    </header>
  );
}

