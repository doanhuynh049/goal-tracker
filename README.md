# 🎯 Goal & Task Management System

A comprehensive JavaFX-based goal and task management application with hierarchical goal structure, statistical analysis, and automated email notifications.

## 📋 Overview

This Goal Tracker is a sophisticated desktop application that helps users organize, track, and achieve their objectives through a hierarchical goal structure. The application supports multi-level goal planning from long-term objectives down to daily tasks, with automatic progress tracking and intelligent scheduling.

### ✨ Key Features

- **🌳 Hierarchical Goal Structure**: Organize goals from long-term (3+ years) down to daily tasks
- **📊 Statistical Dashboard**: Comprehensive charts and analytics for goal progress
- **📧 Email Integration**: Automated daily task reminders and statistics reports
- **📅 Smart Scheduling**: Task scheduling with start dates, due dates, and priority management
- **🎨 Modern UI**: Clean, intuitive JavaFX interface with responsive design
- **💾 Data Persistence**: Automatic saving and loading of goals and tasks
- **📈 Progress Tracking**: Real-time progress calculation across goal hierarchies

## 🏗️ System Architecture

### Goal Hierarchy Levels
```
Level 0: LONG_TERM Goals    (2-3 years)
Level 1: YEARLY Goals       (Annual objectives)
Level 2: MONTHLY Goals      (Monthly milestones)  
Level 3: WEEKLY Goals       (Weekly targets)
Level 4: DAILY Goals        (Daily tasks)
```

### Example Structure
```
🎯 "Become Senior Developer" (LONG_TERM)
  ├── 📅 "2025 - Master Java & AI" (YEARLY)
  │   ├── 📆 "Q1 - Java Fundamentals" (MONTHLY)
  │   │   ├── ✅ "Complete Java OOP course"
  │   │   └── ✅ "Build CRUD application"
  │   └── 📆 "Q2 - AI/ML Basics" (MONTHLY)
  └── 📅 "2026 - Leadership Skills" (YEARLY)
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Gradle 7.6+ (wrapper included)

### Quick Start
```bash
# Clone the repository
git clone <repository-url>
cd goal-tracker

# Run the application
./gradlew run
```

### Running Tests
```bash
# Run all unit tests
./gradlew test

# Run specific integration tests
./gradlew test --tests HierarchicalGoalIntegrationTest
```

## 📱 Application Features

### Main Views

1. **🏠 Main Dashboard** ([`MainView.java`](src/main/java/MainView.java))
   - Central navigation hub
   - Quick access to all features
   - Daily task reminder setup

2. **🎯 Goals Management** ([`GoalsView.java`](src/main/java/GoalsView.java))
   - Hierarchical goal display with tree structure
   - Task management with priority and status tracking
   - Real-time progress updates

3. **✏️ Goal Editor** ([`GoalEditView.java`](src/main/java/GoalEditView.java))
   - Create and edit goals with parent-child relationships
   - Type validation and hierarchy constraints
   - Progress tracking and notes

4. **➕ Task Management** ([`AddTaskView.java`](src/main/java/AddTaskView.java))
   - Add tasks to specific goals
   - Priority levels (LOW, MEDIUM, HIGH, URGENT)
   - Status tracking (TO_DO, IN_PROGRESS, DONE)
   - Start date and due date scheduling

5. **📊 Statistics Dashboard** ([`StatisticsView.java`](src/main/java/StatisticsView.java))
   - Task completion charts
   - Goal progress analytics
   - Priority distribution analysis
   - Timeline visualization

### Task Management Features

- **📅 Smart Scheduling**: Tasks with start dates, due dates, and status tracking
- **🎯 Priority System**: Four-level priority system with visual indicators
- **⏰ Status Management**: TO_DO → IN_PROGRESS → DONE workflow
- **🔔 Overdue Detection**: Automatic identification of overdue tasks
- **📧 Email Reminders**: Daily automated task reminders at 8:00 AM

### Email Integration

- **Daily Reminders**: Automated daily task emails via [`MailService`](src/main/java/util/MailService.java)
- **Statistics Reports**: Send dashboard charts via email
- **Smart Scheduling**: Uses [`DailyScheduler`](src/main/java/util/DailyScheduler.java) for timing

## 🔧 Technical Stack

- **Framework**: JavaFX for modern desktop UI
- **Build Tool**: Gradle with wrapper
- **Data Storage**: File-based persistence (Excel/JSON)
- **Email**: JavaMail API integration
- **Charts**: JavaFX Charts for statistics
- **Testing**: JUnit for comprehensive testing
- **Logging**: Custom [`AppLogger`](src/main/java/util/AppLogger.java) utility

## 📁 Project Structure

```
src/
├── main/java/
│   ├── MainView.java           # Main application entry point
│   ├── GoalsView.java          # Goal hierarchy display
│   ├── GoalEditView.java       # Goal creation/editing
│   ├── AddTaskView.java        # Task management
│   ├── StatisticsView.java     # Analytics dashboard
│   ├── model/                  # Data models
│   │   ├── Goal.java           # Goal entity with hierarchy
│   │   ├── Task.java           # Task entity with scheduling
│   │   └── GoalType.java       # Goal type enumeration
│   ├── service/                # Business logic
│   │   └── GoalService.java    # Core goal management
│   └── util/                   # Utilities
│       ├── MailService.java    # Email functionality
│       ├── DailyScheduler.java # Task scheduling
│       └── AppLogger.java      # Logging utility
├── test/java/                  # Unit and integration tests
└── resources/                  # UI resources and configs
```

## 🎯 Usage Examples

### Creating a Hierarchical Goal Structure
1. Start with a **LONG_TERM** goal (e.g., "Career Development")
2. Add **YEARLY** sub-goals (e.g., "2025 - Master Java")
3. Break down into **MONTHLY** milestones
4. Add specific tasks with priorities and deadlines

### Email Configuration
Update email settings in [`MailService.java`](src/main/java/util/MailService.java):
```java
// Update with your email credentials
private static final String EMAIL = "your-email@gmail.com";
private static final String PASSWORD = "your-app-password";
```

### Daily Scheduler Setup
The application automatically sets up daily email reminders at 8:00 AM. This is configured in [`MainView.java`](src/main/java/MainView.java).

## 📊 Analytics & Reporting

The statistics dashboard provides:
- **Task Completion Rate**: Pie chart of completed vs pending tasks
- **Goal Progress**: Bar chart showing progress across all goals
- **Priority Distribution**: Analysis of task priorities
- **Goal Types**: Distribution of different goal types
- **Timeline View**: Task completion over time

## 🧪 Testing

Comprehensive test suite includes:
- **Unit Tests**: Individual component testing
- **Integration Tests**: End-to-end workflow testing ([`HierarchicalGoalIntegrationTest.java`](src/test/java/HierarchicalGoalIntegrationTest.java))
- **Real-world Scenarios**: Career development and fitness goal examples

## 🔄 Development Commands

```bash
# Development build
./gradlew build

# Run with debugging
./gradlew run --debug-jvm

# Clean build
./gradlew clean build

# Generate test reports
./gradlew test jacocoTestReport
```

## 📈 Future Enhancements

- **🌐 Web Interface**: Browser-based access
- **📱 Mobile App**: iOS/Android companion
- **☁️ Cloud Sync**: Multi-device synchronization
- **🤝 Team Collaboration**: Shared goal tracking
- **🔗 Calendar Integration**: Sync with Google Calendar/Outlook
- **📊 Advanced Analytics**: Machine learning insights

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- JavaFX community for excellent UI framework
- Contributors to the hierarchical goal structure design
- Testing framework and best practices community

---

**Built with ❤️ for better goal achievement and productivity**

For detailed hierarchical implementation information, see [`HIERARCHICAL_GOALS_IMPLEMENTATION.md`](HIERARCHICAL_GOALS_IMPLEMENTATION.md).


export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
java -version

./gradlew run

export JAVA_HOME=/opt/java-17
export PATH=$JAVA_HOME/bin:$PATH
./gradlew run
