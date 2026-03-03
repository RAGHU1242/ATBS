# AgriGo - Production Mobile Booking Platform for Farming Resources

AgriGo connects **Farmers** with **Service Providers** (tractor, harvester, transport, labor, warehousing) using secure auth, realtime GPS tracking, and push notifications.

## Stack
- React Native CLI (Android/iOS)
- Node.js + Express
- MongoDB + Mongoose
- Socket.io (JWT-protected)
- Firebase Cloud Messaging (`firebase-admin` + `@react-native-firebase/messaging`)

## Backend (`agrigo/backend`)

### Run
```bash
cd agrigo/backend
cp .env.example .env
npm install
npm run dev
```

### Key capabilities
- JWT auth with bcrypt password hashing
- Role-based access (`farmer`, `provider`)
- Booking state machine (strict transitions)
- Pagination on heavy list APIs
- Global/auth-specific rate limiting
- Provider availability auto-toggle based on active jobs
- JWT-authenticated Socket.io booking rooms

### State transitions
```text
pending -> accepted | rejected
accepted -> ongoing | rejected
ongoing -> completed
completed/rejected -> terminal
```

### API
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/services` (provider)
- `GET /api/services/nearby?lng=&lat=&page=&limit=`
- `POST /api/bookings`
- `GET /api/bookings?page=&limit=&status=`
- `PATCH /api/bookings/:id/status`
- `POST /api/recommendations`

## Mobile (`agrigo/mobile`)

### Run
```bash
cd agrigo/mobile
cp .env.example .env
npm install
npm run android
```

### Features implemented
- Auth context + token persistence
- Full FCM integration:
  - permission prompt
  - token generation on login/register
  - foreground + background handlers
- Live GPS location:
  - current user location for nearby search/map region
  - provider live GPS streaming every ~5s with native geolocation watch
- Role-specific screens for farmer/provider workflows

## Firebase setup
1. Create Firebase project.
2. Download service account JSON for backend and map values to:
   - `FIREBASE_PROJECT_ID`
   - `FIREBASE_CLIENT_EMAIL`
   - `FIREBASE_PRIVATE_KEY`
3. Add mobile files:
   - `android/app/google-services.json`
   - `ios/GoogleService-Info.plist`
4. Install mobile packages (already declared):
   ```bash
   npm i @react-native-firebase/app @react-native-firebase/messaging
   ```

## Android release readiness
- Added release signing placeholders in `mobile/android/gradle.properties`
- Added release shrink/proguard + Firebase messaging dependency in `mobile/android/app/build.gradle`
- Added `proguard-rules.pro`

Build AAB:
```bash
cd agrigo/mobile/android
./gradlew bundleRelease
```

## Deploy backend (Render/Railway)
1. Set service root: `agrigo/backend`
2. Build command: `npm install`
3. Start command: `npm start`
4. Add all `.env` variables.
5. Use MongoDB Atlas URI in `MONGO_URI`.
