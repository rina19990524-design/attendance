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
     * 実際の出勤時刻
     */
    private LocalTime workingStartTime;


    /**
     * 実際の退勤時刻
     */
    private LocalTime workingEndTime;


    /**
     * 勤務時間
     *
     * 例：8時間04分
     */
    private String workingTime;


    /**
     * 勤怠状態
     *
     * 例：正常、勤務中、未出勤
     */
    private String status;
}