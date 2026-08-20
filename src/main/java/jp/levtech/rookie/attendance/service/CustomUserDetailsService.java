package jp.levtech.rookie.attendance.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.TestRepository;

/**
 * ログインユーザーの詳細を管理するサービス
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

	/**
	 * ログインユーザーを管理するリポジトリ\]
	 */
	private final TestRepository testRepository;

	/**
	 * ログインユーザーの詳細を管理するサービスのコンストラクタ
	 *
	 * @param loginUserRepository ログインユーザーを管理するリポジトリ
	 */
	public CustomUserDetailsService(TestRepository testRepository) {
		// ログインユーザーを管理するリポジトリを初期化する。
		this.testRepository = testRepository;
	}

	/**
	 * ユーザー名からログインユーザーの詳細を取得する。
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// ログインユーザーを表す変数loginUserを定義する。
		// ログインユーザーを管理するリポジトリに、ユーザー名からログインユーザーを検索するよう依頼する。
		Optional<TbMstEmployee> loginUser = testRepository.findByEmployeeId(username);
		// ログインユーザーが見つからなかった場合は例外を投げる。
		if (loginUser.isEmpty()) {
			throw new UsernameNotFoundException("ログインユーザーが見つかりませんでした");
		}
		// ログインユーザーの詳細を返す。
		return User
				// ログインユーザーのユーザー名を設定する。
				.withUsername(loginUser.get().getEmployeeId())
				// ログインユーザーのパスワードを設定する。
				.password(loginUser.get().getEmployeePassword())
				// ログインユーザーが無効か設定する。
				// 有効を反転して無効にしている。
				.disabled(false)
				// ログインユーザーのロールを設定する。
				.roles("USER")
				.build();
	}

}