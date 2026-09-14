package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.dto.AdminPaidHolidayRequestView;
import jp.levtech.rookie.attendance.mapper.PaidHolidayRequestMapper;
import jp.levtech.rookie.attendance.model.TbTrnPaidHolidayRequest;

/**
 * 有給申請Repository実装クラス
 */
@Repository
public class PaidHolidayRequestRepositoryImpl
        implements PaidHolidayRequestRepository {

    private final PaidHolidayRequestMapper
            paidHolidayRequestMapper;

    public PaidHolidayRequestRepositoryImpl(
            PaidHolidayRequestMapper paidHolidayRequestMapper) {

        this.paidHolidayRequestMapper =
                paidHolidayRequestMapper;
    }

    /**
     * 有給申請を登録する
     */
    @Override
    public void insert(
            TbTrnPaidHolidayRequest request) {

        paidHolidayRequestMapper.insert(request);
    }

    /**
     * 指定した申請フラグの有給申請件数を取得する
     */
    @Override
    public int countByRequestFlag(
            int requestFlag) {

        return paidHolidayRequestMapper
                .countByRequestFlag(requestFlag);
    }

    /**
     * 指定した申請フラグの有給申請を取得する
     */
    @Override
    public List<AdminPaidHolidayRequestView>
            findByRequestFlag(
                    int requestFlag) {

        return paidHolidayRequestMapper
                .findByRequestFlag(requestFlag);
    }

    /**
     * 有給申請の申請フラグを更新する
     */
    @Override
    public int updateRequestFlag(
            String requestId,
            int currentFlag,
            int newFlag) {

        return paidHolidayRequestMapper
                .updateRequestFlag(
                        requestId,
                        currentFlag,
                        newFlag
                );
    }

    /**
     * 従業員本人の指定期間の有給申請を取得する
     */
    @Override
    public List<TbTrnPaidHolidayRequest>
            findByEmployeeIdAndPeriod(
                    String employeeId,
                    LocalDate startDate,
                    LocalDate endDate) {

        return paidHolidayRequestMapper
                .findByEmployeeIdAndPeriod(
                        employeeId,
                        startDate,
                        endDate
                );
    }

    /**
     * 本人の申請中の有給申請を取得する
     */
    @Override
    public List<TbTrnPaidHolidayRequest>
            findPendingByEmployeeId(
                    String employeeId) {

        return paidHolidayRequestMapper
                .findPendingByEmployeeId(employeeId);
    }

    /**
     * 本人の申請中の有給申請だけを取消済みにする
     */
    @Override
    public int cancelPendingRequest(
            String requestId,
            String employeeId) {

        return paidHolidayRequestMapper
                .cancelPendingRequest(
                        requestId,
                        employeeId
                );
    }
}