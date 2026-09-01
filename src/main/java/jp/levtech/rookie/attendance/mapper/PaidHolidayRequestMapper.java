package jp.levtech.rookie.attendance.mapper;

import org.apache.ibatis.annotations.Mapper;

import jp.levtech.rookie.attendance.model.TbTrnPaidHolidayRequest;

@Mapper
public interface PaidHolidayRequestMapper {

    void insert(TbTrnPaidHolidayRequest request);
}