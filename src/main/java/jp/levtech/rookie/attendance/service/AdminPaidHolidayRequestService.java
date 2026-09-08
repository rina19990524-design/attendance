package jp.levtech.rookie.attendance.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.levtech.rookie.attendance.dto.AdminPaidHolidayRequestView;
import jp.levtech.rookie.attendance.repository.PaidHolidayRequestRepository;

@Service
public class AdminPaidHolidayRequestService {

    /**
     * 申請中
     */
    private static final int REQUESTING = 1;


    /**
     * 承認済み
     */
    private static final int APPROVED = 2;


    private final PaidHolidayRequestRepository
            paidHolidayRequestRepository;


    public AdminPaidHolidayRequestService(
            PaidHolidayRequestRepository
                    paidHolidayRequestRepository) {

        this.paidHolidayRequestRepository =
                paidHolidayRequestRepository;
    }


    /**
     * 承認待ちの有給申請を取得する
     */
    @Transactional(readOnly = true)
    public List<AdminPaidHolidayRequestView>
            findPendingRequests() {

        // DBから申請中の有給申請を取得
        List<AdminPaidHolidayRequestView> requests =
                paidHolidayRequestRepository
                    .findByRequestFlag(
                        REQUESTING
                    );

        // 休暇区分名を設定
        for (AdminPaidHolidayRequestView request
                : requests) {

            String holidayTypeName =
                    getHolidayTypeName(
                            request.getHolidayType()
                    );

            request.setHolidayTypeName(
                    holidayTypeName
            );
        }

        return requests;
    }


    /**
     * 有給申請を1件承認する
     */
    @Transactional
    public void approveRequest(
            String requestId) {

        if (requestId == null
                || requestId.isBlank()) {

            throw new IllegalArgumentException(
                    "有給申請IDが指定されていません"
            );
        }

        int updatedCount =
                paidHolidayRequestRepository
                    .updateRequestFlag(
                        requestId,
                        REQUESTING,
                        APPROVED
                    );

        /*
         * 更新件数が0件の場合は、
         * 申請が存在しないか、すでに承認されている。
         */
        if (updatedCount != 1) {

            throw new IllegalStateException(
                    "有給申請が見つからないか、"
                    + "すでに承認されています"
            );
        }
    }


    /**
     * 選択された有給申請を一括承認する
     *
     * @return 承認した件数
     */
    @Transactional
    public int approveRequests(
            List<String> requestIds) {

        if (requestIds == null
                || requestIds.isEmpty()) {

            throw new IllegalArgumentException(
                    "承認する有給申請を選択してください"
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
     * 休暇区分番号を表示名へ変換する
     */
    private String getHolidayTypeName(
            int holidayType) {

        return switch (holidayType) {

            case 1 ->
                "1日有給";

            case 2 ->
                "午前半休";

            case 3 ->
                "午後半休";

            default ->
                "";
        };
    }
}