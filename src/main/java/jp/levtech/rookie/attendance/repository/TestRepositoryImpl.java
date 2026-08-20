package jp.levtech.rookie.attendance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jp.levtech.rookie.attendance.mapper.TestMapper;
import jp.levtech.rookie.attendance.model.TbMstEmployee;

@Repository
public class TestRepositoryImpl implements TestRepository {
	/**
	 * タスクのマッパー
	 */
	private final TestMapper testMapper;

	/**
	 * タスクをデータベースで管理するリポジトリのコンストラクタ
	 *
	 * @param taskMapper タスクのマッパー
	 */
	public TestRepositoryImpl(TestMapper testMapper) {
		// タスクのマッパーを初期化する。
		this.testMapper = testMapper;
	}
	
	@Override
	public List<TbMstEmployee> findAll() {
		return testMapper.findAll();
	}
	
	@Override
	public Optional<TbMstEmployee> findByEmployeeId(String employeeId) {
		return testMapper.findByEmployeeId(employeeId);
	}

}
