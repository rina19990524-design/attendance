package jp.levtech.rookie.attendance.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理者用勤怠申請一覧の1行分を表すクラス
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAttendanceRequestView {

    /**
     * 勤怠申請ID
     */
    private String attendanceRequestId;


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
     * 変更前の勤務開始時刻
     */
    private LocalTime beforeStartTime;


    /**
     * 変更前の勤務終了時刻
     */
    private LocalTime beforeEndTime;


    /**
     * 変更後の勤務開始始時刻
     */
    private LocalTime afterStartTime;


    /**
     * 変更後の勤務終了時刻
     */
    private LocalTime afterEndTime;


    /**
     * HTMLへ表示する変更前の時間
     *
     * 例：09:00～18:00
     */
    private String beforeTime;


    /**
     * HTMLへ表示する変更後の時間
     *
     * 例：09:30～18:30
     */
    private String afterTime;


    /**
     * 申請種別
     *
     * 1：勤怠修正
     * 2：打刻漏れ
     */
    private int requestType;


    /**
     * HTMLへ表示する申請種別名
     */
    private String requestTypeName;


    /**
     * 申請理由
     */
    private String requestReason;
}