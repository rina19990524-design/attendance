package jp.levtech.rookie.attendance.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Component
public class LoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private static final int EMPLOYEE_TYPE_EMPLOYEE = 1;

    private static final int EMPLOYEE_TYPE_ADMIN = 2;

    private final TestRepository testRepository;


    public LoginSuccessHandler(
            TestRepository testRepository) {

        this.testRepository = testRepository;
    }


    /**
     * ログイン成功後の処理
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        // ログインした社員IDを取得
        String employeeId =
                authentication.getName();

        // DBから社員情報を取得
        TbMstEmployee employee =
                testRepository
                    .findByEmployeeId(employeeId)
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "社員情報が見つかりません"
                        )
                    );

        // 社員タイプを取得
        int employeeType =
                employee.getEmployeeType();

        // 従業員の場合
        if (employeeType
                == EMPLOYEE_TYPE_EMPLOYEE) {

            response.sendRedirect(
                    request.getContextPath() + "/home"
            );

            return;
        }

        // 管理者の場合
        if (employeeType
                == EMPLOYEE_TYPE_ADMIN) {

            response.sendRedirect(
                    request.getContextPath() + "/admin"
            );

            return;
        }

        // 社員タイプが1でも2でもない場合
        response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "利用できない社員タイプです"
        );
    }
}