package jp.levtech.rookie.attendance.repository;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.mapper.AttendanceRequestMapper;
import jp.levtech.rookie.attendance.model.TbTrnAttendanceRequest;

@Repository
public class AttendanceRequestRepositoryImpl
        implements AttendanceRequestRepository {

    private final AttendanceRequestMapper attendanceRequestMapper;

    public AttendanceRequestRepositoryImpl(
            AttendanceRequestMapper attendanceRequestMapper) {

        this.attendanceRequestMapper = attendanceRequestMapper;
    }

    @Override
    public void insert(TbTrnAttendanceRequest attendanceRequest) {

        attendanceRequestMapper.insert(attendanceRequest);
    }
}