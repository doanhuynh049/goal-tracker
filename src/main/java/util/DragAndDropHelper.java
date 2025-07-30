package util;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Goal;
import model.Task;
import service.GoalService;

import java.util.List;

/**
 * Utility class for implementing drag and drop functionality for task reordering
 */
public class DragAndDropHelper {
    
    private static final String DRAG_DATA_FORMAT = "application/x-task-index";
    private static final DataFormat TASK_DATA_FORMAT = new DataFormat(DRAG_DATA_FORMAT);
    
    private final GoalService service;
    private VBox dragIndicator;
    
    public DragAndDropHelper(GoalService service) {
        this.service = service;
        createDragIndicator();
    }
    
    private void createDragIndicator() {
        dragIndicator = new VBox();
        dragIndicator.setPrefHeight(3);
        dragIndicator.setStyle("-fx-background-color: #4299e1; -fx-background-radius: 2px;");
        dragIndicator.setVisible(false);
    }
    
    /**
     * Setup drag and drop for a task card
     */
    public void setupTaskDragAndDrop(VBox taskCard, Task task, Goal parentGoal, VBox tasksContainer) {
        setupDragSource(taskCard, task, parentGoal);
        setupDropTarget(taskCard, task, parentGoal, tasksContainer);
    }
    
    private void setupDragSource(VBox taskCard, Task task, Goal parentGoal) {
        taskCard.setOnDragDetected(event -> {
            if (event.getTarget() instanceof Node) {
                Node target = (Node) event.getTarget();
                if (target instanceof VBox || (target.getParent() != null && target.getParent() instanceof VBox)) {
                    Dragboard dragboard = taskCard.startDragAndDrop(TransferMode.MOVE);
                    
                    ClipboardContent content = new ClipboardContent();
                    content.put(TASK_DATA_FORMAT, task.getId().toString());
                    content.putString(parentGoal.getId().toString());
                    dragboard.setContent(content);
                    
                    // Visual feedback for drag start
                    taskCard.setStyle(taskCard.getStyle() + "-fx-opacity: 0.6;");
                    
                    event.consume();
                }
            }
        });
        
        taskCard.setOnDragDone(event -> {
            // Reset visual state
            taskCard.setStyle(taskCard.getStyle().replace("-fx-opacity: 0.6;", ""));
            event.consume();
        });
    }
    
    private void setupDropTarget(VBox taskCard, Task task, Goal parentGoal, VBox tasksContainer) {
        taskCard.setOnDragOver(event -> {
            if (event.getGestureSource() != taskCard && 
                event.getDragboard().hasContent(TASK_DATA_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
                
                // Show drop indicator
                showDropIndicator(taskCard, tasksContainer, event.getY());
            }
            event.consume();
        });
        
        taskCard.setOnDragEntered(event -> {
            if (event.getGestureSource() != taskCard && 
                event.getDragboard().hasContent(TASK_DATA_FORMAT)) {
                // Visual feedback for valid drop target
                taskCard.setStyle(taskCard.getStyle() + "-fx-border-color: #4299e1; -fx-border-width: 2px;");
            }
            event.consume();
        });
        
        taskCard.setOnDragExited(event -> {
            // Reset border
            taskCard.setStyle(taskCard.getStyle().replaceAll("-fx-border-color: #4299e1; -fx-border-width: 2px;", ""));
            hideDragIndicator(tasksContainer);
            event.consume();
        });
        
        taskCard.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasContent(TASK_DATA_FORMAT)) {
                String draggedTaskId = (String) dragboard.getContent(TASK_DATA_FORMAT);
                String sourceGoalId = dragboard.getString();
                
                // Find source task and goal
                Goal sourceGoal = service.getAllGoals().stream()
                    .filter(g -> g.getId().toString().equals(sourceGoalId))
                    .findFirst().orElse(null);
                
                if (sourceGoal != null) {
                    Task draggedTask = sourceGoal.getTasks().stream()
                        .filter(t -> t.getId().toString().equals(draggedTaskId))
                        .findFirst().orElse(null);
                    
                    if (draggedTask != null) {
                        // Perform the reorder
                        reorderTasks(draggedTask, task, sourceGoal, parentGoal, tasksContainer, event.getY());
                        success = true;
                    }
                }
            }
            
            hideDragIndicator(tasksContainer);
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    private void showDropIndicator(VBox taskCard, VBox tasksContainer, double mouseY) {
        // Determine if we should drop above or below the current task
        double cardHeight = taskCard.getHeight();
        boolean dropAbove = mouseY < cardHeight / 2;
        
        int targetIndex = tasksContainer.getChildren().indexOf(taskCard);
        if (!dropAbove) {
            targetIndex++;
        }
        
        // Ensure we don't exceed container bounds
        if (targetIndex >= tasksContainer.getChildren().size()) {
            targetIndex = tasksContainer.getChildren().size();
        }
        
        // Remove existing indicator
        tasksContainer.getChildren().remove(dragIndicator);
        
        // Add indicator at new position
        if (targetIndex < tasksContainer.getChildren().size()) {
            tasksContainer.getChildren().add(targetIndex, dragIndicator);
        } else {
            tasksContainer.getChildren().add(dragIndicator);
        }
        
        dragIndicator.setVisible(true);
    }
    
    private void hideDragIndicator(VBox tasksContainer) {
        dragIndicator.setVisible(false);
        tasksContainer.getChildren().remove(dragIndicator);
    }
    
    private void reorderTasks(Task draggedTask, Task targetTask, Goal sourceGoal, Goal targetGoal, VBox tasksContainer, double mouseY) {
        List<Task> sourceTasks = sourceGoal.getTasks();
        List<Task> targetTasks = targetGoal.getTasks();
        
        // Remove task from source
        sourceTasks.remove(draggedTask);
        
        if (sourceGoal.equals(targetGoal)) {
            // Reordering within same goal
            int targetIndex = targetTasks.indexOf(targetTask);
            
            // Determine if we should insert above or below
            VBox targetCard = findTaskCard(targetTask, tasksContainer);
            if (targetCard != null) {
                double cardHeight = targetCard.getHeight();
                boolean insertAbove = mouseY < cardHeight / 2;
                
                if (!insertAbove) {
                    targetIndex++;
                }
            }
            
            // Ensure valid index
            if (targetIndex > targetTasks.size()) {
                targetIndex = targetTasks.size();
            } else if (targetIndex < 0) {
                targetIndex = 0;
            }
            
            targetTasks.add(targetIndex, draggedTask);
        } else {
            // Moving between goals
            int targetIndex = targetTasks.indexOf(targetTask);
            targetTasks.add(targetIndex, draggedTask);
        }
        
        // Save changes
        service.saveGoalsToFile();
        
        // Show success feedback
        showReorderFeedback("Task reordered successfully!");
    }
    
    private VBox findTaskCard(Task task, VBox container) {
        // This would need to be implemented based on how you identify task cards
        // For now, returning null as we'd need more context about card identification
        return null;
    }
    
    private void showReorderFeedback(String message) {
        // Create a temporary feedback label
        Label feedback = new Label("✅ " + message);
        feedback.setStyle(
            "-fx-background-color: #48bb78;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-font-weight: bold;"
        );
        
        // Add fade out animation
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(2000), feedback
        );
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (feedback.getParent() != null) {
                ((VBox) feedback.getParent()).getChildren().remove(feedback);
            }
        });
        
        // This would need to be added to the appropriate container
        // For now, we'll just print the message
        System.out.println(message);
    }
    
    /**
     * Enable drag and drop for an entire goals list
     */
    public void setupGoalDragAndDrop(VBox goalCard, Goal goal, VBox goalsContainer) {
        goalCard.setOnDragDetected(event -> {
            Dragboard dragboard = goalCard.startDragAndDrop(TransferMode.MOVE);
            
            ClipboardContent content = new ClipboardContent();
            content.putString(goal.getId().toString());
            dragboard.setContent(content);
            
            // Visual feedback
            goalCard.setStyle(goalCard.getStyle() + "-fx-opacity: 0.7;");
            
            event.consume();
        });
        
        goalCard.setOnDragOver(event -> {
            if (event.getGestureSource() != goalCard && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        goalCard.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasString()) {
                String draggedGoalId = dragboard.getString();
                Goal draggedGoal = service.getAllGoals().stream()
                    .filter(g -> g.getId().toString().equals(draggedGoalId))
                    .findFirst().orElse(null);
                
                if (draggedGoal != null && !draggedGoal.equals(goal)) {
                    // Reorder goals in the service/storage
                    reorderGoals(draggedGoal, goal);
                    success = true;
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        goalCard.setOnDragDone(event -> {
            goalCard.setStyle(goalCard.getStyle().replace("-fx-opacity: 0.7;", ""));
            event.consume();
        });
    }
    
    private void reorderGoals(Goal draggedGoal, Goal targetGoal) {
        // This would need implementation in GoalService to reorder goals
        System.out.println("Reordering goal: " + draggedGoal.getTitle() + " relative to " + targetGoal.getTitle());
    }
}
