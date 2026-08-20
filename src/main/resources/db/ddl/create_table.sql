DROP TABLE IF EXISTS tb_trn_attendance_request;
DROP TABLE IF EXISTS tb_trn_paid_holiday_request;
DROP TABLE IF EXISTS tb_trn_attendance;
DROP TABLE IF EXISTS tb_mst_employee;

CREATE TABLE tb_mst_employee (
    employee_id VARCHAR(10) PRIMARY KEY,
    employee_name VARCHAR(100) NOT NULL,
    department_id INT NOT NULL,
    employee_email VARCHAR(100) UNIQUE NOT NULL,
    employee_password VARCHAR(100) NOT NULL,
    hire_date DATE NOT NULL,
    employee_status_id INT NOT NULL,
    employee_type INT NOT NULL
);

CREATE TABLE tb_trn_attendance (
    attendance_id VARCHAR(10) PRIMARY KEY,
    employee_id VARCHAR(10) NOT NULL,
    working_day DATE NOT NULL,
    working_start_time TIME,
    working_end_time TIME,
    actual_working_start_time TIME,
    actual_working_end_time TIME,

    FOREIGN KEY (employee_id)
        REFERENCES tb_mst_employee(employee_id)
);

CREATE TABLE tb_trn_attendance_request (
    attendance_request_id VARCHAR(10) PRIMARY KEY,
    attendance_id VARCHAR(10) NOT NULL,
    requested_working_start_time TIME,
    requested_working_end_time TIME,
    request_reason VARCHAR(500),
    request_flag INT NOT NULL,
    request_type INT NOT NULL,

    FOREIGN KEY (attendance_id)
        REFERENCES tb_trn_attendance(attendance_id)
);

CREATE TABLE tb_trn_paid_holiday_request (
    paid_holiday_request_id VARCHAR(10) PRIMARY KEY,
    holiday_type INT NOT NULL,
    holiday_date DATE NOT NULL,
    employee_id VARCHAR(10) NOT NULL,
    request_reason VARCHAR(500),
    request_flag INT NOT NULL,

    FOREIGN KEY (employee_id)
        REFERENCES tb_mst_employee(employee_id)
);