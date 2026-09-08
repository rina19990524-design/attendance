package jp.levtech.rookie.attendance.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.levtech.rookie.attendance.dto.AdminAttendanceRequestView;
import jp.levtech.rookie.attendance.repository.AdminAttendanceRequestRepository;

@Service
public class AdminAttendanceRequestService {

	/**
	 * 申請中
	 */
	private static final int REQUESTING = 1;

	/**
	 * 承認済み
	 */
	private static final int APPROVED = 2;


    /**
     * 時刻の表示形式
     */
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");


    private final AdminAttendanceRequestRepository
            adminAttendanceRequestRepository;


    public AdminAttendanceRequestService(
            AdminAttendanceRequestRepository
                    adminAttendanceRequestRepository) {

        this.adminAttendanceRequestRepository =
                adminAttendanceRequestRepository;
    }


    /**
     * 承認待ちの勤怠申請を取得する
     */
    @Transactional(readOnly = true)
    public List<AdminAttendanceRequestView>
            findPendingRequests() {

        List<AdminAttendanceRequestView> requests =
                adminAttendanceRequestRepository
                    .findByRequestFlag(
                        REQUESTING
                    );

        for (AdminAttendanceRequestView request
                : requests) {

            setDisplayValues(request);
        }

        return requests;
    }


    /**
     * 未承認の勤怠申請件数を取得する
     */
    @Transactional(readOnly = true)
    public int countPendingRequests() {

        return adminAttendanceRequestRepository
                .countByRequestFlag(
                        REQUESTING
                );
    }


    /**
     * 勤怠申請を1件承認する
     */
    @Transactional
    public void approveRequest(
            String requestId) {

        if (requestId == null
                || requestId.isBlank()) {

            throw new IllegalArgumentException(
                    "勤怠申請IDが指定されていません"
            );
        }

        /*
         * 申請された時刻を元の勤怠実績へ反映する
         */
        int attendanceUpdatedCount =
                adminAttendanceRequestRepository
                    .updateAttendanceFromRequest(
                        requestId,
                        REQUESTING
                    );

        if (attendanceUpdatedCount != 1) {

            throw new IllegalStateException(
                    "申請が見つからないか、"
                    + "すでに承認されています"
            );
        }

        /*
         * 申請フラグを承認済みに変更する
         */
        int requestUpdatedCount =
                adminAttendanceRequestRepository
                    .updateRequestFlag(
                        requestId,
                        REQUESTING,
                        APPROVED
                    );

        if (requestUpdatedCount != 1) {

            throw new IllegalStateException(
                    "申請状態を更新できませんでした"
            );
        }
    }


    /**
     * 選択した勤怠申請を一括承認する
     */
    @Transactional
    public int approveRequests(
            List<String> requestIds) {

        if (requestIds == null
                || requestIds.isEmpty()) {

            throw new IllegalArgumentException(
                    "承認する申請を選択してください"
            );
        }

        int approvedCount = 0;

        for (String requestId : requestIds) {

            approveRequest(requestId);

            approvedCount++;
        }

        return approvedCount;
    }


    /**
     * HTMLに表示する値を設定する
     */
    private void setDisplayValues(
            AdminAttendanceRequestView request) {

        String beforeTime =
                formatTimeRange(
                        request.getBeforeStartTime(),
                        request.getBeforeEndTime()
                );

        request.setBeforeTime(beforeTime);


        String afterTime =
                formatTimeRange(
                        request.getAfterStartTime(),
                        request.getAfterEndTime()
                );

        request.setAfterTime(afterTime);


        String requestTypeName =
                getRequestTypeName(
                        request.getRequestType()
                );

        request.setRequestTypeName(
                requestTypeName
        );
    }


    /**
     * 開始時刻と終了時刻を表示用文字列へ変換する
     */
    private String formatTimeRange(
            LocalTime startTime,
            LocalTime endTime) {

        String formattedStartTime =
                formatTime(startTime);

        String formattedEndTime =
                formatTime(endTime);

        return formattedStartTime
                + "～"
                + formattedEndTime;
    }


    /**
     * 時刻をHH:mm形式へ変換する
     */
    private String formatTime(
            LocalTime time) {

        if (time == null) {

            return "--:--";
        }

        return time.format(
                TIME_FORMATTER
        );
    }


    /**
     * 申請種別番号を表示名へ変換する
     */
    private String getRequestTypeName(
            int requestType) {

        return switch (requestType) {

            case 1 ->
                "勤怠修正";

            case 2 ->
                "打刻漏れ";

            default ->
                "";
        };
    }
}