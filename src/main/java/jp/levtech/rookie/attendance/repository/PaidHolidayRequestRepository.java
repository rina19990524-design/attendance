package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import jp.levtech.rookie.attendance.dto.AdminPaidHolidayRequestView;
import jp.levtech.rookie.attendance.model.TbTrnPaidHolidayRequest;

/**
 * 有給申請Repository
 */
public interface PaidHolidayRequestRepository {

    /**
     * 有給申請を登録する
     *
     * @param request 有給申請情報
     */
    void insert(
            TbTrnPaidHolidayRequest request
    );

    /**
     * 指定した申請フラグの有給申請件数を取得する
     *
     * @param requestFlag 申請フラグ
     * @return 有給申請件数
     */
    int countByRequestFlag(
            int requestFlag
    );

    /**
     * 指定した申請フラグの有給申請を取得する
     *
     * @param requestFlag 申請フラグ
     * @return 有給申請一覧
     */
    List<AdminPaidHolidayRequestView> findByRequestFlag(
            int requestFlag
    );

    /**
     * 有給申請の申請フラグを更新する
     *
     * @param requestId 有給申請ID
     * @param currentFlag 現在の申請フラグ
     * @param newFlag 新しい申請フラグ
     * @return 更新した件数
     */
    int updateRequestFlag(
            String requestId,
            int currentFlag,
            int newFlag
    );

    /**
     * 従業員本人の指定期間の有給申請を取得する
     *
     * @param employeeId 社員ID
     * @param startDate 期間の開始日
     * @param endDate 期間の終了日
     * @return 有給申請一覧
     */
    List<TbTrnPaidHolidayRequest> findByEmployeeIdAndPeriod(
            String employeeId,
            LocalDate startDate,
            LocalDate endDate
    );
}