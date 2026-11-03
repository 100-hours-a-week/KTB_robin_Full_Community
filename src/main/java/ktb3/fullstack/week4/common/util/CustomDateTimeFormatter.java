package ktb3.fullstack.week4.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@Component
public class CustomDateTimeFormatter {

    private final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .appendFraction(java.time.temporal.ChronoField.NANO_OF_SECOND, 6, 6, true)
            .toFormatter();


    public LocalDateTime format(String timeString) {
        return LocalDateTime.parse(timeString, formatter);
    }
}
