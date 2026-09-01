package jp.levtech.rookie.attendance.repository;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.mapper.PaidHolidayRequestMapper;
import jp.levtech.rookie.attendance.model.TbTrnPaidHolidayRequest;

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

    @Override
    public void insert(
            TbTrnPaidHolidayRequest request) {

        paidHolidayRequestMapper.insert(request);
    }
}