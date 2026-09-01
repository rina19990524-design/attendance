package jp.levtech.rookie.attendance.dto;

/**
 * パスワード再設定結果
 *
 * @param employeeId 社員ID
 * @param temporaryPassword 新しい仮パスワード
 */
public record EmployeePasswordResetResult(
        String employeeId,
        String temporaryPassword) {
}