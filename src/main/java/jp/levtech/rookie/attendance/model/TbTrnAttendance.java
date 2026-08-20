package jp.levtech.rookie.attendance.model;

import java.time.LocalTime;
import java.util.Date;

import lombok.Value;

@Value
public class TbTrnAttendance {
	
	private final String attendanceId;
	
	private final String userId;
	
	private final Date workingDay;
	
	private final LocalTime workingStartTime;
	
	private final LocalTime workingEndTime;
	
	private final LocalTime actualWorkingStartTime;
	
	private final LocalTime actual_workingEndTime;


}
