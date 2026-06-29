# Apogee — Build better habits, together

**Apogee** is a modern Android habit-tracking app focused on consistency and accountability. Track daily habits, watch your streaks grow, and take on challenges with friends — wrapped in a clean, fast, Material 3 experience.

> Formerly **Be Alpha**. The app has been rebuilt around a focused habit + accountability core with a custom Jetpack Compose design system.

---

## ✨ Features

- **Habit tracking** — Create habits, schedule them per weekday, and mark them done with a tap. Undo a slip, and protect your run with **streak freezes**.
- **Daily focus & stats** — A home dashboard with today's progress ring, plus a Stats tab with trends, a weekly view, and an activity heatmap.
- **Community & challenges** — Add friends, create or join **group challenges** (each backed by real habits), check in daily, and cheer each other on.
- **Home-screen widgets** — Glance widgets show today's habits and progress at a glance.
- **Reminders** — Per-habit reminders and an evening streak-risk nudge so you never break a chain by accident.
- **Contextual onboarding** — Premium first-visit walkthroughs that teach each screen in context (replayable from Settings).
- **Profile & achievements** — Levels, XP, lifetime stats, and unlockable achievements.

---

## 🛠 Tech Stack

Multi-module **clean architecture** (data / domain / ui per feature) with incremental **Jetpack Compose** adoption over the existing Fragment + Navigation structure.

| Layer | Technology |
|-------|------------|
| **UI** | Jetpack Compose + Fragments / Navigation, custom **"Apex"** design system (`:common:designsystem`) |
| **DI** | Hilt |
| **Data** | Firebase Auth (Google sign-in), Cloud Firestore (offline-first), Room cache |
| **Messaging** | Firebase Cloud Messaging |
| **Widgets** | Jetpack Glance |
| **Build** | Gradle (Kotlin DSL) with a version catalog |

**Modules:** `app`, `common:{utils, ui, designsystem}`, `home`, `goal`, `profile`, `social`, `authentication`, `onboarding`, `ai_agent`, `create`, `notification` — most split into `_data` / `_domain` / `_ui`.

---

## 🔥 Firebase

- Auth (Google), Cloud Firestore, and FCM are configured via `app/google-services.json`.
- Firestore **security rules** and **indexes** live at the repo root (`firestore.rules`, `firestore.indexes.json`, `firebase.json`).
- An optional push relay for cheers / challenge invites lives in `social-push/` (a small serverless function).

---

## 🚀 Getting started

```bash
git clone https://github.com/mravaneesh/be_alpha.git
```

1. Open the project in **Android Studio**.
2. Provide your own `app/google-services.json` and register your debug SHA-1 in Firebase for Google sign-in.
3. Build & run:
   ```bash
   ./gradlew :app:assembleDebug
   ```

---

## 📸 Screenshots
<img width="300" height="600" alt="bealpha1" src="https://github.com/user-attachments/assets/0fc76b83-4580-40e2-ab27-c1cf122ff0cc" />
<img width="300" height="600" alt="belapha2" src="https://github.com/user-attachments/assets/dedaadef-d698-46d3-97f8-b7ffcdaf15bf" />
<img width="300" height="600" alt="bealpha3" src="https://github.com/user-attachments/assets/994e1e56-bba3-43f8-8c56-be4f274e27e1" />
<img width="300" height="600" alt="bealpha4" src="https://github.com/user-attachments/assets/2731370e-629e-42fd-a7b7-9f5daf55cec2" />

---

## 📍 Roadmap

- [x] Habit tracking with streaks, freezes & analytics
- [x] Compose design system + screen migration
- [x] Friends, group challenges & daily check-ins
- [x] Home-screen widgets & reminders
- [x] Contextual feature-discovery walkthroughs
- [ ] Real-time push notifications (server deploy)
- [ ] Release hardening (R8, signed release build)
- [ ] Wearable & cross-platform support

---

## 📬 Contact

👤 **Avaneesh Pandey**
📧 [avaneeshpandey0830@gmail.com](mailto:avaneeshpandey0830@gmail.com)
🔗 [LinkedIn](https://www.linkedin.com/in/avaneesh-pandey0830/)
