package jp.levtech.rookie.attendance.controller;

import java.security.Principal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.PaidHolidayRequestRepository;
import jp.levtech.rookie.attendance.repository.TestRepository;
import jp.levtech.rookie.attendance.service.AdminAttendanceRequestService;

@Controller
public class AdminController {

    /**
     * 有給申請の申請中フラグ
     */
    private static final int PAID_HOLIDAY_REQUESTING = 1;


    private final TestRepository testRepository;

    private final AdminAttendanceRequestService
            adminAttendanceRequestService;

    private final PaidHolidayRequestRepository
            paidHolidayRequestRepository;


    public AdminController(
            TestRepository testRepository,

            AdminAttendanceRequestService
                    adminAttendanceRequestService,

            PaidHolidayRequestRepository
                    paidHolidayRequestRepository) {

        this.testRepository =
                testRepository;

        this.adminAttendanceRequestService =
                adminAttendanceRequestService;

        this.paidHolidayRequestRepository =
                paidHolidayRequestRepository;
    }


    /**
     * 管理者ホームを表示する
     */
    @GetMapping("/admin")
    public String showAdminHome(
            Principal principal,
            Model model) {

        if (principal == null) {

            throw new AccessDeniedException(
                    "ログインが必要です"
            );
        }

        // ログイン中の社員IDを取得
        String employeeId =
                principal.getName();

        // ログイン中の社員情報を取得
        TbMstEmployee employee =
                testRepository
                    .findByEmployeeId(employeeId)
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "ログイン中の社員情報が"
                            + "見つかりません"
                        )
                    );

        // 管理者以外はアクセス不可
        if (employee.getEmployeeType() != 2) {

            throw new AccessDeniedException(
                    "管理者だけが利用できる画面です"
            );
        }

        // 未承認の勤怠申請件数を取得
        int attendanceRequestCount =
                adminAttendanceRequestService
                    .countPendingRequests();

        // 未承認の有給申請件数を取得
        int paidHolidayRequestCount =
                paidHolidayRequestRepository
                    .countByRequestFlag(
                        PAID_HOLIDAY_REQUESTING
                    );

        // 管理者情報をHTMLへ渡す
        model.addAttribute(
                "employee",
                employee
        );

        // 勤怠申請の未承認件数をHTMLへ渡す
        model.addAttribute(
                "attendanceRequestCount",
                attendanceRequestCount
        );

        // 有給申請の未承認件数をHTMLへ渡す
        model.addAttribute(
                "paidHolidayRequestCount",
                paidHolidayRequestCount
        );

        return "admin";
    }
}