package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
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
            Model model,
            @RequestParam(required = false) String month) {

        // ログイン中の社員IDを取得
        String employeeId = principal.getName();

        // 社員情報を取得
        Optional<TbMstEmployee> employee =
                testRepository.findByEmployeeId(employeeId);

        model.addAttribute(
                "employee",
                employee.get()
        );


        // 表示する月を決める
        YearMonth targetMonth;

        if (month == null || month.isEmpty()) {

            // 月指定がなければ今月
            targetMonth = YearMonth.now();

        } else {

            // 指定された月
            targetMonth = YearMonth.parse(month);
        }


        // ログイン中の社員の勤怠をすべて取得
        List<TbTrnAttendance> attendanceList =
                attendanceRepository.findByUserId(employeeId);


        // 選択した月のデータだけに絞る
        List<TbTrnAttendance> monthlyAttendanceList =
                attendanceList.stream()
                        .filter(attendance ->
                                attendance.getWorkingDay() != null
                                && YearMonth.from(attendance.getWorkingDay())
                                        .equals(targetMonth))
                        .toList();


        // 勤怠一覧をHTMLへ渡す
        model.addAttribute(
                "attendanceList",
                monthlyAttendanceList
        );

        // 表示している月
        model.addAttribute(
                "targetMonth",
                targetMonth
        );

        // 前月
        model.addAttribute(
                "previousMonth",
                targetMonth.minusMonths(1)
        );

        // 翌月
        model.addAttribute(
                "nextMonth",
                targetMonth.plusMonths(1)
        );


        return "attendance";
    }


    // 勤務時間をまとめて登録
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


        // 開始日から終了日まで繰り返す
        while (!date.isAfter(endDate)) {

            boolean isWeekend =
                    date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY;


            // チェックなし → 全日登録
            // チェックあり → 土日を除外
            if (!excludeWeekend || !isWeekend) {

                Optional<TbTrnAttendance> existingAttendance =
                        attendanceRepository.findByUserIdAndWorkingDay(
                                employeeId,
                                date
                        );


                // すでにその日のデータがある場合
                if (existingAttendance.isPresent()) {

                    TbTrnAttendance attendance =
                            existingAttendance.get();

                    attendance.setWorkingStartTime(
                            workingStartTime
                    );

                    attendance.setWorkingEndTime(
                            workingEndTime
                    );

                    attendanceRepository.update(attendance);

                } else {

                    // その日のデータがない場合
                    TbTrnAttendance attendance =
                            new TbTrnAttendance();

                    attendance.setAttendanceId(
                            UUID.randomUUID()
                                    .toString()
                                    .substring(0, 10)
                    );

                    attendance.setUserId(employeeId);

                    attendance.setWorkingDay(date);

                    attendance.setWorkingStartTime(
                            workingStartTime
                    );

                    attendance.setWorkingEndTime(
                            workingEndTime
                    );

                    attendanceRepository.insert(attendance);
                }
            }

            // 次の日へ
            date = date.plusDays(1);
        }


        return "redirect:/attendance";
    }
}