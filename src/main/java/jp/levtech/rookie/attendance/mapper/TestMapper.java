package jp.levtech.rookie.attendance.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jp.levtech.rookie.attendance.model.TbMstEmployee;

@Mapper
public interface TestMapper {

    List<TbMstEmployee> findAll();


    Optional<TbMstEmployee> findByEmployeeId(
            @Param("employeeId")
            String employeeId
    );


    List<TbMstEmployee> findByEmployeeIdOrEmployeeName(
            @Param("keyword")
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
            @Param("employeeId")
            String employeeId,

            @Param("encodedPassword")
            String encodedPassword
    );
}