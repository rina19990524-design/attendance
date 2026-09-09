package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.mapper.AttendanceMapper;
import jp.levtech.rookie.attendance.model.TbTrnAttendance;

@Repository
public class AttendanceRepositoryImpl
        implements AttendanceRepository {

    private final AttendanceMapper
            attendanceMapper;


    public AttendanceRepositoryImpl(
            AttendanceMapper attendanceMapper) {

        this.attendanceMapper =
                attendanceMapper;
    }


    /**
     * 社員IDと勤務日から勤怠情報を取得する
     */
    @Override
    public Optional<TbTrnAttendance>
            findByUserIdAndWorkingDay(
                String employeeId,
                LocalDate workingDay) {

        return attendanceMapper
                .findByUserIdAndWorkingDay(
                        employeeId,
                        workingDay
                );
    }


    /**
     * 勤怠情報を登録する
     */
    @Override
    public void insert(
            TbTrnAttendance attendance) {

        attendanceMapper.insert(
                attendance
        );
    }


    /**
     * 出退勤を含む勤怠情報を更新する
     */
    @Override
    public int update(
            TbTrnAttendance attendance) {

        return attendanceMapper.update(
                attendance
        );
    }


    /**
     * 未打刻の勤務予定を更新する
     */
    @Override
    public int updateWorkSchedule(
            TbTrnAttendance attendance) {

        return attendanceMapper
                .updateWorkSchedule(
                        attendance
                );
    }


    /**
     * 社員IDからすべての勤怠情報を取得する
     */
    @Override
    public List<TbTrnAttendance>
            findByUserId(
                String employeeId) {

        return attendanceMapper
                .findByUserId(
                        employeeId
                );
    }
}