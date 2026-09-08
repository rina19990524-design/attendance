package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.model.TbTrnAttendance;
import jp.levtech.rookie.attendance.repository.AttendanceRepository;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Controller
public class HomeController {

    private final TestRepository testRepository;

    private final AttendanceRepository attendanceRepository;


    public HomeController(
            TestRepository testRepository,
            AttendanceRepository attendanceRepository) {

        this.testRepository = testRepository;
        this.attendanceRepository = attendanceRepository;
    }


    /**
     * 従業員ホーム画面を表示する
     */
    @GetMapping("/home")
    public String home(
            Principal principal,
            Model model) {

        // ログイン中の社員IDを取得
        String employeeId =
                principal.getName();


        // ログイン中の社員情報を取得
        TbMstEmployee employee =
                testRepository
                    .findByEmployeeId(employeeId)
                    .orElseThrow(() ->
                        new IllegalStateException(
                            "ログイン中の社員情報が見つかりません"
                        )
                    );

        model.addAttribute(
                "employee",
                employee
        );


        // 本日の日付を取得
        LocalDate today =
                LocalDate.now();

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern(
                        "yyyy年M月d日"
                );

        model.addAttribute(
                "today",
                today.format(dateFormatter)
        );


        // 初期状態では出勤・退勤時刻をnullにする
        model.addAttribute(
                "actualWorkingStartTime",
                null
        );

        model.addAttribute(
                "actualWorkingEndTime",
                null
        );


        // 本日の勤怠情報を取得
        Optional<TbTrnAttendance> attendanceOptional =
                attendanceRepository
                    .findByUserIdAndWorkingDay(
                            employeeId,
                            today
                    );


        // 本日の勤怠情報が存在する場合
        if (attendanceOptional.isPresent()) {

            TbTrnAttendance attendance =
                    attendanceOptional.get();

            DateTimeFormatter timeFormatter =
                    DateTimeFormatter.ofPattern(
                            "HH:mm"
                    );


            // 出勤時刻が登録されている場合
            if (attendance
                    .getActualWorkingStartTime()
                    != null) {

                model.addAttribute(
                        "actualWorkingStartTime",
                        attendance
                            .getActualWorkingStartTime()
                            .format(timeFormatter)
                );
            }


            // 退勤時刻が登録されている場合
            if (attendance
                    .getActualWorkingEndTime()
                    != null) {

                model.addAttribute(
                        "actualWorkingEndTime",
                        attendance
                            .getActualWorkingEndTime()
                            .format(timeFormatter)
                );
            }
        }

        return "home";
    }


    /**
     * 出勤を打刻する
     */
    @PostMapping("/attendance/start")
    public String start(
            Principal principal,
            RedirectAttributes redirectAttributes) {

        // ログイン中の社員IDを取得
        String employeeId =
                principal.getName();


        // 本日の日付を取得
        LocalDate today =
                LocalDate.now();


        // 現在時刻を取得
        LocalTime nowTime =
                LocalTime.now()
                    .withNano(0);


        // 本日の勤怠情報を取得
        Optional<TbTrnAttendance> existingAttendance =
                attendanceRepository
                    .findByUserIdAndWorkingDay(
                            employeeId,
                            today
                    );


        // 本日の勤怠情報がすでに存在する場合
        if (existingAttendance.isPresent()) {

            TbTrnAttendance attendance =
                    existingAttendance.get();


            // すでに出勤済みの場合は更新しない
            if (attendance
                    .getActualWorkingStartTime()
                    != null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "本日の出勤打刻はすでに完了しています"
                );

                return "redirect:/home";
            }


            // 出勤時刻を保存
            attendance.setActualWorkingStartTime(
                    nowTime
            );

            attendanceRepository.update(
                    attendance
            );

        } else {

            // 本日の勤怠情報が存在しない場合は新規作成
            TbTrnAttendance attendance =
                    new TbTrnAttendance();

            attendance.setAttendanceId(
                    UUID.randomUUID()
                        .toString()
                        .substring(0, 10)
            );

            attendance.setUserId(
                    employeeId
            );

            attendance.setWorkingDay(
                    today
            );

            attendance.setActualWorkingStartTime(
                    nowTime
            );

            attendanceRepository.insert(
                    attendance
            );
        }


        redirectAttributes.addFlashAttribute(
                "message",
                "出勤を打刻しました"
        );

        return "redirect:/home";
    }


    /**
     * 退勤を打刻する
     */
    @PostMapping("/attendance/end")
    public String end(
            Principal principal,
            RedirectAttributes redirectAttributes) {

        // ログイン中の社員IDを取得
        String employeeId =
                principal.getName();


        // 本日の日付を取得
        LocalDate today =
                LocalDate.now();


        // 現在時刻を取得
        LocalTime nowTime =
                LocalTime.now()
                    .withNano(0);


        // 本日の勤怠情報を取得
        Optional<TbTrnAttendance> existingAttendance =
                attendanceRepository
                    .findByUserIdAndWorkingDay(
                            employeeId,
                            today
                    );


        // 本日の勤怠情報が存在しない場合
        if (existingAttendance.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "出勤打刻を先に行ってください"
            );

            return "redirect:/home";
        }


        TbTrnAttendance attendance =
                existingAttendance.get();


        // 出勤時刻が登録されていない場合
        if (attendance
                .getActualWorkingStartTime()
                == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "出勤打刻を先に行ってください"
            );

            return "redirect:/home";
        }


        // すでに退勤済みの場合
        if (attendance
                .getActualWorkingEndTime()
                != null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "本日の退勤打刻はすでに完了しています"
            );

            return "redirect:/home";
        }


        // 退勤時刻を保存
        attendance.setActualWorkingEndTime(
                nowTime
        );

        attendanceRepository.update(
                attendance
        );


        redirectAttributes.addFlashAttribute(
                "message",
                "退勤を打刻しました"
        );

        return "redirect:/home";
    }
}