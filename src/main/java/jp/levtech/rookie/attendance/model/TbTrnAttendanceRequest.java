package jp.levtech.rookie.attendance.model;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbTrnAttendanceRequest {
	
	private String attendanceRequestId;
	
	private String attendanceId;
	
	private LocalTime workingStartTime;
	
	private LocalTime workingEndTime;
	
	private String requestReason;
	
	private int requestFlag;
	
	private int requestType; // キャメルケースに統一

}
