package jp.levtech.rookie.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.mapper.AttendanceRequestMapper;
import jp.levtech.rookie.attendance.model.TbTrnAttendanceRequest;

@Repository
public class AttendanceRequestRepositoryImpl
        implements AttendanceRequestRepository {

    private final AttendanceRequestMapper
            attendanceRequestMapper;

    public AttendanceRequestRepositoryImpl(
            AttendanceRequestMapper attendanceRequestMapper) {

        this.attendanceRequestMapper =
                attendanceRequestMapper;
    }

    /**
     * 勤怠修正申請を登録する
     */
    @Override
    public void insert(
            TbTrnAttendanceRequest attendanceRequest) {

        attendanceRequestMapper.insert(
                attendanceRequest
        );
    }

    /**
     * 指定した社員・期間の申請状態を取得する
     */
    @Override
    public List<TbTrnAttendanceRequest>
            findStatusesByUserIdAndPeriod(
                    String employeeId,
                    LocalDate startDate,
                    LocalDate endDate) {

        return attendanceRequestMapper
                .findStatusesByUserIdAndPeriod(
                        employeeId,
                        startDate,
                        endDate
                );
    }

    /**
     * 指定した勤務日の申請中・承認済みの
     * 勤怠修正申請を時刻付きで取得する
     */
    @Override
    public List<TbTrnAttendanceRequest>
            findActiveByUserIdAndWorkingDay(
                    String employeeId,
                    LocalDate workingDay) {

        return attendanceRequestMapper
                .findActiveByUserIdAndWorkingDay(
                        employeeId,
                        workingDay
                );
    }
}