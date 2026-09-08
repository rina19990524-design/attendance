package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import jp.levtech.rookie.attendance.model.TbTrnAttendanceRequest;

public interface AttendanceRequestRepository {

    /**
     * 勤怠修正申請を登録する
     */
    void insert(
            TbTrnAttendanceRequest attendanceRequest
    );


    /**
     * 指定した社員・期間の申請状態を取得する
     */
    List<TbTrnAttendanceRequest>
            findStatusesByUserIdAndPeriod(
                    String employeeId,
                    LocalDate startDate,
                    LocalDate endDate
            );
}