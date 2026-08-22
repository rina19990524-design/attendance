package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Override
    public void update(TbTrnAttendance attendance) {
        attendanceMapper.update(attendance);
    }

    @Override
    public Optional<TbTrnAttendance> findByUserIdAndWorkingDay(
            String employeeId,
            LocalDate today) {

        return attendanceMapper.findByUserIdAndWorkingDay(
                employeeId,
                today
        );
    }

    @Override
    public List<TbTrnAttendance> findByUserId(String employeeId) {

        return attendanceMapper.findByUserId(employeeId);
    }
}