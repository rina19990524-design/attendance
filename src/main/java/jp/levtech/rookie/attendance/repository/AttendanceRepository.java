package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jp.levtech.rookie.attendance.model.TbTrnAttendance;

public interface AttendanceRepository {

    /**
     * 社員IDと勤務日から勤怠情報を取得する
     */
    Optional<TbTrnAttendance>
            findByUserIdAndWorkingDay(
                String employeeId,
                LocalDate workingDay
            );


    /**
     * 勤怠情報を登録する
     */
    void insert(
            TbTrnAttendance attendance
    );


    /**
     * 出退勤を含む勤怠情報を更新する
     */
    int update(
            TbTrnAttendance attendance
    );


    /**
     * 未打刻の勤務予定を更新する
     */
    int updateWorkSchedule(
            TbTrnAttendance attendance
    );


    /**
     * 社員IDからすべての勤怠情報を取得する
     */
    List<TbTrnAttendance> findByUserId(
            String employeeId
    );
}