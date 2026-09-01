package jp.levtech.rookie.attendance.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TbTrnPaidHolidayRequest {

    private String paidHolidayRequestId;

    private int holidayType;

    private LocalDate holiday;

    private String employeeId;

    private String requestReason;

    private int requestFlag;
}