# Cinema E-Booking System (CES)

## Quick start

### Mac

```bash
cd path/to/software-engineering-project
bash setup.sh
npm start
```

### Windows

```bat
cd path\to\software-engineering-project
.\setup.cmd
npm start
```

1. **`cd`** into the project folder, wherever you saved it.
2. **Setup** checks for Java, Node.js, and MongoDB, and installs anything missing. Run it once per computer.
3. **`npm start`** starts the app and opens http://localhost:5173. Press **Ctrl + C** to stop everything.

After the first time, you only need steps 1 and 3.

---

## Troubleshooting

- **"… was not found. Run setup first"**: run the setup command again. If a tool was just installed, close the terminal, open a new one, and try again.
- **"Port 8080/5173 is already in use"**: the app is already running in another terminal. Press Ctrl + C there first.
- **Something failed while starting**: the error shows the last log lines. Full logs are in the `.logs/` folder, or run `npm start -- --verbose` to see everything live.
- **Windows asks for permission during setup**: click **Yes**. That's the installer for Java, Node.js, or MongoDB.

---

## Project details

Sprint 1: a React + Vite frontend, a Spring Boot REST API, and MongoDB.

```
frontend/                  React 18 + TypeScript + Tailwind (Vite dev server, port 5173)
cinema-ebooking-backend/   Spring Boot 3.2 + Spring Data MongoDB (port 8080)
setup.sh / setup.cmd       One-time setup (Mac / Windows)
scripts/start.mjs          What `npm start` runs
```

Requirements, installed by setup: **Java 17+**, **Node.js 18+**, **MongoDB**. Maven is not needed; the backend uses the Maven Wrapper (`mvnw`), which downloads Maven itself.

`npm start` uses MongoDB if one is already running on port 27017. Otherwise it starts its own, stores data in `.data/mongo`, and stops it on Ctrl + C. The backend seeds 13 movies automatically the first time.

- App: http://localhost:5173
- API: http://localhost:8080/api/v1/movies
- Swagger UI: http://localhost:8080/swagger-ui.html

### Running the parts by hand (optional)

```bash
cd cinema-ebooking-backend && ./mvnw spring-boot:run     # Windows: mvnw.cmd spring-boot:run
cd frontend && npm install && npm run dev
```

MongoDB must be running first (on Mac: `brew services start mongodb-community`). Running `mvnw` by hand also needs Java on your `PATH` or `JAVA_HOME` set. `npm start` and setup find Java for you, so they don't need this. With Homebrew's Java 21 on a Mac: `export JAVA_HOME=$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home`.

### Configuration

These are set in `cinema-ebooking-backend/src/main/resources/application.properties`. Any of them can be overridden with an environment variable.

| Env var | Default | Purpose |
|---------|---------|---------|
| `MONGODB_URI` | `mongodb://localhost:27017/cinema_ebooking` | Database connection (for example, a MongoDB Atlas URI) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,...` | Origins allowed to call the API directly |
| `SEED_RESET` | `false` | `true` wipes the movies collection and re-seeds it on startup |

Each running movie has a weekly schedule (`showDays`) in the database, and the show-date filter matches on it. For example, *The Brutalist* only plays on weekends. A database seeded before schedules existed is re-seeded automatically on the next startup.

To re-seed after changing `DataSeeder.java`, run `SEED_RESET=true npm start`.

### API

All responses are wrapped as `{ success, data, message, total, ... }`.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/movies?status=` | All movies. Optional status: `CURRENTLY_RUNNING` or `COMING_SOON` |
| GET | `/api/v1/movies/{id}` | A single movie (404 if it doesn't exist) |
| GET | `/api/v1/movies/search?title=` | Case-insensitive partial title search |
| GET | `/api/v1/movies/filter?genre=&rating=&status=` | Filter by genre, rating, and/or status |
| GET | `/api/v1/movies/filter?showDate=YYYY-MM-DD[&showDate=...][&genre=]` | Currently running movies shown on those dates (repeat `showDate` for several, e.g. a weekend) |
| GET | `/api/v1/movies/genres` | Genres for the filter bar |

### Tests

```bash
cd cinema-ebooking-backend && ./mvnw test   # backend unit tests (Windows: mvnw.cmd test)
cd frontend && npm run build               # type-check and production build
```

### Sprint 1 feature map

| Requirement | Where it lives |
|-------------|----------------|
| Home page from DB, Currently Running / Coming Soon, showtimes | `frontend/src/pages/Home.tsx`, `GET /movies` |
| Search by title, with a message when nothing matches | Home search bar, `GET /movies/search` |
| Filter by genre and show date (Today / Tomorrow / This Weekend) | Home filter bar, `GET /movies/filter?genre=&showDate=` |
| Movie details: poster, rating, description, trailer, showtimes | `frontend/src/pages/MovieDetails.tsx`, `GET /movies/{id}` |
| Embedded, playable trailer | YouTube iframe on the details page |
| Booking prototype: ticket types and prices, seat selection | `frontend/src/pages/Booking.tsx` (UI only) |
| At least 10 seeded movies, multiple genres, both statuses | `cinema-ebooking-backend/.../util/DataSeeder.java` |
