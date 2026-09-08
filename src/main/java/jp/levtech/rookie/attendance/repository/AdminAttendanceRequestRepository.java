package jp.levtech.rookie.attendance.repository;

import java.util.List;

import jp.levtech.rookie.attendance.dto.AdminAttendanceRequestView;

/**
 * 管理者用勤怠申請Repository
 */
public interface AdminAttendanceRequestRepository {

    /**
     * 指定した申請フラグの勤怠申請を取得する
     */
    List<AdminAttendanceRequestView> findByRequestFlag(
            int requestFlag
    );


    /**
     * 指定した申請フラグの勤怠申請件数を取得する
     */
    int countByRequestFlag(
            int requestFlag
    );


    /**
     * 申請された時刻を元の勤怠実績へ反映する
     */
    int updateAttendanceFromRequest(
            String requestId,
            int requestingFlag
    );


    /**
     * 申請フラグを更新する
     */
    int updateRequestFlag(
            String requestId,
            int currentFlag,
            int newFlag
    );
}