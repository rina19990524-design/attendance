package jp.levtech.rookie.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jp.levtech.rookie.attendance.repository.TestRepository;

/**
 * ホーム画面を管理するコントローラー
 */
@Controller
public class LoginController {
	
	private final TestRepository testRepository;
	
	public LoginController (TestRepository testRepository ) {
		
		this.testRepository = testRepository;
		
	}

	/**
	 * ログイン画面を扱う。
	 *
	 * @return テンプレート
	 */
	@GetMapping("/login")
	public String login() {
		// レンダリングに利用するテンプレート名を返す。
		return "login";
	}

}