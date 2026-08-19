package sunrisedentalclinicsystem.util;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 *
 * @author yasit
 */
public final class AppConstants {

    private AppConstants() {
    }

    // Application Information
    public static final String APP_NAME = "Sunrise Dental Clinic System";
    public static final String VERSION = "Version 1.0";

    // Window Settings
    public static final int WINDOW_WIDTH = 1366;
    public static final int WINDOW_HEIGHT = 768;
    public static final int MIN_WINDOW_WIDTH = 1280;
    public static final int MIN_WINDOW_HEIGHT = 720;

    // Layout Settings
    public static final int SIDEBAR_WIDTH = 250;
    public static final int HEADER_HEIGHT = 80;
    public static final int STATUSBAR_HEIGHT = 25;
    
    public static final int BORDER_RADIUS = 8;

    // Appointment Scheduling Settings
    public static final LocalTime APPOINTMENT_DAY_START = LocalTime.of(9, 0);
    public static final LocalTime APPOINTMENT_DAY_END = LocalTime.of(17, 0);
    public static final int APPOINTMENT_SLOT_MINUTES = 30;

    // Billing Settings
    public static final BigDecimal CONSULTATION_FEE = new BigDecimal("500.00");

    // Formatting
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "hh:mm a";
}
