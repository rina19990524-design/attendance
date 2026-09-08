package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.levtech.rookie.attendance.dto.AdminAttendanceRequestView;
import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.TestRepository;
import jp.levtech.rookie.attendance.service.AdminAttendanceRequestService;

@Controller
public class AdminAttendanceRequestController {

    private final TestRepository testRepository;

    private final AdminAttendanceRequestService
            adminAttendanceRequestService;


    public AdminAttendanceRequestController(
            TestRepository testRepository,
            AdminAttendanceRequestService
                    adminAttendanceRequestService) {

        this.testRepository = testRepository;

        this.adminAttendanceRequestService =
                adminAttendanceRequestService;
    }


    /**
     * 承認待ちの勤怠申請一覧を表示する
     */
    @GetMapping("/admin/attendance-requests")
    public String showAttendanceRequests(
            Principal principal,
            Model model) {

        // ログイン中の管理者を取得
        TbMstEmployee admin =
                getLoginAdmin(principal);

        // 承認待ちの勤怠申請を取得
        List<AdminAttendanceRequestView>
                attendanceRequests =
                    adminAttendanceRequestService
                        .findPendingRequests();

        // 管理者情報をHTMLへ渡す
        model.addAttribute(
                "admin",
                admin
        );

        // 勤怠申請一覧をHTMLへ渡す
        model.addAttribute(
                "attendanceRequests",
                attendanceRequests
        );

        return "admin-attendance-requests";
    }


    /**
     * 勤怠申請を1件承認する
     */
    @PostMapping(
        "/admin/attendance-requests/{requestId}/approve"
    )
    public String approveRequest(
            Principal principal,

            @PathVariable
            String requestId,

            RedirectAttributes redirectAttributes) {

        // 管理者であることを確認
        getLoginAdmin(principal);

        // 申請を1件承認
        adminAttendanceRequestService
            .approveRequest(requestId);

        redirectAttributes.addFlashAttribute(
                "message",
                "勤怠申請を承認しました"
        );

        return "redirect:/admin/attendance-requests";
    }


    /**
     * 選択された勤怠申請を一括承認する
     */
    @PostMapping(
        "/admin/attendance-requests/approve-selected"
    )
    public String approveSelectedRequests(
            Principal principal,

            @RequestParam(
                name = "requestIds",
                required = false
            )
            List<String> requestIds,

            RedirectAttributes redirectAttributes) {

        // 管理者であることを確認
        getLoginAdmin(principal);

        // 何も選択されていない場合
        if (requestIds == null
                || requestIds.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "承認する申請を選択してください"
            );

            return "redirect:/admin/attendance-requests";
        }

        // 選択された申請を一括承認
        int approvedCount =
                adminAttendanceRequestService
                    .approveRequests(requestIds);

        redirectAttributes.addFlashAttribute(
                "message",
                approvedCount
                + "件の勤怠申請を承認しました"
        );

        return "redirect:/admin/attendance-requests";
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