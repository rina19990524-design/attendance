package jp.levtech.rookie.attendance.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.levtech.rookie.attendance.model.TbTrnAttendance;

@Mapper
public interface AttendanceMapper {

    void insert(TbTrnAttendance attendance);

    void update(TbTrnAttendance attendance);

    Optional<TbTrnAttendance> findByUserIdAndWorkingDay(
            @Param("employeeId") String employeeId,
            @Param("today") LocalDate today
    );

    List<TbTrnAttendance> findByUserId(
            @Param("employeeId") String employeeId
    );
}