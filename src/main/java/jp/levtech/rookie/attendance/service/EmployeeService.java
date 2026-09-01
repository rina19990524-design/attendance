package jp.levtech.rookie.attendance.service;

import java.security.SecureRandom;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.levtech.rookie.attendance.dto.EmployeePasswordResetResult;
import jp.levtech.rookie.attendance.dto.EmployeeRegistrationResult;
import jp.levtech.rookie.attendance.model.TbMstEmployee;
import jp.levtech.rookie.attendance.repository.TestRepository;

@Service
public class EmployeeService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private static final String UPPER_CASE =
            "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final String LOWER_CASE =
            "abcdefghijkmnopqrstuvwxyz";

    private static final String NUMBERS =
            "23456789";

    private static final String SYMBOLS =
            "!@#$%";


    private final TestRepository testRepository;

    private final PasswordEncoder passwordEncoder;


    public EmployeeService(
            TestRepository testRepository,
            PasswordEncoder passwordEncoder) {

        this.testRepository = testRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * 社員一覧を取得する
     */
    @Transactional(readOnly = true)
    public List<TbMstEmployee> findEmployees(
            String keyword) {

        if (keyword == null
                || keyword.isBlank()) {

            return testRepository.findAll();
        }

        String trimmedKeyword =
                keyword.trim();

        return testRepository
                .findByEmployeeIdOrEmployeeName(
                        trimmedKeyword
                );
    }


    /**
     * 管理者画面から社員または管理者を登録する
     */
    @Transactional
    public EmployeeRegistrationResult registerEmployee(
            String employeeName,
            int departmentId,
            String employeeEmail,
            LocalDate hireDate,
            int employeeStatusId,
            int employeeType) {

        validateEmployeeInput(
                employeeName,
                departmentId,
                employeeEmail,
                hireDate,
                employeeStatusId,
                employeeType
        );

        long employeeNumber =
                testRepository.nextEmployeeNumber();

        String employeeId =
                String.format(
                        "E%04d",
                        employeeNumber
                );

        String initialPassword =
                generateInitialPassword();

        String encodedPassword =
                passwordEncoder.encode(
                        initialPassword
                );

        TbMstEmployee employee =
                new TbMstEmployee(
                        employeeId,
                        employeeName.trim(),
                        departmentId,
                        employeeEmail.trim(),
                        encodedPassword,
                        Date.valueOf(hireDate),
                        employeeStatusId,
                        employeeType
                );

        testRepository.insert(employee);

        return new EmployeeRegistrationResult(
                employeeId,
                initialPassword
        );
    }


    /**
     * 社員情報を更新する
     */
    @Transactional
    public void updateEmployee(
            String employeeId,
            String employeeName,
            int departmentId,
            String employeeEmail,
            LocalDate hireDate,
            int employeeStatusId,
            int employeeType) {

        validateEmployeeInput(
                employeeName,
                departmentId,
                employeeEmail,
                hireDate,
                employeeStatusId,
                employeeType
        );

        TbMstEmployee currentEmployee =
                testRepository
                    .findByEmployeeId(employeeId)
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "更新する社員が見つかりません"
                        )
                    );

        TbMstEmployee updatedEmployee =
                new TbMstEmployee(
                        currentEmployee.getEmployeeId(),
                        employeeName.trim(),
                        departmentId,
                        employeeEmail.trim(),
                        currentEmployee.getEmployeePassword(),
                        Date.valueOf(hireDate),
                        employeeStatusId,
                        employeeType
                );

        int updatedCount =
                testRepository.update(
                        updatedEmployee
                );

        if (updatedCount != 1) {

            throw new IllegalStateException(
                    "社員情報を更新できませんでした"
            );
        }
    }


    /**
     * パスワードを再設定する
     *
     * @return 社員IDと新しい仮パスワード
     */
    @Transactional
    public EmployeePasswordResetResult resetPassword(
            String employeeId) {

        // 社員が存在するか確認
        testRepository
            .findByEmployeeId(employeeId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "パスワードを再設定する社員が"
                    + "見つかりません"
                )
            );

        // 新しい仮パスワードを作成
        String temporaryPassword =
                generateInitialPassword();

        // BCryptでハッシュ化
        String encodedPassword =
                passwordEncoder.encode(
                        temporaryPassword
                );

        // 暗号化したパスワードをDBへ保存
        int updatedCount =
                testRepository.updatePassword(
                        employeeId,
                        encodedPassword
                );

        if (updatedCount != 1) {

            throw new IllegalStateException(
                    "パスワードを再設定できませんでした"
            );
        }

        /*
         * 画面へ返すのは平文の仮パスワード。
         * DBには暗号化済みパスワードだけが保存される。
         */
        return new EmployeePasswordResetResult(
                employeeId,
                temporaryPassword
        );
    }


    /**
     * 最初の管理者を登録する
     */
    @Transactional
    public void register(
            String employeeId,
            String employeeName,
            int departmentId,
            String employeeEmail,
            String employeePassword,
            java.util.Date hireDate,
            int employeeType) {

        if (testRepository
                .findByEmployeeId(employeeId)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "この社員IDはすでに登録されています"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        employeePassword
                );

        TbMstEmployee employee =
                new TbMstEmployee(
                        employeeId,
                        employeeName,
                        departmentId,
                        employeeEmail,
                        encodedPassword,
                        hireDate,
                        1,
                        employeeType
                );

        testRepository.insert(employee);
    }


    /**
     * 入力内容を確認する
     */
    private void validateEmployeeInput(
            String employeeName,
            int departmentId,
            String employeeEmail,
            LocalDate hireDate,
            int employeeStatusId,
            int employeeType) {

        if (employeeName == null
                || employeeName.isBlank()) {

            throw new IllegalArgumentException(
                    "社員名を入力してください"
            );
        }

        if (employeeName.length() > 100) {

            throw new IllegalArgumentException(
                    "社員名は100文字以内で入力してください"
            );
        }

        if (departmentId < 1
                || departmentId > 3) {

            throw new IllegalArgumentException(
                    "部署を選択してください"
            );
        }

        if (employeeEmail == null
                || employeeEmail.isBlank()) {

            throw new IllegalArgumentException(
                    "メールアドレスを入力してください"
            );
        }

        if (employeeEmail.length() > 100) {

            throw new IllegalArgumentException(
                    "メールアドレスは100文字以内で"
                    + "入力してください"
            );
        }

        if (hireDate == null) {

            throw new IllegalArgumentException(
                    "入社日を入力してください"
            );
        }

        if (employeeStatusId < 1
                || employeeStatusId > 4) {

            throw new IllegalArgumentException(
                    "在籍状況を選択してください"
            );
        }

        if (employeeType != 1
                && employeeType != 2) {

            throw new IllegalArgumentException(
                    "社員タイプを選択してください"
            );
        }
    }


    /**
     * 12文字の初期パスワードを作成する
     */
    private String generateInitialPassword() {

        List<Character> characters =
                new ArrayList<>();

        characters.add(
                randomCharacter(UPPER_CASE)
        );

        characters.add(
                randomCharacter(LOWER_CASE)
        );

        characters.add(
                randomCharacter(NUMBERS)
        );

        characters.add(
                randomCharacter(SYMBOLS)
        );

        String allCharacters =
                UPPER_CASE
                + LOWER_CASE
                + NUMBERS
                + SYMBOLS;

        while (characters.size() < 12) {

            characters.add(
                    randomCharacter(allCharacters)
            );
        }

        Collections.shuffle(
                characters,
                SECURE_RANDOM
        );

        StringBuilder password =
                new StringBuilder();

        for (Character character : characters) {

            password.append(character);
        }

        return password.toString();
    }


    /**
     * 指定した文字列から1文字をランダムに取得する
     */
    private Character randomCharacter(
            String characters) {

        int index =
                SECURE_RANDOM.nextInt(
                        characters.length()
                );

        return characters.charAt(index);
    }
}