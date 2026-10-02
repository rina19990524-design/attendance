package jp.levtech.rookie.attendance.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    // 公開用のデモ管理者ID
    private static final String DEMO_ADMIN_ID = "E0003";

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            LoginSuccessHandler loginSuccessHandler)
            throws Exception {

        http
            // ログイン
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(loginSuccessHandler)
                .permitAll()
            )

            // ログアウト
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            // アクセス制御
            .authorizeHttpRequests(authorize -> authorize

                // デモ管理者は変更操作を実行できない。
                // GET・HEADは画面の閲覧に使用する。
                // ログイン・ログアウトは上の設定で許可する。
                .requestMatchers(request ->
                    !"GET".equals(request.getMethod())
                    && !"HEAD".equals(request.getMethod())
                    && !"OPTIONS".equals(request.getMethod())
                )
                .access((authentication, context) -> {

                    var user = authentication.get();

                    boolean allowed =
                            user != null
                            && user.isAuthenticated()
                            && !(user instanceof
                                AnonymousAuthenticationToken)
                            && !DEMO_ADMIN_ID.equals(
                                user.getName()
                            );

                    return new AuthorizationDecision(allowed);
                })

                // CSS・JavaScriptなど
                .requestMatchers(
                    PathRequest
                        .toStaticResources()
                        .atCommonLocations()
                )
                .permitAll()

                // 最初の画面
                .requestMatchers("/")
                .permitAll()

                // その他はログインが必要
                .anyRequest()
                .authenticated()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}