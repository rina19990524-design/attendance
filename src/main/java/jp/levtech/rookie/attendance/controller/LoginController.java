package jp.levtech.rookie.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ログイン画面を表示するコントローラー
 */
@Controller
public class LoginController {

    /**
     * ログイン画面を表示する
     *
     * @return ログイン画面のテンプレート名
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}