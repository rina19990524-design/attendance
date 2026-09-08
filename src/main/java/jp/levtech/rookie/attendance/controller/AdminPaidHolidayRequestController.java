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

import jp.levtech.rookie.attendance.dto.AdminPaidHolidayRequestView;
import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.TestRepository;
import jp.levtech.rookie.attendance.service.AdminPaidHolidayRequestService;

@Controller
public class AdminPaidHolidayRequestController {

    private final TestRepository testRepository;

    private final AdminPaidHolidayRequestService
            adminPaidHolidayRequestService;


    public AdminPaidHolidayRequestController(
            TestRepository testRepository,
            AdminPaidHolidayRequestService
                    adminPaidHolidayRequestService) {

        this.testRepository =
                testRepository;

        this.adminPaidHolidayRequestService =
                adminPaidHolidayRequestService;
    }


    /**
     * 承認待ちの有給申請一覧を表示する
     */
    @GetMapping("/admin/paid-holiday-requests")
    public String showPaidHolidayRequests(
            Principal principal,
            Model model) {

        // ログイン中の管理者情報を取得
        TbMstEmployee admin =
                getLoginAdmin(principal);

        // 承認待ちの有給申請を取得
        List<AdminPaidHolidayRequestView>
                paidHolidayRequests =
                    adminPaidHolidayRequestService
                        .findPendingRequests();

        // 管理者情報をHTMLへ渡す
        model.addAttribute(
                "admin",
                admin
        );

        // 有給申請一覧をHTMLへ渡す
        model.addAttribute(
                "paidHolidayRequests",
                paidHolidayRequests
        );

        return "admin-paid-holiday-requests";
    }


    /**
     * 有給申請を1件承認する
     */
    @PostMapping(
        "/admin/paid-holiday-requests/{requestId}/approve"
    )
    public String approveRequest(
            Principal principal,

            @PathVariable
            String requestId,

            RedirectAttributes redirectAttributes) {

        // 管理者であることを確認
        getLoginAdmin(principal);

        // 有給申請を1件承認
        adminPaidHolidayRequestService
            .approveRequest(requestId);

        redirectAttributes.addFlashAttribute(
                "message",
                "有給申請を承認しました"
        );

        return "redirect:/admin/paid-holiday-requests";
    }


    /**
     * 選択された有給申請を一括承認する
     */
    @PostMapping(
        "/admin/paid-holiday-requests/approve-selected"
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
                    "承認する有給申請を選択してください"
            );

            return "redirect:/admin/paid-holiday-requests";
        }

        // 選択された有給申請を一括承認
        int approvedCount =
                adminPaidHolidayRequestService
                    .approveRequests(requestIds);

        redirectAttributes.addFlashAttribute(
                "message",
                approvedCount
                + "件の有給申請を承認しました"
        );

        return "redirect:/admin/paid-holiday-requests";
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