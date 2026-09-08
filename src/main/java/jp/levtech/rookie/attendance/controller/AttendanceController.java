package jp.levtech.rookie.attendance.controller;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
public class AttendanceController {

    private final TestRepository testRepository;

    private final AttendanceRepository attendanceRepository;

    private final AttendanceRequestRepository
            attendanceRequestRepository;


    public AttendanceController(
            TestRepository testRepository,
            AttendanceRepository attendanceRepository,
            AttendanceRequestRepository
                    attendanceRequestRepository) {

        this.testRepository =
                testRepository;

        this.attendanceRepository =
                attendanceRepository;

        this.attendanceRequestRepository =
                attendanceRequestRepository;
    }


    /**
     * 勤怠実績一覧を表示する
     */
    @GetMapping("/attendance")
    public String attendance(
            Principal principal,
            Model model,
            @RequestParam(required = false)
            String month) {

        // ログイン中の社員ID
        String employeeId =
                principal.getName();


        // ログイン中の社員情報
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


        // 表示対象月
        YearMonth targetMonth;

        if (month == null
                || month.isBlank()) {

            targetMonth =
                    YearMonth.now();

        } else {

            targetMonth =
                    YearMonth.parse(month);
        }


        // 表示月の開始日
        LocalDate startDate =
                targetMonth.atDay(1);

        // 表示月の終了日
        LocalDate endDate =
                targetMonth.atEndOfMonth();


        // ログイン中の社員の勤怠情報を取得
        List<TbTrnAttendance> allAttendanceList =
                attendanceRepository
                    .findByUserId(employeeId);


        /*
         * 選択月の勤怠情報を
         * 日付をキーにしたMapへ変換する
         */
        Map<LocalDate, TbTrnAttendance> attendanceMap =
                allAttendanceList
                    .stream()
                    .filter(attendance ->
                        attendance.getWorkingDay() != null
                        && YearMonth
                            .from(attendance.getWorkingDay())
                            .equals(targetMonth)
                    )
                    .collect(
                        Collectors.toMap(
                            TbTrnAttendance::getWorkingDay,
                            Function.identity(),
                            (first, second) -> first
                        )
                    );


        /*
         * 表示月の勤怠修正申請状態を取得する
         */
        List<TbTrnAttendanceRequest> requestStatusList =
                attendanceRequestRepository
                    .findStatusesByUserIdAndPeriod(
                            employeeId,
                            startDate,
                            endDate
                    );


        /*
         * 勤怠IDと申請フラグのMapを作成する
         *
         * キー：
         * attendanceId
         *
         * 値：
         * requestFlag
         */
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


        /*
         * 表示月の1日から月末まで作成する
         */
        List<TbTrnAttendance> monthlyAttendanceList =
                IntStream
                    .rangeClosed(
                        1,
                        targetMonth.lengthOfMonth()
                    )
                    .mapToObj(day -> {

                        LocalDate workingDay =
                                targetMonth.atDay(day);

                        TbTrnAttendance attendance =
                                attendanceMap.get(
                                        workingDay
                                );


                        // DBに勤怠情報が存在する場合
                        if (attendance != null) {

                            /*
                             * この勤怠に対応する
                             * 申請状態を設定する
                             */
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


                        /*
                         * DBに勤怠情報が存在しない場合は
                         * 日付だけ設定した表示用データを作る
                         */
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


        // 勤怠一覧
        model.addAttribute(
                "attendanceList",
                monthlyAttendanceList
        );

        // 表示月
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


    /**
     * 勤務時間または公休をまとめて登録する
     */
    @PostMapping("/attendance/register")
    public String register(
            Principal principal,

            @RequestParam
            LocalDate startDate,

            @RequestParam
            LocalDate endDate,

            @RequestParam(required = false)
            LocalTime workingStartTime,

            @RequestParam(required = false)
            LocalTime workingEndTime,

            @RequestParam(defaultValue = "false")
            boolean excludeWeekend,

            @RequestParam(defaultValue = "1")
            int workType,

            RedirectAttributes redirectAttributes) {

        YearMonth redirectMonth =
                YearMonth.from(startDate);


        // 開始日と終了日の確認
        if (startDate.isAfter(endDate)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "開始日は終了日以前の日付にしてください"
            );

            return "redirect:/attendance?month="
                    + redirectMonth;
        }


        // 勤務区分の確認
        if (workType
                != TbTrnAttendance.WORK_TYPE_WORKING_DAY
                && workType
                != TbTrnAttendance.WORK_TYPE_HOLIDAY) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "勤務区分が正しくありません"
            );

            return "redirect:/attendance?month="
                    + redirectMonth;
        }


        /*
         * 出勤日の場合だけ
         * 勤務開始・終了時刻を確認する
         */
        if (workType
                == TbTrnAttendance.WORK_TYPE_WORKING_DAY) {

            if (workingStartTime == null
                    || workingEndTime == null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "勤務開始時間と勤務終了時間を入力してください"
                );

                return "redirect:/attendance?month="
                        + redirectMonth;
            }


            if (!workingStartTime.isBefore(
                    workingEndTime
            )) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "勤務開始時間は勤務終了時間より前にしてください"
                );

                return "redirect:/attendance?month="
                        + redirectMonth;
            }
        }


        String employeeId =
                principal.getName();

        LocalDate date =
                startDate;


        // 開始日から終了日まで繰り返す
        while (!date.isAfter(endDate)) {

            boolean isWeekend =
                    date.getDayOfWeek()
                        == DayOfWeek.SATURDAY
                    || date.getDayOfWeek()
                        == DayOfWeek.SUNDAY;


            if (!excludeWeekend
                    || !isWeekend) {

                saveWorkSchedule(
                        employeeId,
                        date,
                        workingStartTime,
                        workingEndTime,
                        workType
                );
            }


            date =
                    date.plusDays(1);
        }


        String message;

        if (workType
                == TbTrnAttendance.WORK_TYPE_HOLIDAY) {

            message =
                    "公休を登録しました";

        } else {

            message =
                    "勤務時間を登録しました";
        }


        redirectAttributes.addFlashAttribute(
                "message",
                message
        );


        return "redirect:/attendance?month="
                + redirectMonth;
    }


    /**
     * 1日分の勤務予定を保存する
     */
    private void saveWorkSchedule(
            String employeeId,
            LocalDate workingDay,
            LocalTime workingStartTime,
            LocalTime workingEndTime,
            int workType) {

        Optional<TbTrnAttendance> existingAttendance =
                attendanceRepository
                    .findByUserIdAndWorkingDay(
                            employeeId,
                            workingDay
                    );


        TbTrnAttendance attendance;


        // 既存データがある場合
        if (existingAttendance.isPresent()) {

            attendance =
                    existingAttendance.get();

        } else {

            // 既存データがない場合
            attendance =
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
                    workingDay
            );
        }


        attendance.setWorkType(
                workType
        );


        // 公休の場合
        if (workType
                == TbTrnAttendance.WORK_TYPE_HOLIDAY) {

            attendance.setWorkingStartTime(
                    null
            );

            attendance.setWorkingEndTime(
                    null
            );

        } else {

            // 出勤日の場合
            attendance.setWorkingStartTime(
                    workingStartTime
            );

            attendance.setWorkingEndTime(
                    workingEndTime
            );
        }


        // 更新または登録
        if (existingAttendance.isPresent()) {

            attendanceRepository.update(
                    attendance
            );

        } else {

            attendanceRepository.insert(
                    attendance
            );
        }
    }
}