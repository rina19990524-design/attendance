package jp.levtech.rookie.attendance.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.levtech.rookie.attendance.model.TbTrnAttendanceRequest;

@Mapper
public interface AttendanceRequestMapper {

    /**
     * 勤怠修正申請を登録する
     */
    void insert(
            TbTrnAttendanceRequest attendanceRequest
    );

    /**
     * 指定した社員・期間の申請状態を取得する
     * 勤怠一覧画面で使用する
     */
    List<TbTrnAttendanceRequest>
            findStatusesByUserIdAndPeriod(
                    @Param("employeeId")
                    String employeeId,

                    @Param("startDate")
                    LocalDate startDate,

                    @Param("endDate")
                    LocalDate endDate
            );

    /**
     * 指定した勤務日の申請中・承認済みの
     * 勤怠修正申請を時刻付きで取得する
     */
    List<TbTrnAttendanceRequest>
            findActiveByUserIdAndWorkingDay(
                    @Param("employeeId")
                    String employeeId,

                    @Param("workingDay")
                    LocalDate workingDay
            );

    /**
     * 本人の指定期間の申請中の勤怠修正申請を取得する
     */
    List<TbTrnAttendanceRequest>
            findPendingByUserIdAndPeriod(
                    @Param("employeeId")
                    String employeeId,

                    @Param("startDate")
                    LocalDate startDate,

                    @Param("endDate")
                    LocalDate endDate
            );

    /**
     * 本人の申請中の勤怠修正申請だけを取り消す
     *
     * @return 更新件数
     */
    int cancelPendingRequest(
            @Param("requestId")
            String requestId,

            @Param("employeeId")
            String employeeId
    );
}