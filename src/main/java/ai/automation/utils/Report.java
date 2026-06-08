package ai.automation.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for maintaining execution and status logs.
 *
 * <p>Usage Example:</p>
 *
 * <pre>
 * Report r = new Report();
 *
 * Report.reportlog("Open login page");
 * Report.reporter("PASS", "Login page displayed");
 *
 * System.out.println(r);
 * </pre>
 */
public class Report {

    public static void reportLog(String clickFullNameLabel) {

    }

    /**
     * Represents a single report entry.
     */
    public static class Entry {

        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        private final LocalDateTime timestamp;
        private final String type;
        private final String level;
        private final String message;

        /**
         * Constructs an Entry.
         *
         * @param timestamp the timestamp
         * @param type the entry type ("STEP" or "STATUS")
         * @param level the status level (PASS, FAIL, INFO, WARN, etc.)
         * @param message the entry message
         */
        public Entry(LocalDateTime timestamp,
                     String type,
                     String level,
                     String message) {

            this.timestamp = timestamp;
            this.type = type;
            this.level = level;
            this.message = message;
        }

        /**
         * Returns the timestamp.
         *
         * @return timestamp
         */
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        /**
         * Returns the type.
         *
         * @return type
         */
        public String getType() {
            return type;
        }

        /**
         * Returns the level.
         *
         * @return level
         */
        public String getLevel() {
            return level;
        }

        /**
         * Returns the message.
         *
         * @return message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns a formatted string representation.
         *
         * @return formatted entry
         */
        @Override
        public String toString() {

            String formattedTime = timestamp.format(FORMATTER);

            if ("STATUS".equalsIgnoreCase(type)) {
                return "[" + formattedTime + "] "
                        + level
                        + " - "
                        + message;
            }

            return "[" + formattedTime + "] STEP - " + message;
        }
    }

    /**
     * Holds all report entries.
     */
    private static final List<Entry> entries = new ArrayList<>();

    /**
     * Adds a STEP entry using the current timestamp.
     *
     * @param stepMessage step description
     */
    public static void reportlog(String stepMessage) {

        entries.add(
                new Entry(
                        LocalDateTime.now(),
                        "STEP",
                        null,
                        stepMessage
                )
        );
    }

    /**
     * Adds a STATUS entry using the current timestamp.
     *
     * @param status status level (PASS, FAIL, INFO, WARN, etc.)
     * @param message status message
     */
    public static void reporter(String status, String message) {

        entries.add(
                new Entry(
                        LocalDateTime.now(),
                        "STATUS",
                        status,
                        message
                )
        );
    }

    /**
     * Returns all entries as an unmodifiable list.
     *
     * @return log entries
     */
    public List<Entry> getLog() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns only STATUS entries as an unmodifiable list.
     *
     * @return status entries
     */
    public List<Entry> getStatusEntries() {

        List<Entry> statusEntries = entries.stream()
                .filter(entry -> "STATUS".equalsIgnoreCase(entry.getType()))
                .collect(Collectors.toList());

        return Collections.unmodifiableList(statusEntries);
    }

    /**
     * Returns the complete report as a formatted string.
     *
     * @return report contents
     */
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        for (Entry entry : entries) {
            builder.append(entry)
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }
}