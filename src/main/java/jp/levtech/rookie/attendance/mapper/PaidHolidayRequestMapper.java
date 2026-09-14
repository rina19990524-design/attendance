package jp.levtech.rookie.attendance.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.levtech.rookie.attendance.dto.AdminPaidHolidayRequestView;
import jp.levtech.rookie.attendance.model.TbTrnPaidHolidayRequest;

/**
 * 有給申請Mapper
 */
@Mapper
public interface PaidHolidayRequestMapper {

    /**
     * 有給申請を登録する
     */
    void insert(
            TbTrnPaidHolidayRequest request
    );

    /**
     * 指定した申請フラグの有給申請件数を取得する
     */
    int countByRequestFlag(
            @Param("requestFlag")
            int requestFlag
    );

    /**
     * 指定した申請フラグの有給申請を取得する
     */
    List<AdminPaidHolidayRequestView> findByRequestFlag(
            @Param("requestFlag")
            int requestFlag
    );

    /**
     * 有給申請の申請フラグを更新する
     */
    int updateRequestFlag(
            @Param("requestId")
            String requestId,

            @Param("currentFlag")
            int currentFlag,

            @Param("newFlag")
            int newFlag
    );

    /**
     * 従業員本人の指定期間の有給申請を取得する
     */
    List<TbTrnPaidHolidayRequest> findByEmployeeIdAndPeriod(
            @Param("employeeId")
            String employeeId,

            @Param("startDate")
            LocalDate startDate,

            @Param("endDate")
            LocalDate endDate
    );

    /**
     * 本人の申請中の有給申請を取得する
     */
    List<TbTrnPaidHolidayRequest> findPendingByEmployeeId(
            @Param("employeeId")
            String employeeId
    );

    /**
     * 本人の申請中の有給申請だけを取消済みにする
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