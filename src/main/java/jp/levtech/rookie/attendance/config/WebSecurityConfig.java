package jp.levtech.rookie.attendance.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Webセキュリティに関する設定
 */
@Configuration
public class WebSecurityConfig {

    /**
     * HTTPリクエストに対するセキュリティを設定する
     *
     * @param http HTTPセキュリティ
     * @param loginSuccessHandler ログイン成功後の処理
     * @return セキュリティ設定
     * @throws Exception セキュリティ設定に失敗した場合
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LoginSuccessHandler loginSuccessHandler)
            throws Exception {

        http
            // ログインに関する設定
            .formLogin(form -> form

                // ログイン画面のURL
                .loginPage("/login")

                // ログイン成功後の処理
                .successHandler(loginSuccessHandler)

                // ログイン画面は全員アクセス可能
                .permitAll()
            )

            // ログアウトに関する設定
            .logout(logout -> logout

                // ログアウト処理を実行するURL
                .logoutUrl("/logout")

                // ログアウト成功後の移動先
                .logoutSuccessUrl("/login?logout")

                // ログアウト処理は全員利用可能
                .permitAll()
            )

            // URLごとのアクセス制限
            .authorizeHttpRequests(authorize -> authorize

                // CSSやJavaScriptなどの静的ファイルは
                // ログインしていなくても利用可能
                .requestMatchers(
                    PathRequest
                        .toStaticResources()
                        .atCommonLocations()
                )
                .permitAll()

                // 最初の画面は全員アクセス可能
                .requestMatchers("/")
                .permitAll()

                // その他の画面はログイン済みの人だけ利用可能
                .anyRequest()
                .authenticated()
            );

        return http.build();
    }


    /**
     * BCrypt形式のパスワードエンコーダーを作成する
     *
     * @return パスワードエンコーダー
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}