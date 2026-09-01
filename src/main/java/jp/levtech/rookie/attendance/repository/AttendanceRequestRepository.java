package jp.levtech.rookie.attendance.repository;

import jp.levtech.rookie.attendance.model.TbTrnAttendanceRequest;

public interface AttendanceRequestRepository {

    void insert(TbTrnAttendanceRequest attendanceRequest);

}