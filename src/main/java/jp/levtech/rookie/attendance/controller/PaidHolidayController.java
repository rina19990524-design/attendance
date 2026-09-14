package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
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
import jp.levtech.rookie.attendance.model.TbTrnAttendanceRequest;
import jp.levtech.rookie.attendance.model.TbTrnPaidHolidayRequest;
import jp.levtech.rookie.attendance.repository.AttendanceRepository;
import jp.levtech.rookie.attendance.repository.AttendanceRequestRepository;
import jp.levtech.rookie.attendance.repository.PaidHolidayRequestRepository;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Controller
public class PaidHolidayController {

    private static final int FULL_DAY = 1;
    private static final int MORNING_HALF_DAY = 2;
    private static final int AFTERNOON_HALF_DAY = 3;

    private static final int REQUESTING = 1;
    private static final int APPROVED = 2;

    private static final long HALF_DAY_HOURS = 4;
    private static final long FULL_SCHEDULE_HOURS = 9;

    private final TestRepository testRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceRequestRepository
            attendanceRequestRepository;
    private final PaidHolidayRequestRepository
            paidHolidayRequestRepository;

    public PaidHolidayController(
            TestRepository testRepository,
            AttendanceRepository attendanceRepository,
            AttendanceRequestRepository attendanceRequestRepository,
            PaidHolidayRequestRepository paidHolidayRequestRepository) {

        this.testRepository = testRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceRequestRepository =
                attendanceRequestRepository;
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

        // 本人の申請中一覧を取消ボタン用に渡す
        List<TbTrnPaidHolidayRequest> pendingRequests =
                paidHolidayRequestRepository
                    .findPendingByEmployeeId(employeeId);

        model.addAttribute(
                "pendingRequests",
                pendingRequests
        );

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
         * 一括申請の全日付を確認してから保存する。
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

            Optional<TbTrnAttendance> attendanceOptional =
                    attendanceRepository
                        .findByUserIdAndWorkingDay(
                                employeeId,
                                holiday
                        );

            /*
             * 1日有給と打刻実績は同日に併用できない。
             */
            if (holidayType == FULL_DAY
                    && attendanceOptional.isPresent()
                    && isClocked(attendanceOptional.get())) {

                return redirectWithError(
                        holiday
                            + "は打刻済みのため"
                            + "1日有給を申請できません",
                        redirectAttributes
                );
            }

            List<TbTrnAttendanceRequest> attendanceRequests =
                    attendanceRequestRepository
                        .findActiveByUserIdAndWorkingDay(
                                employeeId,
                                holiday
                        );

            /*
             * 1日有給は、申請中・承認済みの勤怠修正と
             * 同じ日に併用できない。
             */
            if (holidayType == FULL_DAY
                    && !attendanceRequests.isEmpty()) {

                return redirectWithError(
                        holiday
                            + "は勤怠修正を申請中または"
                            + "承認済みのため"
                            + "1日有給を申請できません",
                        redirectAttributes
                );
            }

            /*
             * 半休は、休む4時間と勤務時間が
             * 重ならない場合だけ申請できる。
             */
            if (holidayType != FULL_DAY) {

                if (attendanceOptional.isEmpty()
                        || !hasNineHourSchedule(
                                attendanceOptional.get()
                        )) {

                    return redirectWithError(
                            holiday
                                + "は9時間の勤務予定が"
                                + "登録されていないため"
                                + "半休を判定できません",
                            redirectAttributes
                    );
                }

                TbTrnAttendance attendance =
                        attendanceOptional.get();

                LocalTime leaveStart =
                        holidayType == MORNING_HALF_DAY
                            ? attendance.getWorkingStartTime()
                            : attendance.getWorkingEndTime()
                                .minusHours(HALF_DAY_HOURS);

                LocalTime leaveEnd =
                        holidayType == MORNING_HALF_DAY
                            ? attendance.getWorkingStartTime()
                                .plusHours(HALF_DAY_HOURS)
                            : attendance.getWorkingEndTime();

                /*
                 * 実際の打刻時間も確認する。
                 * 片方しか打刻されていない場合は、
                 * 勤務時間が確定できないため拒否する。
                 */
                LocalTime actualStart =
                        attendance.getActualWorkingStartTime();
                LocalTime actualEnd =
                        attendance.getActualWorkingEndTime();

                if ((actualStart == null) != (actualEnd == null)) {

                    return redirectWithError(
                            holiday
                                + "は出退勤の時刻が揃っていないため"
                                + "半休を申請できません",
                            redirectAttributes
                    );
                }

                if (actualStart != null
                        && (!actualStart.isBefore(actualEnd)
                            || overlaps(
                                    actualStart,
                                    actualEnd,
                                    leaveStart,
                                    leaveEnd
                            ))) {

                    return redirectWithError(
                            holiday
                                + "は打刻時間と半休の時間が"
                                + "重なるため申請できません",
                            redirectAttributes
                    );
                }

                /*
                 * 打刻漏れなどの勤怠修正申請も確認する。
                 * 時刻が片方だけの場合は、重複しないと
                 * 確認できないため拒否する。
                 */
                for (TbTrnAttendanceRequest attendanceRequest
                        : attendanceRequests) {

                    LocalTime requestedStart =
                            attendanceRequest.getWorkingStartTime();
                    LocalTime requestedEnd =
                            attendanceRequest.getWorkingEndTime();

                    if (requestedStart == null
                            || requestedEnd == null
                            || !requestedStart.isBefore(requestedEnd)
                            || overlaps(
                                    requestedStart,
                                    requestedEnd,
                                    leaveStart,
                                    leaveEnd
                            )) {

                        return redirectWithError(
                                holiday
                                    + "は勤怠修正申請の勤務時間と"
                                    + "半休の時間が重なるか"
                                    + "確認できないため、"
                                    + "申請できません",
                                redirectAttributes
                        );
                    }
                }
            }

            /*
             * 申請中・承認済みの有給との重複を確認する。
             * 取消済み（3）は取得対象外。
             */
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
                 * 1日有給と半休は併用不可。
                 * 同じ半休の再申請も不可。
                 * 午前半休＋午後半休は許可する。
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

    /**
     * 本人の申請中の有給申請を取り消す
     */
    @PostMapping("/paid-holiday/cancel")
    @Transactional
    public String cancel(
            Principal principal,
            @RequestParam String requestId,
            RedirectAttributes redirectAttributes) {

        String employeeId = principal.getName();

        int updatedCount =
                paidHolidayRequestRepository
                    .cancelPendingRequest(
                            requestId,
                            employeeId
                    );

        if (updatedCount == 1) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "有給申請を取り消しました"
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "申請を取り消せませんでした。"
                    + "承認済み、取消済み、または"
                    + "対象の申請が存在しない可能性があります"
            );
        }

        return "redirect:/paid-holiday";
    }

    /**
     * 9時間拘束の勤務予定があるか確認する。
     * このうち午前・午後に4時間ずつ勤務し、
     * 間の1時間を休憩として扱う。
     */
    private boolean hasNineHourSchedule(
            TbTrnAttendance attendance) {

        LocalTime start =
                attendance.getWorkingStartTime();
        LocalTime end =
                attendance.getWorkingEndTime();

        return start != null
                && end != null
                && start.isBefore(end)
                && Duration.between(start, end).toHours()
                    == FULL_SCHEDULE_HOURS
                && Duration.between(start, end).toMinutes()
                    == FULL_SCHEDULE_HOURS * 60;
    }

    /**
     * 2つの時間帯が重なるか確認する。
     * 終了時刻と開始時刻が同じ場合は重複しない。
     */
    private boolean overlaps(
            LocalTime firstStart,
            LocalTime firstEnd,
            LocalTime secondStart,
            LocalTime secondEnd) {

        return firstStart.isBefore(secondEnd)
                && secondStart.isBefore(firstEnd);
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