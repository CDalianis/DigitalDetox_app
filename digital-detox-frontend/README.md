# Digital Detox — React Frontend

React + TypeScript SPA for the Digital Detox platform.

> **Full documentation:** see [DIGITAL-DETOX-README.md](../DIGITAL-DETOX-README.md) in the workspace root for setup, demo walkthrough, and API integration details.

## Quick start

```bash
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). The dev server proxies `/api` to the backend at `http://localhost:8080`.

**Requires the backend running** — see `digital-detox-project/README.md`.

## Stack

- React 19, TypeScript, Vite 8
- React Router 7
- **Zod** — form/API validation schemas
- **react-hook-form** — controlled forms with Zod resolver
- **JWT** — stored in `localStorage`, sent as `Authorization: Bearer`

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Dev server (port 5173) |
| `npm run build` | Typecheck + production build |
| `npm run preview` | Preview production build |
| `npm run lint` | ESLint |

## Environment

```env
VITE_API_BASE_URL=/api/v1
```

## Pages

| Route | Description |
|-------|-------------|
| `/login` | JWT login |
| `/register` | Member / coach registration |
| `/plans` | Plan list and creation |
| `/plans/:uuid` | Goals, reviews, check-ins, file upload/download |
| `/admin` | Coach approval (admin only) |
