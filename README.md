# 🧠 BeAlpha – Habit & Goal Tracker

**BeAlpha** is a beautiful and efficient habit tracking app built with modern Android architecture. It helps users build better routines, stay consistent, and analyze their self-growth through rich analytics and a visual calendar.

---

## 🚀 Features

### 🗓 Habit & Goal Management
- Add daily or weekly **habit goals** and **tracking metrics** (e.g., steps, water intake).
- Customize goal details with:
  - Title & description
  - Reminder time
  - Color-coded goals
  - Selectable days of the week

### 📆 Interactive Calendar
- Visual monthly calendar for each habit
- Displays:
  - ✅ Completed days
  - ❌ Missed days
  - 🟡 Pending days
  - ⛔ Non-required days
- Smart snapping and gesture-based navigation

### 📊 Powerful Analytics
- Track success rate, streaks, and total completions
- Weekly & monthly line charts for visual progress
- Detailed goal history logs

### 🔔 Reminders & Automation
- Daily reminder notifications
- Background task worker to auto-mark missed habits at midnight
- Intelligent handling for non-required days

---

## 🧱 Tech Stack

| Layer | Tech |
|-------|------|
| Architecture | MVVM + Clean Architecture + Hilt |
| UI | Jetpack ViewBinding + RecyclerView + ViewPager + ChipGroup |
| Backend | Firebase Firestore |
| Background Tasks | WorkManager |
| Charts | MPAndroidChart |
| Dependency Injection | Hilt |
| Time API | Java 8 Time (LocalDate, YearMonth) |

---

## 🛠 Architecture Overview

```text
App Module
│
├── goal_ui/
│   ├── view/              → Fragments & UI logic
│   ├── adapter/           → Calendar & goal cards
│   ├── analytics/         → Habit analytics features
│   ├── viewmodel/         → Shared state and business logic
│
├── goal_domain/
│   ├── usecase/           → Business rules
│   ├── model/             → Goal data classes
│
├── goal_data/
│   ├── repository/        → Firestore integration
│   ├── datasource/        → Firebase SDK wrappers
│
└── common/
    └── utils/             → Helpers (Time, Dialogs, etc.)
