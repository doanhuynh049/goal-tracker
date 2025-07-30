# 🎯 Goal Tracker

A modern, hierarchical goal tracking application built with JavaFX that helps you organize and track your long-term goals, yearly objectives, monthly targets, and daily tasks.

## ✨ Features

- 🏗️ **Hierarchical Goal Structure**: Long-term goals → Year goals → Month goals → Tasks
- 🎨 **Modern UI**: Three view modes (Hierarchical, List, Kanban Board)
- 📊 **Progress Tracking**: Automatic progress aggregation from tasks to goals
- 📧 **Smart Notifications**: Email reminders and progress reports
- 💾 **Data Persistence**: Excel-based storage with auto-save
- 🎯 **Task Management**: Priority levels, due dates, and status tracking

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- Gradle 7.6+

### Running the Application
```bash
# Clone the repository
git clone <repository-url>
cd goal-tracker

# Run the application
./gradlew run
```

### First Steps
1. Launch the application with `./gradlew run`
2. Click "View Goals and Progress" to open the modern interface
3. Start creating your hierarchical goal structure:
   - Long-term goals (2-3 years)
   - Year goals (1 year)
   - Month goals (1 month)
   - Add tasks to achieve your goals

## 🏗️ Project Structure

```
goal-tracker/
├── src/
│   ├── main/java/           # Main application code
│   │   ├── model/           # Data models (Goal, Task, GoalType)
│   │   ├── service/         # Business logic (GoalService)
│   │   ├── util/            # Utility classes
│   │   └── *.java           # View classes (MainView, GoalsView, etc.)
│   ├── main/resources/      # CSS styles and assets
│   ├── integration/         # Integration tests and demos
│   └── test/               # Unit tests
├── build/                   # Compiled classes and distributions
├── data/                    # Excel data files
├── docs/                    # 📚 Complete documentation
│   ├── INDEX.md            # Documentation navigation
│   ├── QUICK_START_GUIDE.md # User guide
│   ├── PROJECT_SUMMARY.md   # Feature overview
│   └── *.md                # Additional documentation
├── gradle/                  # Gradle wrapper files
├── logs/                    # Application log files
├── build.gradle             # Build configuration
├── gradlew                  # Gradle wrapper script
└── README.md               # This file - main project overview
```

## 📖 Documentation

Comprehensive documentation is available in the `docs/` folder:

- **[📚 Documentation Index](docs/INDEX.md)** - Complete documentation guide
- **[🚀 Quick Start Guide](docs/QUICK_START_GUIDE.md)** - Get started quickly
- **[📋 Project Summary](docs/PROJECT_SUMMARY.md)** - Complete feature overview
- **[🔧 Implementation Details](docs/IMPLEMENTATION_COMPLETE.md)** - Technical implementation
- **[🎨 Modern UI Guide](docs/MODERN_UI_INTEGRATION_COMPLETE.md)** - UI features and design
- **[🏗️ Hierarchical Goals](docs/HIERARCHICAL_GOALS_IMPLEMENTATION.md)** - Goal hierarchy system
- **[✨ Enhanced Features](docs/ENHANCED_FEATURES_COMPLETE.md)** - Advanced features
- **[⚙️ Setup Instructions](docs/README.md)** - Development setup

> **📚 For complete navigation, visit the [Documentation Index](docs/INDEX.md)**

## 🎨 User Interface

### Three Powerful View Modes

1. **🏗️ Hierarchical View**: TreeView showing goal relationships
2. **📋 List View**: Enhanced cards with modern design
3. **📊 Board View**: Kanban-style organization by goal types

### Modern Design Features
- Gradient backgrounds and card layouts
- Real-time progress visualization
- Hover effects and animations
- Color-coded goal types and priorities
- Responsive design elements

## 🔧 Development

### Building the Project
```bash
# Compile the application
./gradlew build

# Run tests
./gradlew test

# Create distribution
./gradlew distZip
```

### Key Components
- **GoalService**: Core business logic and data management
- **GoalsView**: Main UI with three view modes
- **Goal/Task Models**: Hierarchical data structure
- **Excel Integration**: Data persistence layer

## 🎯 Goal Types Hierarchy

```
Long-term Goal (2-3 years)
├── Year Goal (1 year)
│   ├── Month Goal (1 month)
│   │   ├── Task 1
│   │   ├── Task 2
│   │   └── Task N
│   └── Month Goal 2
└── Year Goal 2
```

## 📧 Notifications

- Daily task reminders via email
- Weekly progress reports
- Goal deadline alerts
- Achievement notifications

## 💾 Data Storage

- Excel-based persistence (`goals_and_tasks.xlsx`)
- Automatic saving after each change
- Export/import functionality
- Backup and restore capabilities

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📄 License

This project is open source. See the documentation for more details.

---

**Ready to achieve your goals? Start with `./gradlew run` and build your success hierarchy!** 🚀

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
xport PATH=$JAVA_HOME/bin:$PATH
java -version

./gradlew run