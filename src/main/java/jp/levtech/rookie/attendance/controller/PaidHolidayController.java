package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.model.TbTrnAttendance;
import jp.levtech.rookie.attendance.model.TbTrnPaidHolidayRequest;
import jp.levtech.rookie.attendance.repository.AttendanceRepository;
import jp.levtech.rookie.attendance.repository.PaidHolidayRequestRepository;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Controller
public class PaidHolidayController {

    private static final int FULL_DAY = 1;
    private static final int MORNING_HALF_DAY = 2;
    private static final int AFTERNOON_HALF_DAY = 3;

    private static final int REQUESTING = 1;
    private static final int APPROVED = 2;

    private final TestRepository testRepository;

    private final AttendanceRepository attendanceRepository;

    private final PaidHolidayRequestRepository
            paidHolidayRequestRepository;

    public PaidHolidayController(
            TestRepository testRepository,
            AttendanceRepository attendanceRepository,
            PaidHolidayRequestRepository paidHolidayRequestRepository) {

        this.testRepository = testRepository;
        this.attendanceRepository = attendanceRepository;
        this.paidHolidayRequestRepository =
                paidHolidayRequestRepository;
    }

    /**
     * 有給申請画面を表示する
     */
    @GetMapping("/paid-holiday")
    public String paidHoliday(
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

        return "paidholiday";
    }

    /**
     * 有給申請を登録する
     */
    @PostMapping("/paid-holiday/register")
    @Transactional
    public String register(
            Principal principal,

            @RequestParam int holidayType,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            List<LocalDate> holidays,

            @RequestParam String reason,

            RedirectAttributes redirectAttributes) {

        if (holidayType != FULL_DAY
                && holidayType != MORNING_HALF_DAY
                && holidayType != AFTERNOON_HALF_DAY) {

            return redirectWithError(
                    "有給の種類を選択してください",
                    redirectAttributes
            );
        }

        if (holidays == null || holidays.isEmpty()) {

            return redirectWithError(
                    "有給を取得する日付を選択してください",
                    redirectAttributes
            );
        }

        String employeeId = principal.getName();
        Set<LocalDate> selectedDates = new HashSet<>();

        /*
         * 保存前にすべての日付を確認する。
         * 途中で登録してからエラーになることを避ける。
         */
        for (LocalDate holiday : holidays) {

            if (holiday == null) {

                return redirectWithError(
                        "有給の日付を確認してください",
                        redirectAttributes
                );
            }

            if (!selectedDates.add(holiday)) {

                return redirectWithError(
                        holiday + "が複数選択されています",
                        redirectAttributes
                );
            }

            Optional<TbTrnAttendance> attendance =
                    attendanceRepository
                        .findByUserIdAndWorkingDay(
                                employeeId,
                                holiday
                        );

            /*
             * 1日有給と打刻実績は同じ日に共存させない。
             * 半休は残り半日の勤務があり得るため、
             * 打刻があるだけでは拒否しない。
             */
            if (holidayType == FULL_DAY
                    && attendance.isPresent()
                    && isClocked(attendance.get())) {

                return redirectWithError(
                        holiday
                            + "は打刻済みのため"
                            + "1日有給を申請できません",
                        redirectAttributes
                );
            }

            List<TbTrnPaidHolidayRequest> existingRequests =
                    paidHolidayRequestRepository
                        .findByEmployeeIdAndPeriod(
                                employeeId,
                                holiday,
                                holiday
                        );

            for (TbTrnPaidHolidayRequest existing
                    : existingRequests) {

                if (existing.getRequestFlag() != REQUESTING
                        && existing.getRequestFlag() != APPROVED) {
                    continue;
                }

                /*
                 * 1日有給と半休は同じ日に併用しない。
                 * 午前半休と午後半休は、それぞれ1件ずつ
                 * 申請できる。
                 */
                boolean conflicts =
                        holidayType == FULL_DAY
                        || existing.getHolidayType() == FULL_DAY
                        || existing.getHolidayType() == holidayType;

                if (conflicts) {

                    return redirectWithError(
                            holiday
                                + "には申請中または承認済みの"
                                + "有給申請があります",
                            redirectAttributes
                    );
                }
            }
        }

        for (LocalDate holiday : holidays) {

            TbTrnPaidHolidayRequest request =
                    new TbTrnPaidHolidayRequest();

            request.setPaidHolidayRequestId(
                    UUID.randomUUID()
                        .toString()
                        .substring(0, 10)
            );
            request.setHolidayType(holidayType);
            request.setHoliday(holiday);
            request.setEmployeeId(employeeId);
            request.setRequestReason(reason);
            request.setRequestFlag(REQUESTING);

            paidHolidayRequestRepository.insert(request);
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "申請しました"
        );

        return "redirect:/paid-holiday";
    }

    private boolean isClocked(
            TbTrnAttendance attendance) {

        return attendance.getActualWorkingStartTime() != null
                || attendance.getActualWorkingEndTime() != null;
    }

    private String redirectWithError(
            String message,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                message
        );

        return "redirect:/paid-holiday";
    }
}