# 🎯 Goal Tracker - Quick Start Guide

## 🚀 **Getting Started**

### **1. Launch the Application**
```bash
cd /home/quathd_t/workspace/data_workspace/goal-tracker
./gradlew run
```

### **2. Main Menu Options**
- **Create Goal**: Add new goals of any type
- **Add Task to Goal**: Add tasks to existing goals
- **View Goals and Progress**: Open the modern hierarchical interface ⭐
- **View Statistics**: See analytics and reports
- **Send Notification Now**: Email today's tasks
- **Send Statistics Email**: Email dashboard report

---

## 🏗️ **Creating Hierarchical Goals**

### **Step 1: Create a Long-term Goal (2-3 years)**
1. Click "Create Goal"
2. Title: "Become Software Architect"
3. Type: "LONG_TERM_GOAL"
4. Target Date: 2027-12-31
5. Click Create

### **Step 2: Create Year Goals (1 year)**
1. Click "Create Goal"
2. Title: "Master System Design"
3. Type: "YEAR_GOAL"
4. Target Date: 2025-12-31
5. Click Create

### **Step 3: Create Month Goals (1 month)**
1. Click "Create Goal"
2. Title: "Complete Microservices Course"
3. Type: "MONTH_GOAL"
4. Target Date: 2025-08-31
5. Click Create

### **Step 4: Add Tasks**
1. Click "Add Task to Goal"
2. Select the month goal
3. Add tasks like:
   - "Watch course lectures"
   - "Complete hands-on labs"
   - "Build demo project"

---

## 📱 **Using the Modern Interface**

### **Access the Modern UI**
1. From main menu, click **"View Goals and Progress"**
2. You'll see a tabbed interface with three views

### **🏗️ Hierarchical Goals Tab**
- **Left Panel**: Tree view of goal hierarchy
- **Right Panel**: Selected goal details and tasks
- **Features**:
  - Click goals to expand/collapse hierarchy
  - Select goals to see tasks and progress
  - Visual progress bars for each level

### **📋 All Goals Tab**
- **Left Panel**: Modern goal cards with enhanced styling
- **Right Panel**: Task management with in-line editing
- **Features**:
  - Color-coded goal types
  - Progress indicators
  - Overdue goal highlighting
  - Click goals to manage tasks

### **📊 Goal Board Tab**
- **Kanban-style Layout**: Goals organized by type in columns
- **Columns**:
  - 🎯 Long-term Goals
  - 📅 Year Goals
  - 🗓️ Month Goals
  - 📌 Other Goals
- **Features**:
  - Quick overview of all goals
  - Visual grouping by type
  - Card-based goal display

---

## ✅ **Managing Tasks**

### **In the Modern Interface**
1. Select any goal (any tab)
2. Right panel shows all tasks for that goal
3. **Edit tasks in-line**:
   - Change task description
   - Update priority (Urgent/High/Medium/Low)
   - Change status (To Do/In Progress/Done)
   - Set start and due dates

### **Task Status Effects**
- **Mark task as "Done"**: Automatically updates goal progress
- **Progress Calculation**: Cascades up through hierarchy
  - Task completion → Month goal progress
  - Month goal progress → Year goal progress
  - Year goal progress → Long-term goal progress

---

## 🎨 **Interface Features**

### **Visual Indicators**
- **Progress Bars**: Show completion percentage
- **Status Badges**: Color-coded task status
- **Priority Colors**: Visual priority indicators
- **Overdue Warnings**: Red highlighting for overdue items

### **Modern Design Elements**
- **Cards with Shadows**: Professional card-based layout
- **Hover Effects**: Interactive feedback
- **Gradient Backgrounds**: Modern color schemes
- **Icons and Emojis**: Visual goal type indicators

### **Navigation**
- **Tab Switching**: Easy switching between view modes
- **Back Button**: Return to main menu anytime
- **Split Panes**: Resizable panels for optimal viewing

---

## 💾 **Saving and Loading**

### **Auto-Save**
- Changes are automatically saved when you:
  - Update task details
  - Change task status
  - Modify goal information

### **Manual Save/Load**
- **Save Button**: In footer of Goals view
- **Load Button**: Reload from Excel file
- **Excel Integration**: Data stored in `data/goals_and_tasks.xlsx`

---

## 📊 **Progress Tracking**

### **How Progress Works**
1. **Task Level**: Individual task completion (0% or 100%)
2. **Goal Level**: Percentage of completed tasks
3. **Hierarchy Level**: Average of child goal progress
4. **Visual Feedback**: Progress bars and percentages everywhere

### **Example Progress Calculation**
```
🎯 Career Development (11.1%)
  └─ 📅 Technical Skills (33.3%)
     └─ 🗓️ Microservices Course (33.3%)
        ├─ ✅ Watch lectures (100%) 
        ├─ ⏳ Complete labs (0%)
        └─ ⏳ Build project (0%)
```

**Calculation**: 1 of 3 tasks complete = 33.3% → cascades up

---

## 🔧 **Tips and Best Practices**

### **Goal Hierarchy Tips**
- **Long-term Goals**: Big picture, 2-3 year vision
- **Year Goals**: Major milestones within long-term goals
- **Month Goals**: Specific deliverables and learning objectives
- **Tasks**: Actionable items you can complete in days/weeks

### **Task Management Tips**
- **Use Priority Levels**: Focus on Urgent/High priority first
- **Set Realistic Due Dates**: Help track progress and deadlines
- **Update Status Regularly**: Keep progress current
- **Use Descriptive Titles**: Make tasks clear and actionable

### **Interface Tips**
- **Hierarchical Tab**: Best for seeing big picture and relationships
- **List Tab**: Best for detailed task management
- **Board Tab**: Best for quick overview and status checking

---

## 🎯 **Example Usage Scenarios**

### **Career Development**
```
🎯 "Become Senior Developer" (3 years)
  ├─ 📅 "Master Backend Technologies" (2025)
  │  ├─ 🗓️ "Learn Spring Boot" (Jan 2025)
  │  └─ 🗓️ "Master Database Design" (Feb 2025)
  └─ 📅 "Develop Leadership Skills" (2026)
     ├─ 🗓️ "Complete Management Course" (Jan 2026)
     └─ 🗓️ "Lead Team Project" (Mar 2026)
```

### **Personal Fitness**
```
🎯 "Complete Marathon" (2026)
  ├─ 📅 "Build Running Base" (2025)
  │  ├─ 🗓️ "Run 5K consistently" (Aug 2025)
  │  └─ 🗓️ "Complete 10K race" (Nov 2025)
  └─ 📅 "Marathon Training" (2026)
     ├─ 🗓️ "Half Marathon" (Apr 2026)
     └─ 🗓️ "Full Marathon" (Oct 2026)
```

### **Learning New Skills**
```
🎯 "Master Data Science" (2027)
  ├─ 📅 "Learn Fundamentals" (2025)
  │  ├─ 🗓️ "Python Programming" (Aug 2025)
  │  └─ 🗓️ "Statistics Course" (Sep 2025)
  └─ 📅 "Applied Practice" (2026)
     ├─ 🗓️ "Build ML Project" (Mar 2026)
     └─ 🗓️ "Data Science Portfolio" (Jun 2026)
```

---

## 🎉 **You're Ready to Go!**

The Goal Tracker now supports powerful hierarchical goal management with a modern, intuitive interface. Start with the examples above or create your own goal hierarchy to achieve your objectives systematically and track progress visually!

**Happy Goal Tracking! 🎯✨**
