package com.landry.aws.lambda.businessday;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.ReadableInterval;

import de.jollyday.Holiday;
import de.jollyday.HolidayManager;
import net.objectlab.kit.datecalc.common.DateCalculator;
import net.objectlab.kit.datecalc.common.DefaultHolidayCalendar;
import net.objectlab.kit.datecalc.common.HolidayCalendar;
import net.objectlab.kit.datecalc.joda.LocalDateKitCalculatorsFactory;

public class BusinessDayService
{

	public static DateTime addDayToNowIfPastCutOffTime( LocalTime cutOffTime )
	{

		DateTime startDate = DateTime.now();
		if (cutOffTime != null)
		{
			DateTime dateOnCutOffime = new DateTime().withTime(cutOffTime.getHourOfDay(), cutOffTime.getMinuteOfHour(),
					0, 0);
			if (startDate.isAfter(dateOnCutOffime))
				startDate = startDate.plusDays(1);
		}

		return startDate;
	}

	public static boolean isPastCutOffTime( LocalTime cutOffTime )
	{
		DateTime startDate = DateTime.now();
		if (cutOffTime != null)
		{
			DateTime dateOnCutOffime = new DateTime().withTime(cutOffTime.getHourOfDay(), cutOffTime.getMinuteOfHour(),
					0, 0);
			if (startDate.isAfter(dateOnCutOffime))
				return true;
			else
				return false;
		}
		return false;
	}

	public static DateTime moveForward( int businessDays, DateTime startDate )
	{

		// I am not sure if I am using this holiday shit correctly.
		// Need to read more about it. I may not need to haave to set
		// all the time. I am just not sure how it is saved for future
		// instances via the factory.
		DateTime endDate = startDate.plusYears(1);
		HolidayManager holidayManager = HolidayManager.getInstance(de.jollyday.HolidayCalendar.UNITED_STATES);
		ReadableInterval interval = new Interval(startDate.getMillis(), endDate.getMillis());
		Set<Holiday> holiDs = holidayManager.getHolidays(interval, "ma"); // From
																			// now
																			// to
																			// next
																			// year

		Set<LocalDate> holidays = new HashSet<LocalDate>();
		for (Holiday holiday : holiDs)
		{
			if (holiday.getPropertiesKey().equalsIgnoreCase("VETERANS")
					|| holiday.getPropertiesKey().equalsIgnoreCase("MARTIN_LUTHER_KING")
					|| holiday.getPropertiesKey().equalsIgnoreCase("PATRIOT")
					|| holiday.getPropertiesKey().equalsIgnoreCase("COLUMBUS_DAY")
					|| holiday.getPropertiesKey().equalsIgnoreCase("WASHINGTONS_BIRTHDAY"))
				continue;
			System.out.println(holiday.getPropertiesKey());
			holidays.add(holiday.getDate());
		}

		// create the HolidayCalendar ASSUMING that the set covers the date
		// range.
		final HolidayCalendar<LocalDate> calendar = new DefaultHolidayCalendar<LocalDate>(holidays,
				new LocalDate(startDate), new LocalDate(endDate));

		// register the holidays, any calculator with name "US"
		// asked from now on will receive an IMMUTABLE reference to this
		// calendar
		LocalDateKitCalculatorsFactory.getDefaultInstance().registerHolidays("US", calendar);

		// ask for a LocalDate calculator for "US"
		// even if a new set of holidays is registered, this one calculator is
		// not affected
		DateCalculator<LocalDate> cal = LocalDateKitCalculatorsFactory.forwardCalculator("US");

		// set startDate, this will also set the current business date.
		cal.setStartDate(new LocalDate(startDate));

		cal.moveByBusinessDays(businessDays).getCurrentBusinessDate();

		return cal.getCurrentBusinessDate().toDateTimeAtCurrentTime();

	}

}