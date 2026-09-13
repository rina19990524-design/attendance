package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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

    // 日付と打刻時刻に共通で使用するタイムゾーン
    private static final ZoneId JAPAN_ZONE =
            ZoneId.of("Asia/Tokyo");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年M月d日");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

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

        String employeeId = principal.getName();

        TbMstEmployee employee =
                testRepository
                        .findByEmployeeId(employeeId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "ログイン中の社員情報が見つかりません"
                                )
                        );

        model.addAttribute("employee", employee);

        // AWSサーバーのタイムゾーンに依存せず、日本の日付を使う
        LocalDate today =
                LocalDate.now(JAPAN_ZONE);

        model.addAttribute(
                "today",
                today.format(DATE_FORMATTER)
        );

        model.addAttribute(
                "actualWorkingStartTime",
                null
        );

        model.addAttribute(
                "actualWorkingEndTime",
                null
        );

        Optional<TbTrnAttendance> attendanceOptional =
                attendanceRepository
                        .findByUserIdAndWorkingDay(
                                employeeId,
                                today
                        );

        if (attendanceOptional.isPresent()) {

            TbTrnAttendance attendance =
                    attendanceOptional.get();

            if (attendance.getActualWorkingStartTime()
                    != null) {

                model.addAttribute(
                        "actualWorkingStartTime",
                        attendance
                                .getActualWorkingStartTime()
                                .format(TIME_FORMATTER)
                );
            }

            if (attendance.getActualWorkingEndTime()
                    != null) {

                model.addAttribute(
                        "actualWorkingEndTime",
                        attendance
                                .getActualWorkingEndTime()
                                .format(TIME_FORMATTER)
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

        String employeeId = principal.getName();

        LocalDate today =
                LocalDate.now(JAPAN_ZONE);

        LocalTime nowTime =
                LocalTime.now(JAPAN_ZONE)
                        .withNano(0);

        Optional<TbTrnAttendance> existingAttendance =
                attendanceRepository
                        .findByUserIdAndWorkingDay(
                                employeeId,
                                today
                        );

        if (existingAttendance.isPresent()) {

            TbTrnAttendance attendance =
                    existingAttendance.get();

            if (attendance.getActualWorkingStartTime()
                    != null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "本日の出勤打刻はすでに完了しています"
                );

                return "redirect:/home";
            }

            attendance.setActualWorkingStartTime(
                    nowTime
            );

            attendanceRepository.update(
                    attendance
            );

        } else {

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

        String employeeId = principal.getName();

        LocalDate today =
                LocalDate.now(JAPAN_ZONE);

        LocalTime nowTime =
                LocalTime.now(JAPAN_ZONE)
                        .withNano(0);

        Optional<TbTrnAttendance> existingAttendance =
                attendanceRepository
                        .findByUserIdAndWorkingDay(
                                employeeId,
                                today
                        );

        if (existingAttendance.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "出勤打刻を先に行ってください"
            );

            return "redirect:/home";
        }

        TbTrnAttendance attendance =
                existingAttendance.get();

        if (attendance.getActualWorkingStartTime()
                == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "出勤打刻を先に行ってください"
            );

            return "redirect:/home";
        }

        if (attendance.getActualWorkingEndTime()
                != null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "本日の退勤打刻はすでに完了しています"
            );

            return "redirect:/home";
        }

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