package jp.levtech.rookie.attendance.mapper;

import org.apache.ibatis.annotations.Mapper;

import jp.levtech.rookie.attendance.model.TbTrnAttendance;

@Mapper
public interface AttendanceMapper {
	
	void insert(TbTrnAttendance attendance);

}
