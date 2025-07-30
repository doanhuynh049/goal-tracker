# Daily Email Reminder Scheduler

This app sends a daily email reminder of your tasks at 8:00 AM using Java's ScheduledExecutorService and JavaMail.

## How it works
- At startup, the app schedules a task to run every day at 8:00 AM.
- The task filters today's tasks and sends an email reminder.

## Configuration
- Update your email and app password in `MailService.java`.
- Make sure JavaMail dependencies are included in your build.gradle.

## Usage
- Run the app as usual. The scheduler runs in the background.
