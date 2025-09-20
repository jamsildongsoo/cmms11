# Repository Guidelines

## Project Structure & Module Organization
- `src/` — application code (modules, components, utils). Keep feature code co-located (e.g., `src/auth/`, `src/api/`).
- `tests/` — test files mirroring `src/` structure (e.g., `tests/auth/`).
- `scripts/` — developer tooling and CI helpers (bash/ps1/py).
- `assets/` — static assets (images, fixtures, sample data).
- `build/` or `dist/` — compiled artifacts (ignored by Git). Do not edit manually.

## Build, Test, and Development Commands
Use the project’s package scripts or Make targets when present.
- Install deps: `npm ci` or `pip install -r requirements.txt`
- Run dev server: `npm run dev` or `python -m src`
- Build artifacts: `npm run build` or `make build`
- Run tests: `npm test` or `pytest -q`
- Lint/format: `npm run lint && npm run format` or `ruff check && black .`
Adjust to the stack detected in this repo (Node/Python/other).

## Coding Style & Naming Conventions
- Indentation: 2 spaces (JS/TS) or 4 spaces (Python). No tabs.
- Filenames: kebab-case for JS/TS (`user-profile.ts`); snake_case for Python (`user_service.py`).
- Classes: `PascalCase`; functions/vars: `camelCase` (JS/TS) or `snake_case` (Python); constants: `UPPER_SNAKE_CASE`.
- Prefer small, pure modules; avoid cyclic deps. Keep public APIs in `index.ts` or `__init__.py`.
- Use formatters: Prettier (JS/TS) or Black (Python). Do not hand-format diffs.

## Testing Guidelines
- Place tests under `tests/` mirroring `src/` paths.
- Naming: `*.spec.ts` or `test_*.py` depending on stack.
- Aim for meaningful coverage on core logic and error paths; avoid brittle snapshot tests.
- Run locally before pushing: `npm test` or `pytest -q`.

## Commit & Pull Request Guidelines
- Commits: follow Conventional Commits (e.g., `feat: add auth token refresh`, `fix: handle null user`).
- Keep commits focused and atomic; include rationale in body if non-obvious.
- PRs: clear description, linked issue (e.g., `Closes #123`), screenshots for UI, notes on risks/rollout.
- Update docs and tests alongside code. Ensure CI passes.

## Security & Configuration
- Do not commit secrets. Use `.env` with a checked-in `.env.example`.
- Prefer configuration via env vars; document required keys in README/`.env.example`.
- Validate inputs at boundaries; avoid logging sensitive data.

