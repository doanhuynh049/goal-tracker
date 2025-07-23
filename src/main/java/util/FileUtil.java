package util;

import model.Goal;
import model.GoalType;
import model.Task;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        Row taskHeaderRow = tasksSheet.createRow(0);
        taskHeaderRow.createCell(0).setCellValue("Task ID");
        taskHeaderRow.createCell(1).setCellValue("Goal Title");
        taskHeaderRow.createCell(2).setCellValue("Task Description");
        taskHeaderRow.createCell(3).setCellValue("Task Priority");
        taskHeaderRow.createCell(4).setCellValue("Task Due Date");
        taskHeaderRow.createCell(5).setCellValue("Task Completed");

        int taskRowNum = 1;
        for (Goal goal : goals) {
            for (Task task : goal.getTasks()) {
                Row row = tasksSheet.createRow(taskRowNum++);
                row.createCell(0).setCellValue(task.getId().toString());
                row.createCell(1).setCellValue(goal.getTitle());  // Goal Title
                row.createCell(2).setCellValue(task.getDescription());
                row.createCell(3).setCellValue(task.getPriority().toString());
                row.createCell(4).setCellValue(task.getDueDate().toString());
                row.createCell(5).setCellValue(task.isCompleted());
            }
        }

        // Write the Excel file to disk
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
        
        // Read Goals sheet
        Sheet goalsSheet = workbook.getSheet("Goals");
        Iterator<Row> goalIterator = goalsSheet.iterator();
        goalIterator.next();  // Skip header row

        while (goalIterator.hasNext()) {
            Row row = goalIterator.next();
            String goalId = row.getCell(0).getStringCellValue();
            String goalTitle = row.getCell(1).getStringCellValue();
            GoalType goalType = GoalType.valueOf(row.getCell(2).getStringCellValue());
            LocalDate targetDate = LocalDate.parse(row.getCell(3).getStringCellValue());

            Goal goal = new Goal(goalTitle, goalType, targetDate);
            goals.add(goal);
        }

        // Read Tasks sheet
        Sheet tasksSheet = workbook.getSheet("Tasks");
        Iterator<Row> taskIterator = tasksSheet.iterator();
        taskIterator.next();  // Skip header row

        while (taskIterator.hasNext()) {
            Row row = taskIterator.next();
            String taskId = row.getCell(0).getStringCellValue();
            String goalTitle = row.getCell(1).getStringCellValue();
            String taskDescription = row.getCell(2).getStringCellValue();
            Task.Priority taskPriority = Task.Priority.valueOf(row.getCell(3).getStringCellValue());
            LocalDate taskDueDate = LocalDate.parse(row.getCell(4).getStringCellValue());
            boolean taskCompleted = row.getCell(5).getBooleanCellValue();

            // Find the goal object associated with this task
            Goal associatedGoal = goals.stream()
                    .filter(goal -> goal.getTitle().equals(goalTitle))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Goal not found: " + goalTitle));

            Task task = new Task(UUID.fromString(taskId), taskDescription, taskDueDate, taskPriority, taskCompleted);
            associatedGoal.addTask(task);
        }

        workbook.close();
        return goals;
    }
}
