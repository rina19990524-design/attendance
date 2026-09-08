package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jp.levtech.rookie.attendance.dto.AdminAttendanceView;
import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.TestRepository;
import jp.levtech.rookie.attendance.service.AdminAttendanceService;

@Controller
public class AdminAttendanceController {

    private final TestRepository testRepository;

    private final AdminAttendanceService
            adminAttendanceService;


    public AdminAttendanceController(
            TestRepository testRepository,
            AdminAttendanceService adminAttendanceService) {

        this.testRepository = testRepository;

        this.adminAttendanceService =
                adminAttendanceService;
    }


    /**
     * 管理者用の勤怠実績一覧を表示する
     */
    @GetMapping("/admin/attendance")
    public String showAttendanceList(
            Principal principal,

            @RequestParam(
                name = "targetMonth",
                required = false
            )
            String targetMonth,

            @RequestParam(
                name = "departmentId",
                required = false
            )
            Integer departmentId,

            @RequestParam(
                name = "keyword",
                required = false,
                defaultValue = ""
            )
            String keyword,

            Model model) {

        // ログイン中の管理者情報を取得
        TbMstEmployee admin =
                getLoginAdmin(principal);

        // 検索対象月を作成
        YearMonth selectedMonth =
                parseTargetMonth(targetMonth);

        // 前後の空白を削除
        String trimmedKeyword =
                keyword.trim();

        // 検索条件に一致する勤怠実績を取得
        List<AdminAttendanceView> attendances =
                adminAttendanceService
                    .findAttendances(
                        selectedMonth,
                        departmentId,
                        trimmedKeyword
                    );

        // 管理者情報をHTMLへ渡す
        model.addAttribute(
                "admin",
                admin
        );

        // 勤怠実績一覧をHTMLへ渡す
        model.addAttribute(
                "attendances",
                attendances
        );

        // 選択した対象月をHTMLへ戻す
        model.addAttribute(
                "targetMonth",
                selectedMonth.toString()
        );

        // 選択した部署をHTMLへ戻す
        model.addAttribute(
                "departmentId",
                departmentId
        );

        // 入力した検索文字をHTMLへ戻す
        model.addAttribute(
                "keyword",
                trimmedKeyword
        );

        return "admin-attendance";
    }


    /**
     * 画面から受け取った年月をYearMonthへ変換する
     */
    private YearMonth parseTargetMonth(
            String targetMonth) {

        // 最初に画面を開いた場合は現在の月を使用
        if (targetMonth == null
                || targetMonth.isBlank()) {

            return YearMonth.now();
        }

        try {

            // 例：「2026-09」をYearMonthへ変換
            return YearMonth.parse(targetMonth);

        } catch (DateTimeParseException exception) {

            throw new IllegalArgumentException(
                    "対象月の形式が正しくありません"
            );
        }
    }


    /**
     * ログイン中の管理者情報を取得する
     */
    private TbMstEmployee getLoginAdmin(
            Principal principal) {

        if (principal == null) {

            throw new AccessDeniedException(
                    "ログインが必要です"
            );
        }

        String adminId =
                principal.getName();

        TbMstEmployee admin =
                testRepository
                    .findByEmployeeId(adminId)
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "ログイン中の社員情報が"
                            + "見つかりません"
                        )
                    );

        // employeeTypeが2なら管理者
        if (admin.getEmployeeType() != 2) {

            throw new AccessDeniedException(
                    "管理者だけが利用できる画面です"
            );
        }

        return admin;
    }
}