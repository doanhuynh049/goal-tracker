package util;

import java.util.concurrent.*;
import java.time.*;

public class DailyScheduler {
    public static void scheduleDailyTask(Runnable task, int hour, int minute) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        long delay = computeInitialDelay(hour, minute);
        long period = TimeUnit.DAYS.toSeconds(1); // 24 hours
        scheduler.scheduleAtFixedRate(task, delay, period, TimeUnit.SECONDS);
    }

    private static long computeInitialDelay(int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(0);
        if (now.isAfter(nextRun)) nextRun = nextRun.plusDays(1);
        return Duration.between(now, nextRun).getSeconds();
    }
}
