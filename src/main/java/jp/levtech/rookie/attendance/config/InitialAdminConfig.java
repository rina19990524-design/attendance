package jp.levtech.rookie.attendance.config;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jp.levtech.rookie.attendance.repository.TestRepository;
import jp.levtech.rookie.attendance.service.EmployeeService;

@Configuration
public class InitialAdminConfig {

    /**
     * 最初の管理者を登録する
     */
    @Bean
    public CommandLineRunner createInitialAdmin(
            EmployeeService employeeService,
            TestRepository testRepository,

			@Value("${INITIAL_ADMIN_PASSWORD:}")
            String initialAdminPassword) {

        return args -> {

            // A0001がすでに存在する場合は何もしない
            if (testRepository
                    .findByEmployeeId("A0001")
                    .isPresent()) {

                return;
            }

            // 環境変数にパスワードが設定されているか確認
            if (initialAdminPassword.isBlank()) {

                throw new IllegalStateException(
                        "INITIAL_ADMIN_PASSWORDを設定してください"
                );
            }

            // 最初の管理者を登録
            employeeService.register(
                    "A0001",
                    "山田太郎",
                    3,
                    "admin@attendance.co.jp",
                    initialAdminPassword,
                    Date.valueOf(LocalDate.now()),
                    2
            );
        };
    }
}