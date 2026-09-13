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
     * アプリ内で自動控除する休憩時間
     */
    private static final long NO_BREAK_MINUTES = 0;
    private static final long SHORT_BREAK_MINUTES = 45;
    private static final long LONG_BREAK_MINUTES = 60;

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
     * DBには保存せず、勤怠一覧画面の表示に使用する。
     */
    private Integer requestFlag;

    /**
     * 公休か確認する
     */
    public boolean isHoliday() {
        return workType == WORK_TYPE_HOLIDAY;
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

        return formatMinutes(scheduledMinutes);
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

        return formatMinutes(actualMinutes);
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
                Math.max(
                        actualMinutes - scheduledMinutes,
                        0
                );

        return formatMinutes(overtimeMinutes);
    }

    /**
     * 備考欄へ表示する文字を返す
     */
    public String getRemarkDisplay() {

        if (isHoliday()) {
            return "公休";
        }

        if (isRequestPending()) {
            return "勤怠修正申請中";
        }

        if (isRequestApproved()) {
            return "承認済み";
        }

        return "―";
    }

    /**
     * 開始・終了時刻から、休憩を自動控除して
     * 勤務時間を計算する
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

        long breakMinutes =
                calculateBreakMinutes(totalMinutes);

        return Math.max(
                totalMinutes - breakMinutes,
                0
        );
    }

    /**
     * このアプリの自動休憩ルール。
     *
     * 6時間以内：休憩なし
     * 6時間超～8時間以内：45分
     * 8時間超：60分
     */
    private long calculateBreakMinutes(
            long totalMinutes) {

        if (totalMinutes <= 6 * 60) {
            return NO_BREAK_MINUTES;
        }

        if (totalMinutes <= 8 * 60) {
            return SHORT_BREAK_MINUTES;
        }

        return LONG_BREAK_MINUTES;
    }

    /**
     * 分をHH:mm形式へ変換する
     */
    private String formatMinutes(
            long totalMinutes) {

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return String.format(
                "%02d:%02d",
                hours,
                minutes
        );
    }
}