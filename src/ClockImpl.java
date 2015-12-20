import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class ClockImpl implements Clock {
    private static volatile boolean flagExit = false;
    private volatile boolean flagWaiting = false;
    private LocalTime alarmTime;
    private boolean flagTime;
    private boolean flagAlarm;
    private long timeOffset;
    private String zone;

    public ClockImpl() {
        this.alarmTime = null;
        this.flagTime = false;
        this.flagAlarm = false;
        this.timeOffset = 0;
        this.zone = new SimpleDateFormat("zzz").format(new Date());
    }


    public void showTime(Menu menu) {
        ConsoleWorker.printTimeAndMenu(formTimeString(), formAlarmString(), menu);
    }

    private String formAlarmString() {
        return "Alarm " + (alarmTime != null ? alarmTime : "") + " " + (flagAlarm ? "ON" : "OFF");
    }

    private String formTimeString() {
        //find out fact current time: factTime
        long factTime = new Date().getTime();
        //calculate factTime + offset
        Date myDate = new Date(factTime + timeOffset);
        //prepare params for showing
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss yyyy.MM.dd zzz");
        return df.format(myDate);
    }

    @Override
    public boolean setTime(LocalTime time) {
        //calculate offset
        Date oldDate = new Date(new Date().getTime() + timeOffset);
        oldDate.setHours(time.getHour());
        oldDate.setMinutes(time.getMinute());
        oldDate.setSeconds(time.getSecond());
        timeOffset = oldDate.getTime() - new Date().getTime();
        return true;
    }

    @Override
    public boolean setDate(LocalDate date) {
        //calculate offset
        Date oldDate = new Date(new Date().getTime() + timeOffset);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(oldDate);
        calendar.set(date.getYear(), date.getMonth().ordinal(), date.getDayOfMonth());
        timeOffset = calendar.getTime().getTime() - new Date().getTime();
        return true;
    }

    @Override
    public boolean setAlarmTime(LocalTime alarmTime) {
        this.alarmTime = alarmTime;
        return true;
    }

    @Override
    public boolean setTimeZone(String zone) {
        return false;
    }

    @Override
    public void switchOn() {
        flagTime = true;
    }

    @Override
    public void switchOff() {
        flagTime = false;
    }

    @Override
    public void switchAlarmOn() {
        if (alarmTime != null) {
            flagAlarm = true;
        }
    }

    @Override
    public void switchAlarmOff() {
        flagAlarm = false;
    }

    @Override
    public boolean isAlarmTime() {
        if (alarmTime != null && flagAlarm) {
            long factTime = new Date().getTime();
            Date myDate = new Date(factTime + timeOffset);
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            String myDateStr = df.format(myDate);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
            String alarmTimeStr = alarmTime.format(dtf);
            if (myDateStr.equals(alarmTimeStr)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        ClockImpl clock = new ClockImpl();
        ConsoleHandler handler = clock.new ConsoleHandler();
        ConsoleThread consoleThread;
        clock.switchOn();

        while (!flagExit) {
            clock.showTime(Menu.MAIN);
            if (clock.isAlarmTime()) {
                System.out.println("***ALARM***ALARM***ALARM***");
            }
            try {
                consoleThread = clock.new ConsoleThread(clock, handler);
                consoleThread.setDaemon(true);
                consoleThread.start();
                Thread.sleep(5000);

//                if (clock.flagTime && clock.flagWaiting) {
//                    consoleThread.interrupt();
//                } else {
//                    consoleThread.join();
//                }
                  consoleThread.join();

            } catch (InterruptedException e) {
                System.out.println("main is interrupted");
                flagExit = true;
            }

        }
    }

    public class ConsoleThread extends Thread {
        private ClockImpl clock;
        private ConsoleHandler handler;

        public ConsoleThread(ClockImpl clock, ConsoleHandler handler) {
            this.clock = clock;
            this.handler = handler;
        }

        @Override
        public void run() {
            clock.flagWaiting = true;
//            int selection = ConsoleWorker.readFromChannel();
            int selection = ConsoleWorker.readInt();
            clock.flagWaiting = false;
            synchronized (clock) {
                switch (selection) {
                    case 1:
                        clock.showTime(Menu.SET_TIME);
                        if (!clock.setTime(handler.readTime())) {
                            System.out.println("Setting Time Error!");
                        }
                        break;
                    case 2:
                        clock.showTime(Menu.SET_DATA);
                        if (!clock.setDate(handler.readData())) {
                            System.out.println("Setting Data Error!");
                        }
                        break;
                    case 3:
                        clock.showTime(Menu.SET_TIME_ZONE);
                        if (!clock.setTimeZone(handler.readTimeZone())) {
                            System.out.println("Setting TimeZone Error!");
                        }
                        break;
                    case 4:
                        clock.showTime(Menu.SET_ALARM_TIME);
                        if (!clock.setAlarmTime(handler.readAlarmTime())) {
                            System.out.println("Setting AlarmTime Error!");
                        }
                        break;
                    case 5:
                        clock.switchAlarmOn();
                        break;
                    case 6:
                        clock.switchAlarmOff();
                        break;
                    case 7:
                        clock.switchOn();
                        break;
                    case 8:
                        clock.switchOff();
                        break;
                    case 9:
                        //update screen;
                        break;
                    case 0:
                        clock.exit();
                        break;
                    default:
                        System.out.println("Incorrect input value!");
                        break;
                }
            }
        }
    }

    private void exit() {
        flagExit = true;
    }

    public class ConsoleHandler {
        public LocalTime readTime() {
            while (true) {
                String newTimeStr = ConsoleWorker.readString();
                String[] newTimeStrArr = newTimeStr.split(":");
                try {
                    int hh = Integer.parseInt(newTimeStrArr[0]);
                    int mm = Integer.parseInt(newTimeStrArr[1]);
                    int ss = Integer.parseInt(newTimeStrArr[2]);
                    if (hh < 0 || hh > 23) {
                        System.out.println("Incorrect hours, please enter time again");
                    } else if (mm < 0 || mm > 59) {
                        System.out.println("Incorrect minutes, please enter time again");
                    } else if (ss < 0 || ss > 59) {
                        System.out.println("Incorrect seconds, please enter time again");
                    } else {
                        return LocalTime.of(hh, mm, ss);
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Incorrect, please enter time again");
                }
            }
        }

        public LocalDate readData() {
            while (true) {
                String newDateStr = ConsoleWorker.readString();
                String[] newTimeStrArr = newDateStr.split("-");
                try {
                    int yyyy = Integer.parseInt(newTimeStrArr[0]);
                    int mm = Integer.parseInt(newTimeStrArr[1]);
                    int dd = Integer.parseInt(newTimeStrArr[2]);
                    if (yyyy < 0 || yyyy > 3000) {
                        System.out.println("Incorrect years, please enter date again");
                    } else if (mm < 0 || mm > 12) {
                        System.out.println("Incorrect months, please enter date again");
                    } else if (dd < 0 || dd > 31) {
                        System.out.println("Incorrect days, please enter date again");
                    } else {
                        return LocalDate.of(yyyy, mm, dd);
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Incorrect, please enter date again");
                }
            }
        }

        public String readTimeZone() {
            return null;
        }

        public LocalTime readAlarmTime() {
            while (true) {
                String newAlarmStr = ConsoleWorker.readString();
                String[] newAlarmTimeStrArr = newAlarmStr.split(":");
                try {
                    int hh = Integer.parseInt(newAlarmTimeStrArr[0]);
                    int mm = Integer.parseInt(newAlarmTimeStrArr[1]);
                    if (hh < 0 || hh > 23) {
                        System.out.println("Incorrect hours, please enter time again");
                    } else if (mm < 0 || mm > 59) {
                        System.out.println("Incorrect minutes, please enter time again");
                    } else {
                        return LocalTime.of(hh, mm);
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Incorrect, please enter time again");
                }
            }
        }
    }

    public static class ConsoleWorker {
        private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


//        public static int readFromChannel() {
//            int result = 0;
//            ReadableByteChannel readerChannel = Channels.newChannel(System.in);
//            ByteBuffer buffer = ByteBuffer.allocate(2);
//            try {
//               result = readerChannel.read(buffer);
//            } catch (ClosedByInterruptException e) {
//                System.out.println("ClosedByInterruptException result="+result);
//            } catch (IOException e){
//                e.printStackTrace();
//            }
//
//            return result;
//        }

        public static String readString() {
            boolean success = false;
            String string = "";
            while (!success) {
                try {
                    string = reader.readLine();
                    success = true;
                } catch (IOException e) {
                    System.out.println("Incorrect text. Please try again.");
                }
            }
            return string;
        }

        public static int readInt() {
            boolean success = false;
            int result = 0;
            while (!success) {
                try {
                    result = Integer.parseInt(readString());
                    success = true;
                } catch (NumberFormatException e) {
                    System.out.println("Incorrect number. Please try again.");
                }
            }
            return result;
        }

        public static void printTimeAndMenu(String dataTime, String alarm, Menu menu) {
            System.out.println(dataTime);
            System.out.println(alarm);
            System.out.println(menu.getMenuString());
        }
    }

    public enum Menu {
        MAIN("please choose option:\n 1-set time, 2-set data, 3-set time zone, 4-set alarm time, 5-alarm On," +
                " 6-alarm Off, 7-time On, 8-time Off, 0-exit"),
        SET_TIME("please enter new time in format: hh:mm:ss"),
        SET_DATA("please enter new date in format: yyyy-mm-dd"),
        SET_TIME_ZONE("please enter new time zone in format: int"),
        SET_ALARM_TIME("please enter new alarm time in format: hh:mm");
        private String string;

        Menu(String string) {
            this.string = string;
        }

        public String getMenuString() {
            return string;
        }
    }
}
