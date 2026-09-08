package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import jp.levtech.rookie.attendance.dto.AdminAttendanceView;

/**
 * 管理者用勤怠実績のRepository
 */
public interface AdminAttendanceRepository {

    /**
     * 検索条件に一致する勤怠実績をHardされた検索条件に一致する勤怠実績を取得する
     *
     * @param startDate 検索開始日
     * @param endDate 検索終了日
     * @param departmentId 部署ID
     * @param keyword 社員IDまたは社員名
     * @return 勤怠実績一覧
     */
    List<AdminAttendanceView> findByConditions(
            LocalDate startDate,
            LocalDate endDate,
            Integer departmentId,
            String keyword
    );
}