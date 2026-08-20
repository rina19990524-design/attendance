package jp.levtech.rookie.attendance.repository;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.mapper.AttendanceMapper;
import jp.levtech.rookie.attendance.model.TbTrnAttendance;

@Repository
public class AttendanceRepositoryImpl implements AttendanceRepository {
	
	private final AttendanceMapper attendanceMapper;
	
	public AttendanceRepositoryImpl(AttendanceMapper attendanceMapper) {
		this.attendanceMapper = attendanceMapper;
	}
	
	@Override
	public void insert(TbTrnAttendance attendance) {
		attendanceMapper.insert(attendance);
	}

}
