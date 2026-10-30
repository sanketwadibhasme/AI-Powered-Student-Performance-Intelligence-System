-- Demo seed for V0: run with
--   mysql -uroot -p<your-password> < ml/seed_demo.sql
USE student_performance_db;

-- ============ Demo users (BCrypt hashes) ============
-- admin/admin123
INSERT INTO app_users (username, email, password, role, enabled)
SELECT 'admin', 'admin@spis.edu', '$2b$10$nLv6JVOUfjhir0H62sGwdOR93vhpXDj68vmg.mMKG1v/qITSVeBdi', 'ADMIN', 1
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'admin');

-- faculty1/faculty123
INSERT INTO app_users (username, email, password, role, enabled)
SELECT 'faculty1', 'faculty1@spis.edu', '$2b$10$F.2nH2fQVCBTHkRlwJohBuJQSn6kb6HWthrmsVGEETmgxfExngOb2', 'FACULTY', 1
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'faculty1');

-- student1/student123 linked to student_id 2 (Raj, CS dept, semester 5)
INSERT INTO app_users (username, email, password, role, enabled, student_id)
SELECT 'student1', 'student1@spis.edu', '$2b$10$Wi5at90jL/qybExb2B.6/ul.kcQ4X/lPc8eZ3MDO/H3jlC/yVh1x.', 'STUDENT', 1, 2
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE username = 'student1');

-- ============ Extra demo students (same department) ============
INSERT INTO students (roll_number, first_name, last_name, email, phone, gender, dob, admission_year, current_semester, status, department_id)
SELECT * FROM (
  SELECT 'CS23002' AS roll_number, 'Priya' AS first_name, 'Sharma' AS last_name, 'priya.sharma@spis.edu' AS email, '9876500002' AS phone, 'FEMALE' AS gender, DATE '2005-03-14' AS dob, 2023 AS admission_year, 5 AS current_semester, 'ACTIVE' AS status, 1 AS department_id
  UNION ALL SELECT 'CS23003','Amit','Verma','amit.verma@spis.edu','9876500003','MALE','2005-06-21',2023,5,'ACTIVE',1
  UNION ALL SELECT 'CS23004','Sneha','Patel','sneha.patel@spis.edu','9876500004','FEMALE','2005-01-09',2023,5,'ACTIVE',1
  UNION ALL SELECT 'CS23005','Vikram','Rao','vikram.rao@spis.edu','9876500005','MALE','2004-11-30',2023,5,'ACTIVE',1
  UNION ALL SELECT 'CS23006','Neha','Singh','neha.singh@spis.edu','9876500006','FEMALE','2005-04-02',2023,5,'ACTIVE',1
) AS new_students
WHERE NOT EXISTS (SELECT 1 FROM students WHERE roll_number IN ('CS23002','CS23003','CS23004','CS23005','CS23006'));

-- ============ Exams ============
INSERT INTO exams (exam_name, exam_date, total_marks)
SELECT 'Internal Assessment 1', DATE '2026-07-20', 100 WHERE NOT EXISTS (SELECT 1 FROM exams WHERE exam_name = 'Internal Assessment 1');
INSERT INTO exams (exam_name, exam_date, total_marks)
SELECT 'Internal Assessment 2', DATE '2026-08-25', 100 WHERE NOT EXISTS (SELECT 1 FROM exams WHERE exam_name = 'Internal Assessment 2');

-- ============ Marks: spread of performance across students ============
-- students: 2,4,7,8,9,14,15,16,17,18 | subject: 2 (DBMS) | exams: 3, 4 (Internal Assessments)
INSERT INTO marks (marks_obtained, student_id, subject_id, exam_id)
SELECT * FROM (
  SELECT 78.0 AS marks_obtained, 2 AS student_id, 2 AS subject_id, 3 AS exam_id
  UNION ALL SELECT 82.0, 2, 2, 4
  UNION ALL SELECT 45.0, 4, 2, 3
  UNION ALL SELECT 52.0, 4, 2, 4
  UNION ALL SELECT 65.0, 7, 2, 3
  UNION ALL SELECT 71.0, 7, 2, 4
  UNION ALL SELECT 30.0, 8, 2, 3
  UNION ALL SELECT 38.0, 8, 2, 4
  UNION ALL SELECT 88.0, 9, 2, 3
  UNION ALL SELECT 91.0, 9, 2, 4
  UNION ALL SELECT 55.0, 14, 2, 3
  UNION ALL SELECT 49.0, 14, 2, 4
  UNION ALL SELECT 74.0, 15, 2, 3
  UNION ALL SELECT 79.0, 15, 2, 4
  UNION ALL SELECT 62.0, 16, 2, 3
  UNION ALL SELECT 68.0, 16, 2, 4
  UNION ALL SELECT 41.0, 17, 2, 3
  UNION ALL SELECT 47.0, 17, 2, 4
  UNION ALL SELECT 35.0, 18, 2, 3
  UNION ALL SELECT 29.0, 18, 2, 4
) AS new_marks
WHERE NOT EXISTS (SELECT 1 FROM marks m WHERE m.student_id = new_marks.student_id AND m.exam_id = new_marks.exam_id AND m.subject_id = new_marks.subject_id);

-- ============ Attendance over recent weeks ============
-- students: 2,4,7,8,9,14,15,16,17,18 | subject: 2 (DBMS) | dates: 2026-09-01..05
INSERT INTO attendance (attendance_date, status, student_id, subject_id)
SELECT * FROM (
  SELECT DATE '2026-09-01' AS attendance_date, 'PRESENT' AS status, 2 AS student_id, 2 AS subject_id
  UNION ALL SELECT DATE '2026-09-02','PRESENT',2,2
  UNION ALL SELECT DATE '2026-09-03','ABSENT',2,2
  UNION ALL SELECT DATE '2026-09-04','PRESENT',2,2
  UNION ALL SELECT DATE '2026-09-05','PRESENT',2,2
  UNION ALL SELECT DATE '2026-09-01','PRESENT',4,2
  UNION ALL SELECT DATE '2026-09-02','ABSENT',4,2
  UNION ALL SELECT DATE '2026-09-03','ABSENT',4,2
  UNION ALL SELECT DATE '2026-09-04','PRESENT',4,2
  UNION ALL SELECT DATE '2026-09-05','ABSENT',4,2
  UNION ALL SELECT DATE '2026-09-01','PRESENT',7,2
  UNION ALL SELECT DATE '2026-09-02','PRESENT',7,2
  UNION ALL SELECT DATE '2026-09-03','PRESENT',7,2
  UNION ALL SELECT DATE '2026-09-04','ABSENT',7,2
  UNION ALL SELECT DATE '2026-09-05','PRESENT',7,2
  UNION ALL SELECT DATE '2026-09-01','ABSENT',8,2
  UNION ALL SELECT DATE '2026-09-02','ABSENT',8,2
  UNION ALL SELECT DATE '2026-09-03','PRESENT',8,2
  UNION ALL SELECT DATE '2026-09-04','ABSENT',8,2
  UNION ALL SELECT DATE '2026-09-05','ABSENT',8,2
  UNION ALL SELECT DATE '2026-09-01','PRESENT',9,2
  UNION ALL SELECT DATE '2026-09-02','PRESENT',9,2
  UNION ALL SELECT DATE '2026-09-03','PRESENT',9,2
  UNION ALL SELECT DATE '2026-09-04','PRESENT',9,2
  UNION ALL SELECT DATE '2026-09-05','PRESENT',9,2
  UNION ALL SELECT DATE '2026-09-01','PRESENT',14,2
  UNION ALL SELECT DATE '2026-09-02','ABSENT',14,2
  UNION ALL SELECT DATE '2026-09-03','PRESENT',14,2
  UNION ALL SELECT DATE '2026-09-04','PRESENT',14,2
  UNION ALL SELECT DATE '2026-09-05','ABSENT',14,2
  UNION ALL SELECT DATE '2026-09-01','PRESENT',15,2
  UNION ALL SELECT DATE '2026-09-02','PRESENT',15,2
  UNION ALL SELECT DATE '2026-09-03','ABSENT',15,2
  UNION ALL SELECT DATE '2026-09-04','PRESENT',15,2
  UNION ALL SELECT DATE '2026-09-05','PRESENT',15,2
  UNION ALL SELECT DATE '2026-09-01','PRESENT',16,2
  UNION ALL SELECT DATE '2026-09-02','PRESENT',16,2
  UNION ALL SELECT DATE '2026-09-03','PRESENT',16,2
  UNION ALL SELECT DATE '2026-09-04','PRESENT',16,2
  UNION ALL SELECT DATE '2026-09-05','PRESENT',16,2
  UNION ALL SELECT DATE '2026-09-01','ABSENT',17,2
  UNION ALL SELECT DATE '2026-09-02','PRESENT',17,2
  UNION ALL SELECT DATE '2026-09-03','ABSENT',17,2
  UNION ALL SELECT DATE '2026-09-04','PRESENT',17,2
  UNION ALL SELECT DATE '2026-09-05','ABSENT',17,2
  UNION ALL SELECT DATE '2026-09-01','ABSENT',18,2
  UNION ALL SELECT DATE '2026-09-02','ABSENT',18,2
  UNION ALL SELECT DATE '2026-09-03','ABSENT',18,2
  UNION ALL SELECT DATE '2026-09-04','PRESENT',18,2
  UNION ALL SELECT DATE '2026-09-05','ABSENT',18,2
) AS new_att
WHERE NOT EXISTS (SELECT 1 FROM attendance a WHERE a.student_id = new_att.student_id AND a.attendance_date = new_att.attendance_date AND a.subject_id = new_att.subject_id);
