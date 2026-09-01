package jp.levtech.rookie.attendance.repository;

import jp.levtech.rookie.attendance.model.TbTrnPaidHolidayRequest;

public interface PaidHolidayRequestRepository {

    void insert(TbTrnPaidHolidayRequest request);
}