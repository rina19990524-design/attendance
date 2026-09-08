package jp.levtech.rookie.attendance.model;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbTrnAttendanceRequest {

    /**
     * 申請中
     */
    public static final int REQUEST_FLAG_PENDING = 1;

    /**
     * 承認済み
     */
    public static final int REQUEST_FLAG_APPROVED = 2;


    private String attendanceRequestId;

    private String attendanceId;

    private LocalTime workingStartTime;

    private LocalTime workingEndTime;

    private String requestReason;

    private int requestFlag;

    private int requestType;


    /**
     * 申請中か確認する
     */
    public boolean isPending() {

        return requestFlag
                == REQUEST_FLAG_PENDING;
    }


    /**
     * 承認済みか確認する
     */
    public boolean isApproved() {

        return requestFlag
                == REQUEST_FLAG_APPROVED;
    }


    /**
     * 画面に表示する申請状況を返す
     */
    public String getRequestStatusDisplay() {

        if (isPending()) {

            return "勤怠修正申請中";
        }

        if (isApproved()) {

            return "承認済み";
        }

        return "―";
    }
}