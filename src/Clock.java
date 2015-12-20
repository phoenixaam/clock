import java.time.LocalDate;
import java.time.LocalTime;

public interface Clock {

    boolean setTime(LocalTime time);
    boolean setDate(LocalDate date);
    void switchOn();
    void switchOff();
    boolean setAlarmTime(LocalTime alarmTime);
    boolean setTimeZone(String zone);
    void switchAlarmOn();
    void switchAlarmOff();

    boolean isAlarmTime();
}
