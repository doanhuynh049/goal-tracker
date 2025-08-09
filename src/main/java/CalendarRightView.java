// RightSchedulerPanel.java
// A Google Calendar–style right panel for editing & scheduling tasks, with optional sync to Google Calendar.
// Drop-in for your MainView right area. Requires GoogleCalendarService (below) and minor additions to Task/GoalService.

package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import model.Task;
import model.Goal;
import service.GoalService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RightSchedulerPanel extends VBox {
    private final GoalService goalService;
    private final GoogleCalendarService gcalService; // may be null if not configured
    private final boolean isDarkTheme;

    // UI controls
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final ListView<Task> agendaList = new ListView<>();

    private final TextField titleField = new TextField();
    private final CheckBox allDayCheck = new CheckBox("All-day");
    private final ComboBox<String> startHour = new ComboBox<>();
    private final ComboBox<String> startMinute = new ComboBox<>();
    private final ComboBox<String> endHour = new ComboBox<>();
    private final ComboBox<String> endMinute = new ComboBox<>();

    private final ComboBox<Task.Priority> priorityCombo = new ComboBox<>();
    private final ComboBox<Task.TaskStatus> statusCombo = new ComboBox<>();
    private final TextField locationField = new TextField();
    private final TextArea notesArea = new TextArea();

    private final ComboBox<Recurrence> recurrenceCombo = new ComboBox<>();
    private final ComboBox<Reminder> reminderCombo = new ComboBox<>();

    private final Button saveBtn = new Button("Save");
    private final Button newBtn = new Button("New");
    private final Button deleteBtn = new Button("Delete");

    private final Button addToGCalBtn = new Button("Add to Google Calendar");
    private final Button openOnWebBtn = new Button("Open in Google Calendar");
    private final Button removeFromGCalBtn = new Button("Remove from Google");
    private final CheckBox autoSyncCheck = new CheckBox("Auto-sync with Google");

    private Task editingTask = null; // currently selected/edited task

    public RightSchedulerPanel(GoalService goalService, GoogleCalendarService gcalService, boolean isDarkTheme) {
        this.goalService = goalService;
        this.gcalService = gcalService;
        this.isDarkTheme = isDarkTheme;
        buildUI();
        refreshAgendaFor(datePicker.getValue());
    }

    private void buildUI() {
        setSpacing(16);
        setPadding(new Insets(16));
        getStyleClass().add("right-scheduler-panel");
        if (isDarkTheme) setStyle("-fx-background-color:#1e293b");

        // Header with nav like Google Calendar
        HBox header = new HBox(8);
        Button prevDay = new Button("←");
        Button today = new Button("Today");
        Button nextDay = new Button("→");
        Label dateLabel = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d")));
        Region hspacer = new Region();
        HBox.setHgrow(hspacer, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(prevDay, today, nextDay, new Separator(), dateLabel, hspacer);

        prevDay.setOnAction(e -> datePicker.setValue(datePicker.getValue().minusDays(1)));
        nextDay.setOnAction(e -> datePicker.setValue(datePicker.getValue().plusDays(1)));
        today.setOnAction(e -> datePicker.setValue(LocalDate.now()));
        datePicker.valueProperty().addListener((obs, o, n) -> {
            dateLabel.setText(n.format(DateTimeFormatter.ofPattern("EEE, MMM d")));
            refreshAgendaFor(n);
        });

        // Left column: mini month + agenda list
        VBox left = new VBox(10);
        left.getChildren().addAll(datePicker, new Label("Agenda"), agendaList);
        VBox.setVgrow(agendaList, Priority.ALWAYS);
        agendaList.setCellFactory(v -> new TaskCell());
        agendaList.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b != null) loadTaskToEditor(b);
        });

        // Right column: editor form
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(28);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(72);
        form.getColumnConstraints().addAll(c1, c2);

        titleField.setPromptText("Task title");
        locationField.setPromptText("Location (optional)");
        notesArea.setPromptText("Description / notes");
        notesArea.setPrefRowCount(4);

        fillTimeCombos();
        priorityCombo.getItems().setAll(Task.Priority.values());
        statusCombo.getItems().setAll(Task.TaskStatus.values());
        recurrenceCombo.getItems().setAll(Recurrence.values());
        reminderCombo.getItems().setAll(Reminder.values());
        priorityCombo.getSelectionModel().select(Task.Priority.MEDIUM);
        statusCombo.getSelectionModel().select(Task.TaskStatus.TODO);
        recurrenceCombo.getSelectionModel().select(Recurrence.NONE);
        reminderCombo.getSelectionModel().select(Reminder.MINUTES_30);

        int r = 0;
        form.add(new Label("Title"), 0, r); form.add(titleField, 1, r++);
        form.add(new Label("Date"), 0, r); form.add(buildDateTimeRow(), 1, r++);
        form.add(new Label("Priority"), 0, r); form.add(priorityCombo, 1, r++);
        form.add(new Label("Status"), 0, r); form.add(statusCombo, 1, r++);
        form.add(new Label("Recurrence"), 0, r); form.add(recurrenceCombo, 1, r++);
        form.add(new Label("Reminder"), 0, r); form.add(reminderCombo, 1, r++);
        form.add(new Label("Location"), 0, r); form.add(locationField, 1, r++);
        form.add(new Label("Notes"), 0, r); form.add(notesArea, 1, r++);

        HBox actions = new HBox(8);
        actions.getChildren().addAll(newBtn, saveBtn, deleteBtn, new Separator(), addToGCalBtn, openOnWebBtn, removeFromGCalBtn, new Separator(), autoSyncCheck);

        newBtn.setOnAction(e -> clearEditorForNew());
        saveBtn.setOnAction(e -> saveEditor());
        deleteBtn.setOnAction(e -> deleteCurrent());

        addToGCalBtn.setOnAction(e -> pushToGoogleCalendar());
        openOnWebBtn.setOnAction(e -> openInBrowser());
        removeFromGCalBtn.setOnAction(e -> removeFromGoogle());
        autoSyncCheck.setSelected(true);
        toggleGcalButtons(gcalService != null);

        SplitPane body = new SplitPane(left, form);
        body.setDividerPositions(0.35);
        VBox.setVgrow(body, Priority.ALWAYS);

        getChildren().addAll(header, body, actions);
    }

    private Node buildDateTimeRow() {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        allDayCheck.selectedProperty().addListener((o, a, b) -> setTimePickersDisabled(b));
        row.getChildren().addAll(datePicker, allDayCheck, new Label("Start"), startHour, new Label(":"), startMinute,
                new Label("End"), endHour, new Label(":"), endMinute);
        return row;
    }

    private void fillTimeCombos() {
        List<String> hours = new ArrayList<>();
        for (int i=0;i<24;i++) hours.add(String.format("%02d", i));
        List<String> minutes = Arrays.asList("00","15","30","45");
        startHour.getItems().setAll(hours); endHour.getItems().setAll(hours);
        startMinute.getItems().setAll(minutes); endMinute.getItems().setAll(minutes);
        startHour.getSelectionModel().select("09"); startMinute.getSelectionModel().select("00");
        endHour.getSelectionModel().select("10"); endMinute.getSelectionModel().select("00");
    }

    private void setTimePickersDisabled(boolean disabled) {
        startHour.setDisable(disabled); startMinute.setDisable(disabled);
        endHour.setDisable(disabled); endMinute.setDisable(disabled);
    }

    private void refreshAgendaFor(LocalDate date) {
        List<Task> tasks = goalService.getTasksForDate(date); // implement in GoalService
        tasks.sort(Comparator.comparing((Task t) -> t.getStartDateTime().orElse(date.atStartOfDay())));
        agendaList.getItems().setAll(tasks);
        clearEditorForNew();
    }

    private void loadTaskToEditor(Task t) {
        editingTask = t;
        titleField.setText(t.getTitle());
        locationField.setText(t.getLocation().orElse(""));
        notesArea.setText(t.getDescription().orElse(""));
        priorityCombo.getSelectionModel().select(t.getPriority());
        statusCombo.getSelectionModel().select(t.getStatus());
        recurrenceCombo.getSelectionModel().select(Recurrence.fromRRule(t.getRRule().orElse(null)));
        reminderCombo.getSelectionModel().select(Reminder.fromMinutes(t.getReminderMinutes().orElse(30)));

        LocalDate date = t.getStartDateTime().map(LocalDateTime::toLocalDate).orElse(datePicker.getValue());
        datePicker.setValue(date);
        if (t.isAllDay()) {
            allDayCheck.setSelected(true); setTimePickersDisabled(true);
        } else {
            allDayCheck.setSelected(false); setTimePickersDisabled(false);
            LocalTime st = t.getStartDateTime().map(LocalDateTime::toLocalTime).orElse(LocalTime.of(9,0));
            LocalTime et = t.getEndDateTime().map(LocalDateTime::toLocalTime).orElse(st.plusHours(1));
            startHour.getSelectionModel().select(String.format("%02d", st.getHour()));
            startMinute.getSelectionModel().select(String.format("%02d", st.getMinute()/15*15));
            endHour.getSelectionModel().select(String.format("%02d", et.getHour()));
            endMinute.getSelectionModel().select(String.format("%02d", et.getMinute()/15*15));
        }
    }

    private void clearEditorForNew() {
        editingTask = null;
        titleField.clear();
        locationField.clear();
        notesArea.clear();
        priorityCombo.getSelectionModel().select(Task.Priority.MEDIUM);
        statusCombo.getSelectionModel().select(Task.TaskStatus.TODO);
        recurrenceCombo.getSelectionModel().select(Recurrence.NONE);
        reminderCombo.getSelectionModel().select(Reminder.MINUTES_30);
        allDayCheck.setSelected(false); setTimePickersDisabled(false);
        startHour.getSelectionModel().select("09"); startMinute.getSelectionModel().select("00");
        endHour.getSelectionModel().select("10"); endMinute.getSelectionModel().select("00");
        agendaList.getSelectionModel().clearSelection();
    }

    private void saveEditor() {
        String title = titleField.getText()==null?"":titleField.getText().trim();
        if (title.isEmpty()) { showToast("Title is required"); return; }
        LocalDate date = datePicker.getValue();
        boolean allDay = allDayCheck.isSelected();
        LocalDateTime start = allDay? date.atStartOfDay() : LocalDateTime.of(date, LocalTime.of(Integer.parseInt(startHour.getValue()), Integer.parseInt(startMinute.getValue())));
        LocalDateTime end = allDay? date.atTime(23,59) : LocalDateTime.of(date, LocalTime.of(Integer.parseInt(endHour.getValue()), Integer.parseInt(endMinute.getValue())));
        if (!allDay && !end.isAfter(start)) { showToast("End must be after start"); return; }

        if (editingTask == null) {
            editingTask = goalService.createTask(title);
        }
        editingTask.setTitle(title);
        editingTask.setAllDay(allDay);
        editingTask.setStartDateTime(Optional.ofNullable(start));
        editingTask.setEndDateTime(Optional.ofNullable(end));
        editingTask.setPriority(priorityCombo.getValue());
        editingTask.setStatus(statusCombo.getValue());
        editingTask.setLocation(Optional.ofNullable(emptyToNull(locationField.getText())));
        editingTask.setDescription(Optional.ofNullable(emptyToNull(notesArea.getText())));
        String rrule = recurrenceCombo.getValue().toRRule(date);
        editingTask.setRRule(Optional.ofNullable(rrule));
        editingTask.setReminderMinutes(Optional.of(reminderCombo.getValue().minutes));

        goalService.saveTask(editingTask);
        refreshAgendaFor(date);
        showToast("Saved");

        if (autoSyncCheck.isSelected() && gcalService != null) {
            try {
                String eventId = gcalService.upsertEventForTask(editingTask);
                editingTask.setExternalId("google", eventId);
                goalService.saveTask(editingTask);
            } catch (Exception ex) {
                showToast("Google sync failed: " + ex.getMessage());
            }
        }
    }

    private void deleteCurrent() {
        Task sel = editingTask!=null? editingTask: agendaList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        goalService.deleteTask(sel);
        refreshAgendaFor(datePicker.getValue());
        showToast("Deleted");
        if (gcalService!=null && sel.getExternalId("google").isPresent()) {
            try { gcalService.deleteEvent(sel.getExternalId("google").get()); } catch (Exception ignored) {}
        }
    }

    private void pushToGoogleCalendar() {
        if (gcalService == null) { showToast("Google not configured"); return; }
        saveEditor();
    }

    private void removeFromGoogle() {
        if (gcalService == null || editingTask==null) return;
        editingTask.getExternalId("google").ifPresent(id -> {
            try { gcalService.deleteEvent(id); editingTask.clearExternalId("google"); goalService.saveTask(editingTask); showToast("Removed from Google"); }
            catch (Exception ex) { showToast("Failed: "+ex.getMessage()); }
        });
    }

    private void openInBrowser() {
        if (editingTask==null) return;
        String url = GoogleCalendarService.buildEventUrl(editingTask);
        // Open in embedded webview (or Desktop.getDesktop().browse)
        StageUtil.openInWebView("Google Calendar", url);
    }

    private void showToast(String msg) { System.out.println("[RightPanel] " + msg); }

    private void toggleGcalButtons(boolean enabled) {
        addToGCalBtn.setDisable(!enabled);
        openOnWebBtn.setDisable(!enabled);
        removeFromGCalBtn.setDisable(!enabled);
        autoSyncCheck.setDisable(!enabled);
    }

    private static String emptyToNull(String s) { return (s==null || s.trim().isEmpty())? null : s.trim(); }

    // ----- helpers & inner classes -----
    public enum Recurrence { NONE, DAILY, WEEKLY, MONTHLY;
        public String toRRule(LocalDate start) {
            switch (this) {
                case DAILY: return "FREQ=DAILY";
                case WEEKLY: return "FREQ=WEEKLY;BYDAY=" + DayOfWeek.from(start);
                case MONTHLY: return "FREQ=MONTHLY;BYMONTHDAY=" + start.getDayOfMonth();
                default: return null;
            }
        }
        public static Recurrence fromRRule(String r) {
            if (r==null) return NONE;
            if (r.contains("DAILY")) return DAILY;
            if (r.contains("WEEKLY")) return WEEKLY;
            if (r.contains("MONTHLY")) return MONTHLY;
            return NONE;
        }
    }

    public enum Reminder { NONE(0), MINUTES_10(10), MINUTES_30(30), HOUR_1(60), DAY_1(1440);
        public final int minutes; Reminder(int m){this.minutes=m;}
        public static Reminder fromMinutes(int m) {
            for (Reminder r: values()) if (r.minutes==m) return r; return MINUTES_30;
        }
    }

    private static class TaskCell extends ListCell<Task> {
        @Override protected void updateItem(Task t, boolean empty) {
            super.updateItem(t, empty);
            if (empty || t==null) { setText(null); setGraphic(null); return; }
            String time = t.isAllDay()? "All day" : t.getStartDateTime().map(dt -> dt.toLocalTime().toString()).orElse("");
            setText(time + "  •  " + t.getTitle());
        }
    }
}

// StageUtil.java (utility)
package ui;
import javafx.scene.Scene; import javafx.scene.web.WebView; import javafx.stage.Stage;
public class StageUtil { public static void openInWebView(String title, String url){ WebView w=new WebView(); w.getEngine().load(url); Stage s=new Stage(); s.setTitle(title); s.setScene(new Scene(w, 1000, 700)); s.show(); } }


// GoogleCalendarService.java
// Minimal wrapper around Google Calendar API. Replace placeholders with your credentials.
package integration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import model.Task;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.*;
import java.util.*;

public class GoogleCalendarService {
    private final Calendar calendar; // authenticated client
    private final String calendarId; // typically "primary"

    public GoogleCalendarService(Calendar calendarClient, String calendarId) {
        this.calendar = calendarClient; this.calendarId = calendarId==null?"primary":calendarId;
    }

    public String upsertEventForTask(Task t) throws IOException {
        String existingId = t.getExternalId("google").orElse(null);
        Event e = mapTaskToEvent(t);
        Event saved;
        if (existingId == null) saved = calendar.events().insert(calendarId, e).execute();
        else saved = calendar.events().update(calendarId, existingId, e).execute();
        return saved.getId();
    }

    public void deleteEvent(String eventId) throws IOException { calendar.events().delete(calendarId, eventId).execute(); }

    private Event mapTaskToEvent(Task t) {
        Event e = new Event();
        e.setSummary(t.getTitle());
        e.setLocation(t.getLocation().orElse(null));
        e.setDescription(t.getDescription().orElse(null));

        if (t.isAllDay()) {
            EventDateTime start = new EventDateTime().setDate(new com.google.api.client.util.DateTime(true, t.getStartDateTime().orElse(LocalDate.now().atStartOfDay()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 0));
            EventDateTime end = new EventDateTime().setDate(new DateTime(true, t.getEndDateTime().orElse(LocalDate.now().atTime(23,59)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 0));
            e.setStart(start); e.setEnd(end);
        } else {
            ZonedDateTime zst = t.getStartDateTime().orElse(LocalDateTime.now()).atZone(ZoneId.systemDefault());
            ZonedDateTime zend = t.getEndDateTime().orElse(zst.plusHours(1).toLocalDateTime()).atZone(ZoneId.systemDefault());
            e.setStart(new EventDateTime().setDateTime(new DateTime(zst.toInstant().toEpochMilli())));
            e.setEnd(new EventDateTime().setDateTime(new DateTime(zend.toInstant().toEpochMilli())));
        }

        t.getRRule().ifPresent(rr -> e.setRecurrence(Collections.singletonList("RRULE:" + rr)));

        Integer mins = t.getReminderMinutes().orElse(30);
        if (mins > 0) {
            EventReminder er = new EventReminder().setMethod("popup").setMinutes(mins);
            e.setReminders(new Event.Reminders().setUseDefault(false).setOverrides(Collections.singletonList(er)));
        }
        return e;
    }

    // Build a Google Calendar web URL for quick view/edit (no API auth needed).
    public static String buildEventUrl(Task t) {
        // If an eventId exists, open direct edit URL; else open TEMPLATE prefilled
        Optional<String> id = t.getExternalId("google");
        if (id.isPresent()) {
            return "https://calendar.google.com/calendar/event?eid=" + url(id.get());
        }
        String base = "https://calendar.google.com/calendar/render?action=TEMPLATE";
        String text = "&text=" + url(t.getTitle());
        String details = t.getDescription().map(v -> "&details=" + url(v)).orElse("");
        String location = t.getLocation().map(v -> "&location=" + url(v)).orElse("");
        String dates;
        if (t.isAllDay()) {
            LocalDate d = t.getStartDateTime().map(LocalDateTime::toLocalDate).orElse(LocalDate.now());
            String ds = d.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            dates = "&dates=" + ds + "/" + ds;
        } else {
            ZonedDateTime s = t.getStartDateTime().orElse(LocalDateTime.now()).atZone(ZoneId.systemDefault());
            ZonedDateTime e = t.getEndDateTime().orElse(s.plusHours(1).toLocalDateTime()).atZone(ZoneId.systemDefault());
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
            dates = "&dates=" + fmt.format(s) + "/" + fmt.format(e);
        }
        return base + text + details + location + dates;
    }

    private static String url(String s){ return URLEncoder.encode(s, StandardCharsets.UTF_8); }
}


// --- Minimal additions expected in your model/service layer ---
// Task.java additions (sketch)
//  - Optional<LocalDateTime> startDateTime, endDateTime; boolean allDay;
//  - Optional<String> rRule; Optional<Integer> reminderMinutes; Map<String,String> externalIds;
//  - getters/setters + helper getExternalId(provider), setExternalId(provider, id), clearExternalId(provider)

// GoalService.java additions (sketch)
//  List<Task> getTasksForDate(LocalDate date)
//  Task createTask(String title)
//  void saveTask(Task t)
//  void deleteTask(Task t)
