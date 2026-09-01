package jp.levtech.rookie.attendance.mapper;

import org.apache.ibatis.annotations.Mapper;

import jp.levtech.rookie.attendance.model.TbTrnAttendanceRequest;

@Mapper
public interface AttendanceRequestMapper {

    void insert(TbTrnAttendanceRequest attendanceRequest);

}