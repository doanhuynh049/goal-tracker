# MainView.java

## Overview
`MainView.java` is the main entry point and UI controller for the Goal & Task Manager JavaFX application. It manages the primary window, navigation, theming, and integrates with core services for goals, tasks, statistics, and notifications.

## Key Features
- **Modern Header:** Includes branding, theme toggle, settings, and user profile buttons.
- **Sidebar Navigation Panel:** Provides quick access to Dashboard, Goals, Tasks, Statistics, and Settings with icons and active state styling.
- **Main Content Area:** Displays dashboard actions and dynamically updates based on navigation.
- **Theme Switching:** Supports light/dark theme toggle, updating all UI elements.
- **Email Notifications:** Schedules daily reminders and allows manual sending of task/statistics emails.
- **Statistics Dashboard:** View and send statistics as a dashboard image via email.

## Main Components
- **Header:** Created by `createModernHeader()`. Contains logo, app title, theme toggle, settings, and profile buttons.
- **Sidebar:** Created by `createSidebar()`. Contains navigation buttons with icons and active state management.
- **Main Content:** Created by `createMainContent()`. Contains dashboard actions and updates based on navigation.

## Navigation
- **Dashboard:** Shows main actions (create goal, add task, view goals, etc.).
- **Goals:** Opens the goals view.
- **Tasks:** Opens the add task view (can be extended for a dedicated tasks view).
- **Statistics:** Opens the statistics dashboard.
- **Settings:** Shows a settings dialog (feature placeholder).

## Theming
- All major UI sections (header, sidebar, main content, buttons) update their styles based on the current theme (`isDarkTheme`).
- Theme toggle button switches between light and dark modes.

## Integration
- **GoalService:** Loads and manages goals/tasks.
- **MailService:** Sends email notifications and dashboard images.
- **DailyScheduler:** Schedules daily email reminders.

## How to Extend
- Add new navigation buttons to the sidebar by editing `createSidebar()`.
- Implement dedicated views for tasks, settings, or user profile.
- Customize styles in the theme management methods.

## Entry Point
Run the application using:
```bash
./gradlew run
```
Or execute the `MainView` class directly if compiled.

## File Location
`src/main/java/MainView.java`

---
For more details, see inline comments in the source code.
