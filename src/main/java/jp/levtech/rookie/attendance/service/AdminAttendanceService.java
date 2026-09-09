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

    /**
     * 固定休憩時間
     */
    private static final long BREAK_MINUTES = 60;


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

        // 各勤怠に実働時間と状態を設定
        for (AdminAttendanceView attendance
                : attendances) {

            setWorkingTimeAndStatus(
                    attendance
            );
        }

        return attendances;
    }


    /**
     * 実働時間と勤怠状態を設定する
     */
    private void setWorkingTimeAndStatus(
            AdminAttendanceView attendance) {

        LocalDate workingDay =
                attendance.getWorkingDay();

        /*
         * 実際の出勤・退勤打刻を取得する
         */
        LocalTime actualStartTime =
                attendance.getActualWorkingStartTime();

        LocalTime actualEndTime =
                attendance.getActualWorkingEndTime();


        /*
         * 公休の場合
         */
        if (attendance.isHoliday()) {

            attendance.setWorkingTime(
                    "―"
            );

            attendance.setStatus(
                    "休日"
            );

            return;
        }


        /*
         * 承認済みの有給申請がある場合
         */
        if (Boolean.TRUE.equals(
                attendance.getPaidLeave())) {

            /*
             * 半休などで出退勤している場合は、
             * 実働時間も表示する
             */
            if (actualStartTime != null
                    && actualEndTime != null) {

                attendance.setWorkingTime(
                        calculateWorkingTime(
                                actualStartTime,
                                actualEndTime
                        )
                );

            } else {

                attendance.setWorkingTime(
                        "―"
                );
            }

            attendance.setStatus(
                    "有給"
            );

            return;
        }


        /*
         * 勤務日が取得できない場合
         */
        if (workingDay == null) {

            attendance.setWorkingTime(
                    "--:--"
            );

            attendance.setStatus(
                    "打刻漏れ"
            );

            return;
        }


        /*
         * 出勤時刻と退勤時刻が両方ある場合
         */
        if (actualStartTime != null
                && actualEndTime != null) {

            attendance.setWorkingTime(
                    calculateWorkingTime(
                            actualStartTime,
                            actualEndTime
                    )
            );

            attendance.setStatus(
                    "正常"
            );

            return;
        }


        /*
         * 今日、出勤済みで退勤していない場合
         */
        if (workingDay.isEqual(LocalDate.now())
                && actualStartTime != null
                && actualEndTime == null) {

            attendance.setWorkingTime(
                    "--:--"
            );

            attendance.setStatus(
                    "勤務中"
            );

            return;
        }


        /*
         * 未来の出勤予定日で、
         * 出勤・退勤がまだない場合
         */
        if (workingDay.isAfter(LocalDate.now())
                && actualStartTime == null
                && actualEndTime == null) {

            attendance.setWorkingTime(
                    "--:--"
            );

            attendance.setStatus(
                    "予定"
            );

            return;
        }


        /*
         * 過去または当日の勤務日で、
         * 必要な打刻が不足している場合
         */
        attendance.setWorkingTime(
                "--:--"
        );

        attendance.setStatus(
                "打刻漏れ"
        );
    }


    /**
     * 実際の出勤時刻と退勤時刻から
     * 実働時間を計算する
     */
    private String calculateWorkingTime(
            LocalTime startTime,
            LocalTime endTime) {

        Duration duration =
                Duration.between(
                        startTime,
                        endTime
                );

        /*
         * 日をまたぐ勤務の場合
         *
         * 例：
         * 出勤 22:00
         * 退勤 06:00
         */
        if (duration.isNegative()) {

            duration =
                    duration.plusDays(1);
        }

        long totalMinutes =
                duration.toMinutes();

        /*
         * 勤務時間が60分を超えている場合、
         * 固定休憩時間を引く
         */
        if (totalMinutes > BREAK_MINUTES) {

            totalMinutes -= BREAK_MINUTES;
        }

        long hours =
                totalMinutes / 60;

        long minutes =
                totalMinutes % 60;

        return String.format(
                "%d時間%02d分",
                hours,
                minutes
        );
    }
}