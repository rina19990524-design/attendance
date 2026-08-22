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


    @GetMapping("/home")
    public String home(
            Principal principal,
            Model model) {

        String employeeId = principal.getName();

        Optional<TbMstEmployee> employee =
                testRepository.findByEmployeeId(employeeId);

        model.addAttribute(
                "employee",
                employee.get()
        );

        LocalDate today = LocalDate.now();

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("yyyy年M月d日");

        model.addAttribute(
                "today",
                today.format(dateFormatter)
        );

        Optional<TbTrnAttendance> attendance =
                attendanceRepository.findByUserIdAndWorkingDay(
                        employeeId,
                        today
                );

        if (attendance.isPresent()) {

            TbTrnAttendance todayAttendance =
                    attendance.get();

            DateTimeFormatter timeFormatter =
                    DateTimeFormatter.ofPattern("HH:mm");


            // 実際の出勤時間
            if (todayAttendance.getActualWorkingStartTime() != null) {

                model.addAttribute(
                        "actualWorkingStartTime",
                        todayAttendance
                                .getActualWorkingStartTime()
                                .format(timeFormatter)
                );
            }


            // 実際の退勤時間
            if (todayAttendance.getActualWorkingEndTime() != null) {

                model.addAttribute(
                        "actualWorkingEndTime",
                        todayAttendance
                                .getActualWorkingEndTime()
                                .format(timeFormatter)
                );
            }
        }

        return "home";
    }


    // 出勤
    @PostMapping("/attendance/start")
    public String start(Principal principal) {

        String employeeId = principal.getName();

        LocalDate today = LocalDate.now();

        LocalTime nowTime = LocalTime.now();

        Optional<TbTrnAttendance> existingAttendance =
                attendanceRepository.findByUserIdAndWorkingDay(
                        employeeId,
                        today
                );


        if (existingAttendance.isPresent()) {

            TbTrnAttendance attendance =
                    existingAttendance.get();

            // 実際の出勤時間を保存
            attendance.setActualWorkingStartTime(nowTime);

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

            attendance.setWorkingDay(today);

            // 実際の出勤時間
            attendance.setActualWorkingStartTime(nowTime);

            attendanceRepository.insert(attendance);
        }

        return "redirect:/home";
    }


    // 退勤
    @PostMapping("/attendance/end")
    public String end(Principal principal) {

        String employeeId = principal.getName();

        LocalDate today = LocalDate.now();

        LocalTime nowTime = LocalTime.now();

        Optional<TbTrnAttendance> existingAttendance =
                attendanceRepository.findByUserIdAndWorkingDay(
                        employeeId,
                        today
                );


        if (existingAttendance.isPresent()) {

            TbTrnAttendance attendance =
                    existingAttendance.get();

            // 実際の退勤時間を保存
            attendance.setActualWorkingEndTime(nowTime);

            attendanceRepository.update(attendance);
        }

        return "redirect:/home";
    }
}