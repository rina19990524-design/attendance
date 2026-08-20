package jp.levtech.rookie.attendance.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import jp.levtech.rookie.attendance.model.TbMstEmployee;

@Mapper
public interface TestMapper {
	List<TbMstEmployee> findAll();
	
	Optional<TbMstEmployee> findByEmployeeId(String employeeId);

}
