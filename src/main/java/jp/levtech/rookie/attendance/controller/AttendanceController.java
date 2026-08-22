package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.model.TbTrnAttendance;
import jp.levtech.rookie.attendance.repository.AttendanceRepository;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Controller
public class AttendanceController {

    private final TestRepository testRepository;
    private final AttendanceRepository attendanceRepository;

    public AttendanceController(
            TestRepository testRepository,
            AttendanceRepository attendanceRepository) {

        this.testRepository = testRepository;
        this.attendanceRepository = attendanceRepository;
    }


    // 勤怠実績一覧画面を表示
    @GetMapping("/attendance")
    public String attendance(
            Principal principal,
            Model model) {

        // ログイン中の社員IDを取得
        String employeeId = principal.getName();

        // 社員情報を取得
        Optional<TbMstEmployee> employee =
                testRepository.findByEmployeeId(employeeId);

        // HTMLに社員情報を渡す
        model.addAttribute(
                "employee",
                employee.get()
        );

        // ログイン中の社員の勤怠データを全部取得
        List<TbTrnAttendance> attendanceList =
                attendanceRepository.findByUserId(employeeId);

        // HTMLに勤怠一覧を渡す
        model.addAttribute(
                "attendanceList",
                attendanceList
        );

        return "attendance";
    }


    // 勤務時間を登録
    @PostMapping("/attendance/register")
    public String register(
            Principal principal,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam LocalTime workingStartTime,
            @RequestParam LocalTime workingEndTime,
            @RequestParam(defaultValue = "false") boolean excludeWeekend) {

        String employeeId = principal.getName();

        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {

            boolean isWeekend =
                    date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY;

            // チェックなし → 全日登録
            // チェックあり → 土日だけ飛ばす
            if (!excludeWeekend || !isWeekend) {

                Optional<TbTrnAttendance> existingAttendance =
                        attendanceRepository.findByUserIdAndWorkingDay(
                                employeeId,
                                date
                        );

                if (existingAttendance.isPresent()) {

                    TbTrnAttendance attendance =
                            existingAttendance.get();

                    attendance.setWorkingStartTime(workingStartTime);
                    attendance.setWorkingEndTime(workingEndTime);

                    attendanceRepository.update(attendance);

                } else {

                    TbTrnAttendance attendance =
                            new TbTrnAttendance();

                    attendance.setAttendanceId(
                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 10)
                    );

                    attendance.setUserId(employeeId);
                    attendance.setWorkingDay(date);
                    attendance.setWorkingStartTime(workingStartTime);
                    attendance.setWorkingEndTime(workingEndTime);

                    attendanceRepository.insert(attendance);
                }
            }

            date = date.plusDays(1);
        }

        return "redirect:/attendance";
    }
}