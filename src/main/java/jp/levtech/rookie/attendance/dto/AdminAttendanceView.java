package jp.levtech.rookie.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理者用勤怠実績一覧の1行分を表すクラス
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAttendanceView {

    /**
     * 勤務日
     */
    private LocalDate workingDay;


    /**
     * 社員ID
     */
    private String employeeId;


    /**
     * 社員名
     */
    private String employeeName;


    /**
     * 部署名
     */
    private String departmentName;


    /**
     * 出勤予定時刻
     */
    private LocalTime workingStartTime;


    /**
     * 退勤予定時刻
     */
    private LocalTime workingEndTime;


    /**
     * 実際の出勤時刻
     */
    private LocalTime actualWorkingStartTime;


    /**
     * 実際の退勤時刻
     */
    private LocalTime actualWorkingEndTime;


    /**
     * 勤務区分
     *
     * 1：出勤日
     * 2：公休
     */
    private Integer workType;


    /**
     * 実働時間
     *
     * 例：8時間00分
     */
    private String workingTime;


    /**
     * 承認済みの有給休暇か
     *
     * true：有給
     * false：有給ではない
     */
    private Boolean paidLeave;


    /**
     * 勤怠状態
     *
     * 正常
     * 勤務中
     * 打刻漏れ
     * 休日
     * 有給
     * 予定
     */
    private String status;


    /**
     * 公休か判定する
     */
    public boolean isHoliday() {

        return workType != null
                && workType == 2;
    }
}