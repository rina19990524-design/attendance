package jp.levtech.rookie.attendance.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.levtech.rookie.attendance.model.TbTrnAttendance;

@Mapper
public interface AttendanceMapper {

    /**
     * 社員IDと勤務日から勤怠情報を取得する
     */
    Optional<TbTrnAttendance>
            findByUserIdAndWorkingDay(

                @Param("employeeId")
                String employeeId,

                @Param("today")
                LocalDate today
            );


    /**
     * 勤怠情報を登録する
     */
    void insert(
            TbTrnAttendance attendance
    );


    /**
     * 出退勤を含む勤怠情報を更新する
     *
     * HomeControllerなどの打刻処理で使用する
     */
    int update(
            TbTrnAttendance attendance
    );


    /**
     * 未打刻の勤怠について勤務予定だけを更新する
     */
    int updateWorkSchedule(
            TbTrnAttendance attendance
    );


    /**
     * 社員IDからすべての勤怠情報を取得する
     */
    List<TbTrnAttendance> findByUserId(
            @Param("employeeId")
            String employeeId
    );
}