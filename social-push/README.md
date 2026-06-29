# Apogee social push (free)

One serverless function (`/api/push`) that delivers real push notifications for **friend nudges**
("cheer") and **challenge invites**. Runs on Vercel's free Hobby tier; FCM sending via Firebase
Admin is free. The Android app never holds FCM server credentials — it just calls this endpoint with
the sender's Firebase ID token and a `type`.

## How it fits together

1. App writes the in-app record to Firestore (nudge → `users/{toUid}/nudges`; invite → the
   `challenges/{id}` doc) — these work with no server.
2. App also POSTs to `/api/push` (`PushConfig.ENDPOINT`) with the sender's ID token and
   `{ toUid, type, title? }` (`type` = `nudge` | `challengeInvite`).
3. The function verifies the caller, confirms the two are friends, builds the message by type, looks
   up the recipient's FCM tokens (`users/{toUid}/tokens`), and sends the push.

## Deploy

1. **Service account key:** Firebase console → Project settings → Service accounts →
   *Generate new private key*. You get a JSON file.

2. **Deploy to Vercel** (from this `social-push/` folder):
   ```bash
   npm i -g vercel
   vercel            # first run links/creates the project
   vercel --prod     # production deployment
   ```
   (Or import the folder in the Vercel dashboard.)

3. **Set the env var** in the Vercel project → Settings → Environment Variables:
   - Name: `FIREBASE_SERVICE_ACCOUNT`
   - Value: the **entire** service-account JSON on one line (paste the file contents).
   Redeploy after setting it.

4. **Point the app at it:** put the deployed URL in
   `social/src/main/java/com/example/social/data/PushConfig.kt`:
   ```kotlin
   const val ENDPOINT = "https://<your-project>.vercel.app/api/push"
   ```
   Rebuild the app. Until this is set, nudges/invites are in-app only (no push) — nothing breaks.

## Notes

- Recipient devices register their FCM token automatically on app launch (and on token refresh),
  stored at `users/{uid}/tokens/{token}` — covered by the Firestore rules in `/firestore.rules`.
- The function prunes tokens FCM reports as permanently invalid.
- No card required: Vercel Hobby + FCM are free for this volume. (A Firebase Cloud Function would
  also work but needs the Blaze plan / a card.)
