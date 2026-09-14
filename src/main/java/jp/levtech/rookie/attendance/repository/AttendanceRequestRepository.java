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

    /**
     * 指定した勤務日の申請中・承認済みの
     * 勤怠修正申請を時刻付きで取得する
     */
    List<TbTrnAttendanceRequest>
            findActiveByUserIdAndWorkingDay(
                    String employeeId,
                    LocalDate workingDay
            );

    /**
     * 本人の指定期間の「申請中」の勤怠修正申請を取得する
     */
    List<TbTrnAttendanceRequest>
            findPendingByUserIdAndPeriod(
                    String employeeId,
                    LocalDate startDate,
                    LocalDate endDate
            );

    /**
     * 本人の申請中の勤怠修正申請だけを取り消す
     *
     * @return 更新件数。取り消せなければ0
     */
    int cancelPendingRequest(
            String requestId,
            String employeeId
    );
}