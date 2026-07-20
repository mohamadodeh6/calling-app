# Calling App (WhatsApp-style Voice MVP)

Minimal production-oriented MVP for one-to-one voice calls with:
- Android client (`android/`) in Kotlin (MVVM + Hilt + StateFlow)
- Node.js backend (`backend/`) with Express + ws + PostgreSQL

## Architecture Overview

### Backend (`backend/`)
- REST API:
  - `POST /register`
  - `POST /login`
  - `POST /logout`
  - `GET /users` (JWT-protected)
- WebSocket signaling endpoint: `/signal?token=<JWT>`
- Responsibilities:
  - user auth + password hashing (bcrypt)
  - presence updates (online/offline)
  - call signaling routing (`call_request`, `offer`, `answer`, `ice`, `hangup`)
  - no media relay

### Android (`android/`)
- Package: `com.mohamadodeh6.callingapp`
- MVVM + repository pattern + Hilt DI
- Coroutines + StateFlow
- Material 3 XML UI
- WebRTC audio stack:
  - `PeerConnectionFactory`
  - `PeerConnection`
  - `AudioSource` / `AudioTrack`
  - SDP + ICE handling
- WebSocket signaling client over JWT-authenticated `/signal`

## Repository Structure

- `android/` Android Studio project
- `backend/` Node.js backend
- `backend/sql/001_init.sql` PostgreSQL schema

## Backend Setup

1. Install dependencies:
   ```bash
   cd backend
   npm install
   ```
2. Configure env:
   ```bash
   cp .env.example .env
   # Fill DATABASE_URL and JWT_SECRET
   ```
3. Apply schema:
   ```bash
   psql "$DATABASE_URL" -f sql/001_init.sql
   ```
4. Run:
   ```bash
   npm start
   ```

### Backend Env Vars

- `PORT` (default: `3000`)
- `DATABASE_URL` (required)
- `JWT_SECRET` (required)
- `JWT_EXPIRES_IN` (default: `7d`)
- `CORS_ORIGIN` (default: `http://localhost:3000`, comma-separated allow-list supported)
- `STUN_SERVER` (default: `stun:stun.l.google.com:19302`)
- `TURN_URL` (optional)
- `TURN_USERNAME` (optional)
- `TURN_CREDENTIAL` (optional)

## Android Setup

1. Open `android/` in Android Studio.
2. Ensure backend is reachable from device/emulator.
3. Update URLs if needed in `app/build.gradle.kts` (`BuildConfig` fields).
4. Build and run app.

### Android Permissions

- `INTERNET`
- `RECORD_AUDIO`
- `MODIFY_AUDIO_SETTINGS`

Runtime audio permission is requested before answering/starting calls.

## WebSocket Signaling Messages

Supported message types:
- `call_request`
- `offer`
- `answer`
- `ice`
- `hangup`

Each message carries routing metadata (`to`/`from`) as needed.

## Test and Validation

Backend tests:
```bash
cd backend
npm test
```

## Current MVP Limitations

- No messaging, video, group calls, push notifications, contact sync, SMS verification, or recording
- Stateless JWT logout (client-side token removal)
- Android build in this sandbox may fail to resolve Android Gradle Plugin due remote dependency access limitations; project files are ready for Android Studio/local environment

## Next Steps

- Add refresh token flow and token revocation strategy
- Add robust reconnect strategy for WebSocket
- Add call state persistence and better edge-case handling
- Add UI polish and instrumentation tests
