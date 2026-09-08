package jp.levtech.rookie.attendance.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理者用有給申請一覧の1行分を表すクラス
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaidHolidayRequestView {

    /**
     * 有給申請ID
     */
    private String paidHolidayRequestId;


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
     * 休暇区分
     *
     * 1：1日有給
     * 2：午前半休
     * 3：午後半休
     */
    private int holidayType;


    /**
     * HTMLへ表示する休暇区分名
     */
    private String holidayTypeName;


    /**
     * 有給取得日
     */
    private LocalDate holidayDate;


    /**
     * 申請理由
     */
    private String requestReason;
}