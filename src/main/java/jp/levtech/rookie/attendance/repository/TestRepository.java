package jp.levtech.rookie.attendance.repository;

import java.util.List;
import java.util.Optional;

import jp.levtech.rookie.attendance.model.TbMstEmployee;

public interface TestRepository {

    List<TbMstEmployee> findAll();


    Optional<TbMstEmployee> findByEmployeeId(
            String employeeId
    );


    List<TbMstEmployee> findByEmployeeIdOrEmployeeName(
            String keyword
    );


    long nextEmployeeNumber();


    int insert(
            TbMstEmployee employee
    );


    int update(
            TbMstEmployee employee
    );


    /**
     * パスワードを更新する
     */
    int updatePassword(
            String employeeId,
            String encodedPassword
    );
}