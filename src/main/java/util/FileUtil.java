// Replace the existing code in FileUtil.java with this updated version:

package util;

import model.Goal;
import model.GoalType;
import model.Task;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.time.LocalDate;

public class FileUtil {
    private static final String FILE_PATH = "data/goals_and_tasks.xlsx";

    // Save goals and tasks to Excel
    public static void saveGoalsAndTasks(List<Goal> goals) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Write Goals sheet
        Sheet goalsSheet = workbook.createSheet("Goals");
        Row goalHeaderRow = goalsSheet.createRow(0);
        goalHeaderRow.createCell(0).setCellValue("Goal ID");
        goalHeaderRow.createCell(1).setCellValue("Goal Title");
        goalHeaderRow.createCell(2).setCellValue("Goal Type");
        goalHeaderRow.createCell(3).setCellValue("Target Date");

        int goalRowNum = 1;
        for (Goal goal : goals) {
            Row row = goalsSheet.createRow(goalRowNum++);
            row.createCell(0).setCellValue(goal.getId().toString());
            row.createCell(1).setCellValue(goal.getTitle());
            row.createCell(2).setCellValue(goal.getType().toString());
            row.createCell(3).setCellValue(goal.getTargetDate().toString());
        }

        // Write Tasks sheet
        Sheet tasksSheet = workbook.createSheet("Tasks");
        // Add Task Start Day column
        Row taskHeaderRow = tasksSheet.createRow(0);
        taskHeaderRow.createCell(0).setCellValue("Task ID");
        taskHeaderRow.createCell(1).setCellValue("Goal Title");
        taskHeaderRow.createCell(2).setCellValue("Task Title");
        taskHeaderRow.createCell(3).setCellValue("Task Description");
        taskHeaderRow.createCell(4).setCellValue("Task Priority");
        taskHeaderRow.createCell(5).setCellValue("Task Start Day");
        taskHeaderRow.createCell(6).setCellValue("Task Due Date");
        taskHeaderRow.createCell(7).setCellValue("Task Status");
        taskHeaderRow.createCell(8).setCellValue("Task Completed");

        int taskRowNum = 1;
        for (Goal goal : goals) {
            for (Task task : goal.getTasks()) {
                Row row = tasksSheet.createRow(taskRowNum++);
                row.createCell(0).setCellValue(task.getId().toString());
                row.createCell(1).setCellValue(goal.getTitle());
                row.createCell(2).setCellValue(task.getTitle());
                row.createCell(3).setCellValue(task.getDescription());
                row.createCell(4).setCellValue(task.getPriority().toString());
                row.createCell(5).setCellValue(task.getStartDay() != null ? task.getStartDay().toString() : "");
                row.createCell(6).setCellValue(task.getDueDate() != null ? task.getDueDate().toString() : "");
                row.createCell(7).setCellValue(task.getStatus() != null ? task.getStatus().toString() : "To Do");
                row.createCell(8).setCellValue(task.isCompleted());
            }
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
    }

    // Load goals and tasks from Excel
    public static List<Goal> loadGoalsAndTasks() throws IOException {
        List<Goal> goals = new ArrayList<>();
        FileInputStream file = new FileInputStream(FILE_PATH);
        Workbook workbook = new XSSFWorkbook(file);
        try {
            // Read Goals
            Sheet goalsSheet = workbook.getSheet("Goals");
            Iterator<Row> goalIterator = goalsSheet.iterator();
            if (goalIterator.hasNext()) goalIterator.next();  // Skip header

            while (goalIterator.hasNext()) {
                Row row = goalIterator.next();
                if (row == null) continue;
                try {
                    String goalId = row.getCell(0).getStringCellValue();
                    String goalTitle = row.getCell(1).getStringCellValue();
                    GoalType goalType = GoalType.valueOf(row.getCell(2).getStringCellValue());
                    LocalDate targetDate = LocalDate.parse(row.getCell(3).getStringCellValue());
                    Goal goal = new Goal(goalTitle, goalType, targetDate);
                    goals.add(goal);
                } catch (Exception e) {
                    System.err.println("Skipping malformed goal row: " + e.getMessage());
                }
            }

            // Read Tasks
            Sheet tasksSheet = workbook.getSheet("Tasks");
            Iterator<Row> taskIterator = tasksSheet.iterator();
            if (taskIterator.hasNext()) taskIterator.next();  // Skip header

            while (taskIterator.hasNext()) {
                Row row = taskIterator.next();
                if (row == null) continue;
                try {
                    String taskId = row.getCell(0).getStringCellValue();
                    String goalTitle = row.getCell(1).getStringCellValue();
                    String taskTitle = row.getCell(2).getStringCellValue();
                    String taskDescription = row.getCell(3).getStringCellValue();
                    Task.Priority taskPriority = Task.Priority.valueOf(row.getCell(4).getStringCellValue());
                    // Update load logic for start day
                    String taskStartDayStr = row.getCell(5).getStringCellValue();
                    LocalDate taskStartDay = null;
                    if (taskStartDayStr != null && !taskStartDayStr.isEmpty()) {
                        taskStartDay = LocalDate.parse(taskStartDayStr);
                    }
                    LocalDate taskDueDate = LocalDate.parse(row.getCell(6).getStringCellValue());
                    String taskStatusStr = row.getCell(7).getStringCellValue();
                    Task.TaskStatus taskStatus = Task.TaskStatus.fromString(taskStatusStr);
                    boolean taskCompleted = row.getCell(8).getBooleanCellValue();

                    Goal associatedGoal = goals.stream()
                            .filter(goal -> goal.getTitle().equals(goalTitle))
                            .findFirst()
                            .orElse(null);

                    if (associatedGoal == null) {
                        System.err.println("Skipping task: Goal not found: " + goalTitle);
                        continue;
                    }

                    // Use constructor with startDay and title
                    Task task = new Task(UUID.fromString(taskId), taskTitle, taskDescription, taskPriority, taskStartDay, taskDueDate, taskStatus, taskCompleted);
                    associatedGoal.addTask(task);
                } catch (Exception e) {
                    System.err.println("Skipping malformed task row: " + e.getMessage());
                }
            }
        } finally {
            workbook.close();
        }
        return goals;
    }
}
