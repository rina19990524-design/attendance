package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.model.TbTrnPaidHolidayRequest;
import jp.levtech.rookie.attendance.repository.PaidHolidayRequestRepository;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Controller
public class PaidHolidayController {

    private final TestRepository testRepository;

    private final PaidHolidayRequestRepository
            paidHolidayRequestRepository;


    public PaidHolidayController(
            TestRepository testRepository,
            PaidHolidayRequestRepository paidHolidayRequestRepository) {

        this.testRepository = testRepository;

        this.paidHolidayRequestRepository =
                paidHolidayRequestRepository;
    }


    // 有給申請画面を表示する
    @GetMapping("/paid-holiday")
    public String paidHoliday(
            Principal principal,
            Model model) {

        // ログインしている社員のIDを取得
        String employeeId =
                principal.getName();

        // 社員IDを使って社員情報を取得
        Optional<TbMstEmployee> employee =
                testRepository.findByEmployeeId(employeeId);

        // 社員情報をHTMLへ渡す
        model.addAttribute(
                "employee",
                employee.get()
        );

        // paidholiday.htmlを表示
        return "paidholiday";
    }


    // 有給申請を登録する
    @PostMapping("/paid-holiday/register")
    public String register(
            Principal principal,

            @RequestParam int holidayType,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            List<LocalDate> holidays,

            @RequestParam String reason,

            RedirectAttributes redirectAttributes) {

        // ログインしている社員のIDを取得
        String employeeId =
                principal.getName();

        // 選択された日付を1件ずつ取り出す
        for (LocalDate holiday : holidays) {

            // 空の有給申請データを作る
            TbTrnPaidHolidayRequest request =
                    new TbTrnPaidHolidayRequest();

            // 有給申請IDを作って設定
            request.setPaidHolidayRequestId(
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 10)
            );

            // 休暇区分を設定
            request.setHolidayType(holidayType);

            // 有給を取得する日付を設定
            request.setHoliday(holiday);

            // 社員IDを設定
            request.setEmployeeId(employeeId);

            // 申請理由を設定
            request.setRequestReason(reason);

            // 申請フラグを設定
            // 1：申請中
            // 2：承認済み
            request.setRequestFlag(1);

            // Repositoryへ渡してDBに登録
            paidHolidayRequestRepository.insert(request);
        }

        // 申請後の画面に表示するメッセージ
        redirectAttributes.addFlashAttribute(
                "message",
                "申請しました"
        );

        // 有給申請画面へ移動
        return "redirect:/paid-holiday";
    }
}