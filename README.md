## サービスのURL
https://www.rina-attendance.mydns.jp:8443/login

## サービスへの想い
初めてのアプリ開発として、これまでの業務経験で身近だった勤怠管理をテーマに選びました。<br>
業務の中で発生する勤怠管理の手間や確認作業を解決するため、<br>
従業員が簡単に打刻・申請でき、管理者が効率よく確認できるサービスを開発しました。<br>
利用者目線を大切にし、日々の業務を支えるシステムを目指しています。

## アプリケーションのイメージ
![イメージ](src/main/resources/docs/bannerkoubou-gif-20260914-214817.gif)

## 機能一覧
| ログイン画面 | 従業員ホーム画面 | 
| :--------------------------: | :-------------------------: | 
| ![ログイン画面](src/main/resources/docs/img/login.png) | ![従業員ホーム画面](src/main/resources/docs/img/home-employer.png)
| ログインIDとパスワードでの認証機能を実装しました。| 出勤・退勤を打刻し、当日の打刻時刻を確認できます。勤怠一覧・勤怠修正・有給申請の各画面へ移動できます。 |

| 勤怠一覧画面 | 勤怠申請画面 | 
| :--------------------------: | :-------------------------: | 
| ![勤怠一覧画面](src/main/resources/docs/img/attendance-employer.png) | ![勤怠申請画面](src/main/resources/docs/img/attendance-request.png) 
| 勤務予定の登録・日ごとの打刻実績・申請状況の確認ができます。 | 打刻漏れや勤務時間の修正を、理由を添えて申請できます。 |

| 有給申請画面 | 管理者ホーム画面 | 
| :--------------------------: | :-------------------------: | 
| ![有給申請画面](src/main/resources/docs/img/holiday-request.png) | ![管理者ホーム画面](src/main/resources/docs/img/home-manager.png) 
| 取得日と有給の種類を選び、理由を添えて申請できます。 | 社員管理や勤怠確認、申請の承認画面へ移動できます。 |

| 社員管理画面 | 新規登録画面 | 
| :--------------------------: | :-------------------------: | 
| ![社員管理画面](src/main/resources/docs/img/employer-list.png) | ![新規登録画面](src/main/resources/docs/img/employer-regist.png) 
| 社員の検索・登録・編集ができます。 | 社員情報を登録し、社員IDと初期パスワードを発行します。 |

| 勤怠実績確認画面 | 勤怠承認画面 |
| :--------------------------: | :-------------------------: | 
| ![勤怠実績確認画面](src/main/resources/docs/img/attendance-manager.png) | ![勤怠承認画面](src/main/resources/docs/img/attendance-approval.png)
| 月や部署、社員で絞り込み、勤務状況を確認できます。 | 申請内容と変更前後の時刻を確認し、承認できます。 |

## 使用技術
| 分類 | 使用技術 |
| :--- | :--- |
| フロントエンド | HTML , CSS , JavaScript、Thymeleaf |
| バックエンド | Java 21 , Spring Boot , Spring Security , MyBatis |
| データベース | PostgreSQL |
| インフラ | AWS EC2 |
| 開発環境・バージョン管理 | Eclipse , Gradle , Git , GitHub |

## システム構成図
<table>
  <tr>
    <td>
      <img src="src/main/resources/docs/img/system-configuration-diagram.png" alt=構成図">
    </td>
  </tr>
</table>

## ER図
<table>
  <tr>
    <td>
      <img src="src/main/resources/docs/img/ER.png" alt="ER図">
    </td>
  </tr>
</table>

## 今後の展望
本アプリでは、出退勤の打刻や勤怠修正、有給休暇の申請、管理者による承認など、勤怠管理の基本機能を実装しています。<br>
将来的には、以下の５つのフェーズに分けて段階的に機能の追加し、従業員と管理者の双方が使いやすいアプリを目指します。<br>
・フェーズ１：申請履歴の確認機能<br>
　過去の勤怠修正や有給休暇の申請内容と、承認や却下の結果を一覧で確認できる機能を追加する。<br>
 
・フェーズ２：有給休暇の残日数の表示機能<br>
　有給休暇の取得日数と残日数の確認ができる機能を追加する。<br>
 
・フェーズ３：承認済みの申請の取り消し機能<br>
　従業員が承認済み申請の取り消しを申請し、管理者の確認と承認を経て取り消せる機能を追加する。<br>
 
・フェーズ４：通知機能<br>
　申請が承認や却下された際に、アプリ内に通知を表示する機能を追加する。<br>

 ・フェーズ５：操作前の確認画面の改善<br>
 　パスワードの再設定など、重要な操作を行う際の確認画面をアプリ内に表示し、ご操作を防ぎながら、画像デザインを統一する。
