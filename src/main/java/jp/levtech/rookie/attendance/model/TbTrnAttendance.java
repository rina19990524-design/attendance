package jp.levtech.rookie.attendance.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbTrnAttendance {

    /**
     * 出勤日
     */
    public static final int WORK_TYPE_WORKING_DAY = 1;

    /**
     * 公休
     */
    public static final int WORK_TYPE_HOLIDAY = 2;

    /**
     * 固定休憩時間
     */
    private static final long BREAK_MINUTES = 60;


    private String attendanceId;

    private String userId;

    private LocalDate workingDay;

    private LocalTime workingStartTime;

    private LocalTime workingEndTime;

    private LocalTime actualWorkingStartTime;

    private LocalTime actualWorkingEndTime;

    private int workType;


    /**
     * 勤怠修正申請の状態
     *
     * null：申請なし
     * 1：申請中
     * 2：承認済み
     *
     * この項目はtb_trn_attendanceには保存しない。
     * 勤怠一覧画面の表示にだけ使用する。
     */
    private Integer requestFlag;


    /**
     * 公休か確認する
     */
    public boolean isHoliday() {

        return workType
                == WORK_TYPE_HOLIDAY;
    }


    /**
     * 勤怠修正申請中か確認する
     */
    public boolean isRequestPending() {

        return requestFlag != null
                && requestFlag
                    == TbTrnAttendanceRequest
                        .REQUEST_FLAG_PENDING;
    }


    /**
     * 勤怠修正申請が承認済みか確認する
     */
    public boolean isRequestApproved() {

        return requestFlag != null
                && requestFlag
                    == TbTrnAttendanceRequest
                        .REQUEST_FLAG_APPROVED;
    }


    /**
     * 申告した勤務時間を表示する
     */
    public String getScheduledWorkingHoursDisplay() {

        if (isHoliday()) {

            return "公休";
        }

        if (workingStartTime == null
                || workingEndTime == null) {

            return "--:--";
        }

        long scheduledMinutes =
                calculateWorkingMinutes(
                        workingStartTime,
                        workingEndTime
                );

        return formatMinutes(
                scheduledMinutes
        );
    }


    /**
     * 実際の勤務時間を表示する
     */
    public String getActualWorkingHoursDisplay() {

        if (isHoliday()) {

            return "―";
        }

        if (actualWorkingStartTime == null
                || actualWorkingEndTime == null) {

            return "--:--";
        }

        long actualMinutes =
                calculateWorkingMinutes(
                        actualWorkingStartTime,
                        actualWorkingEndTime
                );

        return formatMinutes(
                actualMinutes
        );
    }


    /**
     * 残業時間を表示する
     */
    public String getOvertimeDisplay() {

        if (isHoliday()) {

            return "―";
        }

        if (workingStartTime == null
                || workingEndTime == null
                || actualWorkingStartTime == null
                || actualWorkingEndTime == null) {

            return "--:--";
        }

        long scheduledMinutes =
                calculateWorkingMinutes(
                        workingStartTime,
                        workingEndTime
                );

        long actualMinutes =
                calculateWorkingMinutes(
                        actualWorkingStartTime,
                        actualWorkingEndTime
                );

        long overtimeMinutes =
                actualMinutes - scheduledMinutes;

        if (overtimeMinutes <= 0) {

            return "00:00";
        }

        return formatMinutes(
                overtimeMinutes
        );
    }


    /**
     * 備考欄へ表示する文字を返す
     */
    public String getRemarkDisplay() {

        // 公休を最優先で表示
        if (isHoliday()) {

            return "公休";
        }

        // 勤怠修正申請中
        if (isRequestPending()) {

            return "勤怠修正申請中";
        }

        // 承認済み
        if (isRequestApproved()) {

            return "承認済み";
        }

        return "―";
    }


    /**
     * 休憩時間を除いた勤務時間を計算する
     */
    private long calculateWorkingMinutes(
            LocalTime startTime,
            LocalTime endTime) {

        long totalMinutes =
                Duration.between(
                        startTime,
                        endTime
                ).toMinutes();

        // 日をまたぐ勤務
        if (totalMinutes < 0) {

            totalMinutes += 24 * 60;
        }

        // 固定休憩時間の60分を引く
        long workingMinutes =
                totalMinutes - BREAK_MINUTES;

        return Math.max(
                workingMinutes,
                0
        );
    }


    /**
     * 分をHH:mm形式へ変換する
     */
    private String formatMinutes(
            long totalMinutes) {

        long hours =
                totalMinutes / 60;

        long minutes =
                totalMinutes % 60;

        return String.format(
                "%02d:%02d",
                hours,
                minutes
        );
    }
}