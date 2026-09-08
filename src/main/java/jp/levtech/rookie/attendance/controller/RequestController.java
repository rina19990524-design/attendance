package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.model.TbTrnAttendance;
import jp.levtech.rookie.attendance.model.TbTrnAttendanceRequest;
import jp.levtech.rookie.attendance.repository.AttendanceRepository;
import jp.levtech.rookie.attendance.repository.AttendanceRequestRepository;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Controller
public class RequestController {

	private final AttendanceRepository attendanceRepository;
	private final AttendanceRequestRepository attendanceRequestRepository;
	private final TestRepository testRepository;

    public RequestController(
    		TestRepository testRepository,
    		AttendanceRepository attendanceRepository,
            AttendanceRequestRepository attendanceRequestRepository) {
        this.testRepository = testRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceRequestRepository = attendanceRequestRepository;
    }

    @GetMapping("/request")
    public String request(Principal principal, Model model) {

        String employeeId = principal.getName();

        Optional<TbMstEmployee> employee =
                testRepository.findByEmployeeId(employeeId);

        model.addAttribute("employee", employee.get());

        return "request";
    }
    
    @PostMapping("/request/register")
    public String register(
            Principal principal,
            @RequestParam LocalDate workingDay,
            @RequestParam LocalTime workingStartTime,
            @RequestParam LocalTime workingEndTime,
            @RequestParam String reason,
            @RequestParam int requestType,
            RedirectAttributes redirectAttributes) {

        String employeeId =
                principal.getName();

        Optional<TbTrnAttendance> attendance =
                attendanceRepository
                        .findByUserIdAndWorkingDay(
                                employeeId,
                                workingDay
                        );

        /*
         * 指定日の勤怠データが存在しない場合
         */
        if (attendance.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "指定した日付の勤怠情報がありません"
            );

            return "redirect:/request";
        }

        TbTrnAttendance todayAttendance =
                attendance.get();

        TbTrnAttendanceRequest request =
                new TbTrnAttendanceRequest();

        request.setAttendanceRequestId(
                UUID.randomUUID()
                        .toString()
                        .substring(0, 10)
        );

        request.setAttendanceId(
                todayAttendance.getAttendanceId()
        );

        request.setWorkingStartTime(
                workingStartTime
        );

        request.setWorkingEndTime(
                workingEndTime
        );

        request.setRequestReason(
                reason
        );

        /*
         * 1 = 申請中
         */
        request.setRequestFlag(
                TbTrnAttendanceRequest.REQUEST_FLAG_PENDING
        );

        request.setRequestType(
                requestType
        );

        attendanceRequestRepository.insert(
                request
        );

        /*
         * リダイレクト後に表示するメッセージ
         */
        redirectAttributes.addFlashAttribute(
                "message",
                "申請しました"
        );

        return "redirect:/request";
    }
    
}