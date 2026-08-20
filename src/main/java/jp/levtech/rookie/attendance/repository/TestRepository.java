package jp.levtech.rookie.attendance.repository;

import java.util.List;
import java.util.Optional;

import jp.levtech.rookie.attendance.model.TbMstEmployee;

public interface TestRepository {
	
	List<TbMstEmployee> findAll();
	
	Optional<TbMstEmployee> findByEmployeeId(String employeeId);
}
