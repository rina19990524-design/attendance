package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jp.levtech.rookie.attendance.model.TbTrnAttendance;

public interface AttendanceRepository {
	
	void insert(TbTrnAttendance attendance);

	void update(TbTrnAttendance attendance);

	Optional<TbTrnAttendance> findByUserIdAndWorkingDay(String userId, LocalDate workingDay);
	
	List<TbTrnAttendance> findByUserId(String employeeId);

}