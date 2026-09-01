package jp.levtech.rookie.attendance.model;

import java.util.Date;

import lombok.Value;

@Value
public class TbMstEmployee {

    private final String employeeId;

    private final String employeeName;

    private final int departmentId;

    private final String employeeEmail;

    private final String employeePassword;

    private final Date hireDate;

    private final int employeeStatusId;

    private final int employeeType;
}