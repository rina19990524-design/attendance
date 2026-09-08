package jp.levtech.rookie.attendance.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.dto.AdminAttendanceRequestView;
import jp.levtech.rookie.attendance.mapper.AdminAttendanceRequestMapper;

/**
 * 管理者用勤怠申請Repository実装クラス
 */
@Repository
public class AdminAttendanceRequestRepositoryImpl
        implements AdminAttendanceRequestRepository {

    private final AdminAttendanceRequestMapper
            adminAttendanceRequestMapper;


    public AdminAttendanceRequestRepositoryImpl(
            AdminAttendanceRequestMapper
                    adminAttendanceRequestMapper) {

        this.adminAttendanceRequestMapper =
                adminAttendanceRequestMapper;
    }


    /**
     * 指定した申請フラグの勤怠申請を取得する
     */
    @Override
    public List<AdminAttendanceRequestView>
            findByRequestFlag(
                    int requestFlag) {

        return adminAttendanceRequestMapper
                .findByRequestFlag(
                        requestFlag
                );
    }


    /**
     * 指定した申請フラグの勤怠申請件数を取得する
     */
    @Override
    public int countByRequestFlag(
            int requestFlag) {

        return adminAttendanceRequestMapper
                .countByRequestFlag(
                        requestFlag
                );
    }


    /**
     * 申請された時刻を元の勤怠実績へ反映する
     */
    @Override
    public int updateAttendanceFromRequest(
            String requestId,
            int requestingFlag) {

        return adminAttendanceRequestMapper
                .updateAttendanceFromRequest(
                        requestId,
                        requestingFlag
                );
    }


    /**
     * 申請フラグを更新する
     */
    @Override
    public int updateRequestFlag(
            String requestId,
            int currentFlag,
            int newFlag) {

        return adminAttendanceRequestMapper
                .updateRequestFlag(
                        requestId,
                        currentFlag,
                        newFlag
                );
    }
}