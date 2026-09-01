package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.levtech.rookie.attendance.dto.EmployeePasswordResetResult;
import jp.levtech.rookie.attendance.dto.EmployeeRegistrationResult;
import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.TestRepository;
import jp.levtech.rookie.attendance.service.EmployeeService;

@Controller
@RequestMapping("/admin/users")
public class AdminEmployeeController {

    private final TestRepository testRepository;

    private final EmployeeService employeeService;


    public AdminEmployeeController(
            TestRepository testRepository,
            EmployeeService employeeService) {

        this.testRepository = testRepository;
        this.employeeService = employeeService;
    }


    /**
     * 社員管理画面を表示する
     */
    @GetMapping
    public String showEmployeeList(
            Principal principal,

            @RequestParam(
                name = "keyword",
                required = false,
                defaultValue = ""
            )
            String keyword,

            Model model) {

        TbMstEmployee admin =
                getLoginAdmin(principal);

        List<TbMstEmployee> employees =
                employeeService.findEmployees(
                        keyword
                );

        model.addAttribute(
                "admin",
                admin
        );

        model.addAttribute(
                "employees",
                employees
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        return "admin-users";
    }


    /**
     * 社員新規登録画面を表示する
     */
    @GetMapping("/new")
    public String showRegistrationForm(
            Principal principal,
            Model model) {

        TbMstEmployee admin =
                getLoginAdmin(principal);

        model.addAttribute(
                "admin",
                admin
        );

        return "admin-user-new";
    }


    /**
     * 社員または管理者を新規登録する
     */
    @PostMapping("/new")
    public String registerEmployee(
            Principal principal,

            @RequestParam
            String employeeName,

            @RequestParam
            int departmentId,

            @RequestParam
            String employeeEmail,

            @RequestParam
            @DateTimeFormat(
                iso = DateTimeFormat.ISO.DATE
            )
            LocalDate hireDate,

            @RequestParam
            int employeeStatusId,

            @RequestParam
            int employeeType,

            RedirectAttributes redirectAttributes) {

        getLoginAdmin(principal);

        EmployeeRegistrationResult result =
                employeeService.registerEmployee(
                        employeeName,
                        departmentId,
                        employeeEmail,
                        hireDate,
                        employeeStatusId,
                        employeeType
                );

        redirectAttributes.addFlashAttribute(
                "message",
                "登録が完了しました。"
                + "社員IDとパスワードは"
                + "安全な方法でお伝えください。"
        );

        redirectAttributes.addFlashAttribute(
                "registrationResult",
                result
        );

        return "redirect:/admin/users/new";
    }


    /**
     * 社員編集画面を表示する
     */
    @GetMapping("/{employeeId}/edit")
    public String showEditForm(
            Principal principal,

            @PathVariable
            String employeeId,

            Model model) {

        TbMstEmployee admin =
                getLoginAdmin(principal);

        TbMstEmployee employee =
                testRepository
                    .findByEmployeeId(employeeId)
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "編集する社員が見つかりません"
                        )
                    );

        model.addAttribute(
                "admin",
                admin
        );

        model.addAttribute(
                "employee",
                employee
        );

        return "admin-user-edit";
    }


    /**
     * 社員情報を更新する
     */
    @PostMapping("/{employeeId}/edit")
    public String updateEmployee(
            Principal principal,

            @PathVariable
            String employeeId,

            @RequestParam
            String employeeName,

            @RequestParam
            int departmentId,

            @RequestParam
            String employeeEmail,

            @RequestParam
            @DateTimeFormat(
                iso = DateTimeFormat.ISO.DATE
            )
            LocalDate hireDate,

            @RequestParam
            int employeeStatusId,

            @RequestParam
            int employeeType,

            RedirectAttributes redirectAttributes) {

        getLoginAdmin(principal);

        employeeService.updateEmployee(
                employeeId,
                employeeName,
                departmentId,
                employeeEmail,
                hireDate,
                employeeStatusId,
                employeeType
        );

        redirectAttributes.addFlashAttribute(
                "message",
                "社員情報を更新しました"
        );

        return "redirect:/admin/users/"
                + employeeId
                + "/edit";
    }


    /**
     * パスワードを再設定する
     */
    @PostMapping("/{employeeId}/password-reset")
    public String resetPassword(
            Principal principal,

            @PathVariable
            String employeeId,

            RedirectAttributes redirectAttributes) {

        // 管理者であることを確認
        getLoginAdmin(principal);

        // 新しい仮パスワードを発行
        EmployeePasswordResetResult result =
                employeeService.resetPassword(
                        employeeId
                );

        redirectAttributes.addFlashAttribute(
                "passwordMessage",
                "パスワードを再設定しました。"
                + "新しい仮パスワードは"
                + "安全な方法で本人へお伝えください。"
        );

        /*
         * 仮パスワードはFlashAttributeに入れる。
         * そのため、次に表示する画面で一度だけ確認できる。
         */
        redirectAttributes.addFlashAttribute(
                "passwordResetResult",
                result
        );

        return "redirect:/admin/users/"
                + employeeId
                + "/edit";
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

        // 2：管理者
        if (admin.getEmployeeType() != 2) {

            throw new AccessDeniedException(
                    "管理者だけが利用できる画面です"
            );
        }

        return admin;
    }
}