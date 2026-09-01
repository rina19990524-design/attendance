package jp.levtech.rookie.attendance.controller;

import java.security.Principal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Controller
public class AdminController {

    private final TestRepository testRepository;


    public AdminController(
            TestRepository testRepository) {

        this.testRepository = testRepository;
    }


    /**
     * 管理者ホームを表示する
     */
    @GetMapping("/admin")
    public String showAdminHome(
            Principal principal,
            Model model) {

        // ログインしている人の社員IDを取得
        String employeeId =
                principal.getName();

        // DBからログイン中の社員情報を取得
        TbMstEmployee employee =
                testRepository
                    .findByEmployeeId(employeeId)
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "ログイン中の社員情報が見つかりません"
                        )
                    );

        // 管理者ではない場合はアクセスを拒否
        if (employee.getEmployeeType() != 2) {

            throw new AccessDeniedException(
                    "管理者だけが利用できる画面です"
            );
        }

        // 管理者情報をadmin.htmlへ渡す
        model.addAttribute(
                "employee",
                employee
        );

        // 未承認の勤怠申請数
        // 件数を取得する機能は後で追加
        model.addAttribute(
                "attendanceRequestCount",
                0
        );

        // 未承認の有給申請数
        // 件数を取得する機能は後で追加
        model.addAttribute(
                "paidHolidayRequestCount",
                0
        );

        // admin.htmlを表示
        return "admin";
    }
}