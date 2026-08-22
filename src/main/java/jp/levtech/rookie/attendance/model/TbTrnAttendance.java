package jp.levtech.rookie.attendance.model;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbTrnAttendance {
	
	private String attendanceId;
	
	private String userId;
	
	private LocalDate workingDay;
	
	private LocalTime workingStartTime;
	
	private LocalTime workingEndTime;
	
	private LocalTime actualWorkingStartTime;
	
	private LocalTime actualWorkingEndTime; // キャメルケースに統一

}