package jp.levtech.rookie.attendance.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.levtech.rookie.attendance.dto.AdminAttendanceRequestView;

/**
 * 管理者用勤怠申請Mapper
 */
@Mapper
public interface AdminAttendanceRequestMapper {

    /**
     * 指定した申請フラグの勤怠申請を取得する
     */
    List<AdminAttendanceRequestView> findByRequestFlag(
            @Param("requestFlag")
            int requestFlag
    );


    /**
     * 指定した申請フラグの勤怠申請件数を取得する
     */
    int countByRequestFlag(
            @Param("requestFlag")
            int requestFlag
    );


    /**
     * 申請された時刻を元の勤怠実績へ反映する
     */
    int updateAttendanceFromRequest(
            @Param("requestId")
            String requestId,

            @Param("requestingFlag")
            int requestingFlag
    );


    /**
     * 申請フラグを更新する
     */
    int updateRequestFlag(
            @Param("requestId")
            String requestId,

            @Param("currentFlag")
            int currentFlag,

            @Param("newFlag")
            int newFlag
    );
}