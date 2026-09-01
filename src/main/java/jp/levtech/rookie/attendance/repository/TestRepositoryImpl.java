package jp.levtech.rookie.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.mapper.TestMapper;
import jp.levtech.rookie.attendance.model.TbMstEmployee;

@Repository
public class TestRepositoryImpl
        implements TestRepository {

    private final TestMapper testMapper;


    public TestRepositoryImpl(
            TestMapper testMapper) {

        this.testMapper = testMapper;
    }


    @Override
    public List<TbMstEmployee> findAll() {

        return testMapper.findAll();
    }


    @Override
    public Optional<TbMstEmployee> findByEmployeeId(
            String employeeId) {

        return testMapper.findByEmployeeId(
                employeeId
        );
    }


    @Override
    public List<TbMstEmployee>
            findByEmployeeIdOrEmployeeName(
                    String keyword) {

        return testMapper
                .findByEmployeeIdOrEmployeeName(
                        keyword
                );
    }


    @Override
    public long nextEmployeeNumber() {

        return testMapper.nextEmployeeNumber();
    }


    @Override
    public int insert(
            TbMstEmployee employee) {

        return testMapper.insert(employee);
    }


    @Override
    public int update(
            TbMstEmployee employee) {

        return testMapper.update(employee);
    }


    @Override
    public int updatePassword(
            String employeeId,
            String encodedPassword) {

        return testMapper.updatePassword(
                employeeId,
                encodedPassword
        );
    }
}