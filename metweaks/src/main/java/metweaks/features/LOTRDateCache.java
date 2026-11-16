package metweaks.features;

import java.util.Calendar;

public class LOTRDateCache {
	public static boolean isNewYearsDay;
	public static boolean isAprilFools;
	public static boolean isHalloween;
	public static boolean isChristmas;
	public static final Calendar calendar = Calendar.getInstance();
	
	public static long nextCheck;
	
	static {
		Calendar nextMidnight = (Calendar) calendar.clone();
		nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 0);
        nextMidnight.set(Calendar.MILLISECOND, 0);
        nextMidnight.add(Calendar.DAY_OF_YEAR, 1);
        nextCheck = nextMidnight.getTimeInMillis();
        System.out.println(((nextCheck - System.currentTimeMillis()) / 1000L)+" seconds until midnight");
	}
	 
	public static void updateDates() {
		if(nextCheck < System.currentTimeMillis()) {
			long current = System.currentTimeMillis();
			nextCheck = current + (24L * 60L * 60L * 1000L);
			calendar.setTimeInMillis(current);
			final int month = calendar.get(Calendar.MONTH);
			final int day = calendar.get(Calendar.DAY_OF_MONTH);
	
			isNewYearsDay = month == Calendar.JANUARY && day == 1;
			isAprilFools = month == Calendar.APRIL && day == 1;
			isHalloween = month == Calendar.OCTOBER && day == 31;
			isChristmas = month == Calendar.DECEMBER && (day == 24 || day == 25 || day == 26);
			System.out.println("date synced");
		}
	}
}
