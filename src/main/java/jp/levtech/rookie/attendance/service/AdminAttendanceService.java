package jp.levtech.rookie.attendance.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.levtech.rookie.attendance.dto.AdminAttendanceView;
import jp.levtech.rookie.attendance.repository.AdminAttendanceRepository;

@Service
public class AdminAttendanceService {

    private final AdminAttendanceRepository
            adminAttendanceRepository;


    public AdminAttendanceService(
            AdminAttendanceRepository
                    adminAttendanceRepository) {

        this.adminAttendanceRepository =
                adminAttendanceRepository;
    }


    /**
     * 検索条件に一致する勤怠実績を取得する
     */
    @Transactional(readOnly = true)
    public List<AdminAttendanceView> findAttendances(
            YearMonth targetMonth,
            Integer departmentId,
            String keyword) {

        // 選択された月の1日
        LocalDate startDate =
                targetMonth.atDay(1);

        // 選択された月の最終日
        LocalDate endDate =
                targetMonth.atEndOfMonth();

        // 前後の空白を取り除く
        String trimmedKeyword = "";

        if (keyword != null) {

            trimmedKeyword =
                    keyword.trim();
        }

        // DBから検索条件に一致する勤怠を取得
        List<AdminAttendanceView> attendances =
                adminAttendanceRepository
                    .findByConditions(
                        startDate,
                        endDate,
                        departmentId,
                        trimmedKeyword
                    );

        // 各勤怠に勤務時間と状態を設定
        for (AdminAttendanceView attendance
                : attendances) {

            setWorkingTimeAndStatus(
                    attendance
            );
        }

        return attendances;
    }


    /**
     * 勤務時間と状態を設定する
     */
    private void setWorkingTimeAndStatus(
            AdminAttendanceView attendance) {

        LocalTime startTime =
                attendance.getWorkingStartTime();

        LocalTime endTime =
                attendance.getWorkingEndTime();


        /*
         * 出勤時刻がない場合
         */
        if (startTime == null) {

            attendance.setWorkingTime(
                    "0時間00分"
            );

            attendance.setStatus(
                    "未出勤"
            );

            return;
        }


        /*
         * 出勤済みだが退勤していない場合
         */
        if (endTime == null) {

            attendance.setWorkingTime(
                    "勤務中"
            );

            attendance.setStatus(
                    "勤務中"
            );

            return;
        }


        /*
         * 出勤時刻と退勤時刻が両方ある場合
         */
        Duration duration =
                Duration.between(
                        startTime,
                        endTime
                );

        /*
         * 日をまたぐ勤務の場合に対応する
         *
         * 例：
         * 出勤 22:00
         * 退勤 06:00
         */
        if (duration.isNegative()) {

            duration =
                    duration.plusDays(1);
        }

        long hours =
                duration.toHours();

        long minutes =
                duration.toMinutes() % 60;

        String workingTime =
                String.format(
                        "%d時間%02d分",
                        hours,
                        minutes
                );

        attendance.setWorkingTime(
                workingTime
        );

        attendance.setStatus(
                "正常"
        );
    }
}