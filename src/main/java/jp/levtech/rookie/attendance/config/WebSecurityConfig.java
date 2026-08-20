package jp.levtech.rookie.attendance .config;

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
	 * HTTPリクエストに対するセキュリティを設定するBean
	 *
	 * @param http HTTPセキュリティ
	 * @return セキュリティに関する設定
	 * @throws Exception
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// formでのログインに関する設定
			.formLogin(form -> form
				// ログイン画面のパスを /login に設定する。
				.loginPage("/login")
				//ホーム画面のパスを /homeに設定する
				.defaultSuccessUrl("/home", true)
				// ログイン画面へのアクセスを全ユーザーに許可する。
				.permitAll()
			)
			// ログアウトに関する設定
			.logout(logout -> logout
				// ログアウト機能のパスを /logout に設定する。
				.logoutUrl("/logout")
				// ログアウトが成功した場合にリダイレクトするパスを /?logout に設定する。
				.logoutSuccessUrl("/?logout")
				// ログアウト機能へのアクセスを全ユーザーに許可する。
				.permitAll()
			)
			// 認可に関する設定
			.authorizeHttpRequests(authorize -> authorize
				// CSS, JavaScriptなど静的リソースへのアクセスを全ユーザーに許可する。
				.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
				.permitAll()
				// ホーム画面へのアクセスを全ユーザーに許可する。
				.requestMatchers("/")
				.permitAll()
				// その他へのアクセスを認証済みのユーザーのみに制限する。
				.anyRequest()
				.authenticated()
			);
		return http.build();
	}
	
	/**
	 * パスワードのエンコーダーのBean
	 *
	 * @return パスワードのエンコーダー
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		// bcryptと呼ばれる強力なハッシュ関数を利用してパスワードを暗号化するエンコーダーを返す。
		return new BCryptPasswordEncoder();
	}

}
