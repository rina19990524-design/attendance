package jp.levtech.rookie.attendance.dto;

/**
 * 社員登録後に画面へ返す情報
 */
public record EmployeeRegistrationResult(
        String employeeId,
        String initialPassword) {
}