package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import jp.levtech.rookie.attendance.repository.AttendanceRepository;
import jp.levtech.rookie.attendance.repository.AttendanceRequestRepository;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Controller
public class AttendanceController {

    private static final ZoneId JAPAN_ZONE =
            ZoneId.of("Asia/Tokyo");

    private final TestRepository testRepository;

    private final AttendanceRepository attendanceRepository;

    private final AttendanceRequestRepository
            attendanceRequestRepository;

    public AttendanceController(
            TestRepository testRepository,
            AttendanceRepository attendanceRepository,
            AttendanceRequestRepository
                    attendanceRequestRepository) {

        this.testRepository = testRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceRequestRepository =
                attendanceRequestRepository;
    }

    /**
     * 勤怠実績一覧を表示する
     */
    @GetMapping("/attendance")
    @Transactional(readOnly = true)
    public String attendance(
            Principal principal,
            Model model,
            @RequestParam(required = false)
            String month) {

        String employeeId =
                principal.getName();

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

        YearMonth targetMonth;

        if (month == null
                || month.isBlank()) {

            targetMonth =
                    YearMonth.now(JAPAN_ZONE);

        } else {

            try {

                targetMonth =
                        YearMonth.parse(month);

            } catch (DateTimeParseException exception) {

                targetMonth =
                        YearMonth.now(JAPAN_ZONE);
            }
        }

        LocalDate startDate =
                targetMonth.atDay(1);

        LocalDate endDate =
                targetMonth.atEndOfMonth();

        List<TbTrnAttendance> allAttendanceList =
                attendanceRepository
                    .findByUserId(employeeId);

        YearMonth selectedMonth =
                targetMonth;

        Map<LocalDate, TbTrnAttendance> attendanceMap =
                allAttendanceList
                    .stream()
                    .filter(attendance ->
                        attendance.getWorkingDay() != null
                        && YearMonth
                            .from(attendance.getWorkingDay())
                            .equals(selectedMonth)
                    )
                    .collect(
                        Collectors.toMap(
                            TbTrnAttendance::getWorkingDay,
                            Function.identity(),
                            (first, second) -> first
                        )
                    );

        List<TbTrnAttendanceRequest> requestStatusList =
                attendanceRequestRepository
                    .findStatusesByUserIdAndPeriod(
                            employeeId,
                            startDate,
                            endDate
                    );

        Map<String, Integer> requestStatusMap =
                requestStatusList
                    .stream()
                    .filter(request ->
                        request.getAttendanceId() != null
                    )
                    .collect(
                        Collectors.toMap(
                            TbTrnAttendanceRequest
                                ::getAttendanceId,
                            TbTrnAttendanceRequest
                                ::getRequestFlag,
                            (first, second) -> first
                        )
                    );

        List<TbTrnAttendance> monthlyAttendanceList =
                IntStream
                    .rangeClosed(
                        1,
                        targetMonth.lengthOfMonth()
                    )
                    .mapToObj(day -> {

                        LocalDate workingDay =
                                selectedMonth.atDay(day);

                        TbTrnAttendance attendance =
                                attendanceMap.get(
                                        workingDay
                                );

                        if (attendance != null) {

                            Integer requestFlag =
                                    requestStatusMap.get(
                                            attendance
                                                .getAttendanceId()
                                    );

                            attendance.setRequestFlag(
                                    requestFlag
                            );

                            return attendance;
                        }

                        TbTrnAttendance emptyAttendance =
                                new TbTrnAttendance();

                        emptyAttendance.setUserId(
                                employeeId
                        );

                        emptyAttendance.setWorkingDay(
                                workingDay
                        );

                        emptyAttendance.setWorkType(
                                TbTrnAttendance
                                    .WORK_TYPE_WORKING_DAY
                        );

                        emptyAttendance.setRequestFlag(
                                null
                        );

                        return emptyAttendance;
                    })
                    .toList();

        model.addAttribute(
                "attendanceList",
                monthlyAttendanceList
        );

        model.addAttribute(
                "targetMonth",
                targetMonth
        );

        model.addAttribute(
                "previousMonth",
                targetMonth.minusMonths(1)
        );

        model.addAttribute(
                "nextMonth",
                targetMonth.plusMonths(1)
        );

        return "attendance";
    }

    /**
     * 複数の勤務予定を登録・変更する
     */
    @PostMapping("/attendance/register")
    @Transactional
    public String register(
            Principal principal,

            @RequestParam(
                name = "workingDates",
                required = false
            )
            List<String> workingDateValues,

            @RequestParam(
                name = "workTypes",
                required = false
            )
            List<String> workTypeValues,

            @RequestParam(
                name = "workingStartTimes",
                required = false
            )
            List<String> workingStartTimeValues,

            @RequestParam(
                name = "workingEndTimes",
                required = false
            )
            List<String> workingEndTimeValues,

            RedirectAttributes redirectAttributes) {

        if (workingDateValues == null
                || workingDateValues.isEmpty()) {

            return redirectWithError(
                    YearMonth.now(JAPAN_ZONE),
                    "登録する勤務日を入力してください",
                    redirectAttributes
            );
        }

        int scheduleCount =
                workingDateValues.size();

        if (workTypeValues == null
                || workingStartTimeValues == null
                || workingEndTimeValues == null
                || workTypeValues.size() != scheduleCount
                || workingStartTimeValues.size()
                    != scheduleCount
                || workingEndTimeValues.size()
                    != scheduleCount) {

            return redirectWithError(
                    YearMonth.now(JAPAN_ZONE),
                    "勤務予定の入力内容が正しくありません",
                    redirectAttributes
            );
        }

        YearMonth redirectMonth;

        try {

            redirectMonth =
                    YearMonth.from(
                            LocalDate.parse(
                                    workingDateValues.get(0)
                            )
                    );

        } catch (RuntimeException exception) {

            redirectMonth =
                    YearMonth.now(JAPAN_ZONE);
        }

        List<WorkSchedule> schedules =
                new ArrayList<>();

        Set<LocalDate> selectedDates =
                new HashSet<>();

        for (int index = 0;
                index < scheduleCount;
                index++) {

            LocalDate workingDate;

            try {

                workingDate =
                        LocalDate.parse(
                                workingDateValues.get(index)
                        );

            } catch (RuntimeException exception) {

                return redirectWithError(
                        redirectMonth,
                        (index + 1)
                            + "行目の勤務日が正しくありません",
                        redirectAttributes
                );
            }

            if (!selectedDates.add(workingDate)) {

                return redirectWithError(
                        redirectMonth,
                        workingDate
                            + "が複数入力されています",
                        redirectAttributes
                );
            }

            int workType;

            try {

                workType =
                        Integer.parseInt(
                                workTypeValues.get(index)
                        );

            } catch (NumberFormatException exception) {

                return redirectWithError(
                        redirectMonth,
                        (index + 1)
                            + "行目の勤務区分が正しくありません",
                        redirectAttributes
                );
            }

            if (workType
                    != TbTrnAttendance
                        .WORK_TYPE_WORKING_DAY
                    && workType
                    != TbTrnAttendance
                        .WORK_TYPE_HOLIDAY) {

                return redirectWithError(
                        redirectMonth,
                        (index + 1)
                            + "行目の勤務区分が正しくありません",
                        redirectAttributes
                );
            }

            LocalTime workingStartTime =
                    null;

            LocalTime workingEndTime =
                    null;

            if (workType
                    == TbTrnAttendance
                        .WORK_TYPE_WORKING_DAY) {

                String startTimeValue =
                        workingStartTimeValues.get(index);

                String endTimeValue =
                        workingEndTimeValues.get(index);

                if (startTimeValue == null
                        || startTimeValue.isBlank()
                        || endTimeValue == null
                        || endTimeValue.isBlank()) {

                    return redirectWithError(
                            redirectMonth,
                            (index + 1)
                                + "行目の勤務時間を入力してください",
                            redirectAttributes
                    );
                }

                try {

                    workingStartTime =
                            LocalTime.parse(
                                    startTimeValue
                            );

                    workingEndTime =
                            LocalTime.parse(
                                    endTimeValue
                            );

                } catch (DateTimeParseException exception) {

                    return redirectWithError(
                            redirectMonth,
                            (index + 1)
                                + "行目の勤務時間が正しくありません",
                            redirectAttributes
                    );
                }

                if (!workingStartTime.isBefore(
                        workingEndTime
                )) {

                    return redirectWithError(
                            redirectMonth,
                            (index + 1)
                                + "行目の勤務開始時間は"
                                + "勤務終了時間より前にしてください",
                            redirectAttributes
                    );
                }
            }

            schedules.add(
                    new WorkSchedule(
                            workingDate,
                            workType,
                            workingStartTime,
                            workingEndTime
                    )
            );
        }

        String employeeId =
                principal.getName();

        /*
         * 複数日を登録する前に全件確認する。
         * 打刻済みの日を公休へ変更することは認めない。
         */
        for (WorkSchedule schedule : schedules) {

            Optional<TbTrnAttendance> existingAttendance =
                    attendanceRepository
                        .findByUserIdAndWorkingDay(
                                employeeId,
                                schedule.workingDate()
                        );

            if (existingAttendance.isPresent()
                    && isClocked(existingAttendance.get())
                    && schedule.workType()
                        == TbTrnAttendance
                            .WORK_TYPE_HOLIDAY) {

                return redirectWithError(
                        redirectMonth,
                        schedule.workingDate()
                            + "は打刻済みのため"
                            + "公休に変更できません",
                        redirectAttributes
                );
            }
        }

        int insertedCount =
                0;

        int updatedCount =
                0;

        for (WorkSchedule schedule : schedules) {

            boolean inserted =
                    saveWorkSchedule(
                            employeeId,
                            schedule
                    );

            if (inserted) {

                insertedCount++;

            } else {

                updatedCount++;
            }
        }

        String message =
                "勤務予定を登録しました";

        if (insertedCount > 0
                && updatedCount > 0) {

            message =
                    insertedCount
                    + "件を登録し、"
                    + updatedCount
                    + "件を変更しました";

        } else if (updatedCount > 0) {

            message =
                    updatedCount
                    + "件の勤務予定を変更しました";

        } else if (insertedCount > 0) {

            message =
                    insertedCount
                    + "件の勤務予定を登録しました";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                message
        );

        return "redirect:/attendance?month="
                + redirectMonth;
    }

    /**
     * 出勤・退勤のどちらかを打刻済みか確認する
     */
    private boolean isClocked(
            TbTrnAttendance attendance) {

        return attendance.getActualWorkingStartTime()
                    != null
                || attendance.getActualWorkingEndTime()
                    != null;
    }

    /**
     * 1日分の勤務予定を保存する
     *
     * @return 新規登録の場合true、更新の場合false
     */
    private boolean saveWorkSchedule(
            String employeeId,
            WorkSchedule schedule) {

        Optional<TbTrnAttendance> existingAttendance =
                attendanceRepository
                    .findByUserIdAndWorkingDay(
                            employeeId,
                            schedule.workingDate()
                    );

        if (existingAttendance.isPresent()) {

            TbTrnAttendance attendance =
                    existingAttendance.get();

            /*
             * 保存直前にも確認する。
             * 打刻済みでも出勤日の勤務予定時間は変更できる。
             */
            if (isClocked(attendance)
                    && schedule.workType()
                        == TbTrnAttendance
                            .WORK_TYPE_HOLIDAY) {

                throw new IllegalStateException(
                        schedule.workingDate()
                            + "は打刻済みのため"
                            + "公休に変更できません"
                );
            }

            setWorkSchedule(
                    attendance,
                    schedule
            );

            /*
             * 勤務予定だけを更新する。
             * 打刻実績は更新しない。
             */
            int updatedCount =
                    attendanceRepository
                        .updateWorkSchedule(
                                attendance
                        );

            if (updatedCount != 1) {

                throw new IllegalStateException(
                        schedule.workingDate()
                            + "の勤務予定を"
                            + "更新できませんでした"
                );
            }

            return false;
        }

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
                schedule.workingDate()
        );

        attendance.setActualWorkingStartTime(
                null
        );

        attendance.setActualWorkingEndTime(
                null
        );

        setWorkSchedule(
                attendance,
                schedule
        );

        attendanceRepository.insert(
                attendance
        );

        return true;
    }

    /**
     * 勤務予定を勤怠モデルへ設定する
     */
    private void setWorkSchedule(
            TbTrnAttendance attendance,
            WorkSchedule schedule) {

        attendance.setWorkType(
                schedule.workType()
        );

        if (schedule.workType()
                == TbTrnAttendance
                    .WORK_TYPE_HOLIDAY) {

            attendance.setWorkingStartTime(
                    null
            );

            attendance.setWorkingEndTime(
                    null
            );

        } else {

            attendance.setWorkingStartTime(
                    schedule.workingStartTime()
            );

            attendance.setWorkingEndTime(
                    schedule.workingEndTime()
            );
        }
    }

    /**
     * エラーを設定して勤怠画面へ戻す
     */
    private String redirectWithError(
            YearMonth redirectMonth,
            String errorMessage,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                errorMessage
        );

        return "redirect:/attendance?month="
                + redirectMonth;
    }

    /**
     * 画面から受け取った勤務予定
     */
    private record WorkSchedule(
            LocalDate workingDate,
            int workType,
            LocalTime workingStartTime,
            LocalTime workingEndTime) {
    }
}