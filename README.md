# HabitPulse

A modern habit tracker Android application built with Jetpack Compose and Material3 design. Track your daily habits, monitor streaks, and visualise your progress with weekly and monthly statistics.

## Screenshots

*Coming soon*

## Features

- **Create, Edit, Delete Habits** -- Define habits with name, description, frequency (daily/weekly/monthly), and optional reminder time
- **Daily Completion Tracking** -- Mark habits as completed each day with a single tap
- **Streak Tracking** -- Automatically computed current streak and all-time best streak
- **Statistics Dashboard** -- Weekly bar charts and monthly calendar heatmaps powered by Canvas composables
- **Reminder Notifications** -- Schedule daily reminders via AlarmManager with exact alarm support
- **Home Screen Widget** -- AppWidget showing today's habit progress at a glance
- **Dark Mode** -- Full support for system dark theme and Material You dynamic colours on Android 12+

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose, Material3 |
| Architecture | MVVM + Repository pattern |
| Persistence | Room Database with Flow-based reactive queries |
| Navigation | Navigation Compose |
| Notifications | AlarmManager + NotificationCompat |
| Widget | AppWidgetProvider with RemoteViews |
| Async | Kotlin Coroutines + Flow |
| Build | Gradle Kotlin DSL, Version Catalog |

## Architecture

```
com.habitpulse.app/
├── data/
│   ├── local/          # Room database, DAOs, entities
│   └── repository/     # Repository bridging data and domain layers
├── domain/
│   └── model/          # Domain models (Habit, HabitCompletion, HabitStats)
├── ui/
│   ├── theme/          # Material3 colour, typography, theme definitions
│   ├── screens/        # Screen composables and ViewModels
│   │   ├── home/       # Main habits list with daily progress
│   │   ├── addhabit/   # Create/edit habit form
│   │   ├── detail/     # Habit detail with streak info
│   │   └── stats/      # Weekly and monthly statistics
│   ├── components/     # Reusable composables (HabitCard, StreakBadge, charts)
│   └── navigation/     # NavGraph and Screen route definitions
├── notification/       # AlarmManager scheduler, BroadcastReceivers
├── widget/             # Home screen AppWidget
├── HabitPulseApp.kt    # Application class (dependency root)
└── MainActivity.kt     # Single-activity entry point
```

## Building

1. Open the project in Android Studio Hedgehog (2023.1.1) or later
2. Sync Gradle files
3. Build and run on a device or emulator running Android 8.0 (API 26) or higher

```bash
./gradlew assembleDebug
```

## Requirements

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- Minimum device: Android 8.0 (API 26)
