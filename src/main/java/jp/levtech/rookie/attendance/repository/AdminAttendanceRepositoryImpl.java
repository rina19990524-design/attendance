package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.dto.AdminAttendanceView;
import jp.levtech.rookie.attendance.mapper.AdminAttendanceMapper;

/**
 * 管理者用勤怠実績のRepository実装クラス
 */
@Repository
public class AdminAttendanceRepositoryImpl
        implements AdminAttendanceRepository {

    private final AdminAttendanceMapper
            adminAttendanceMapper;


    /**
     * コンストラクタ
     */
    public AdminAttendanceRepositoryImpl(
            AdminAttendanceMapper
                    adminAttendanceMapper) {

        this.adminAttendanceMapper =
                adminAttendanceMapper;
    }


    /**
     * 検索条件に一致する勤怠実績を取得する
     */
    @Override
    public List<AdminAttendanceView> findByConditions(
            LocalDate startDate,
            LocalDate endDate,
            Integer departmentId,
            String keyword) {

        return adminAttendanceMapper
                .findByConditions(
                        startDate,
                        endDate,
                        departmentId,
                        keyword
                );
    }
}