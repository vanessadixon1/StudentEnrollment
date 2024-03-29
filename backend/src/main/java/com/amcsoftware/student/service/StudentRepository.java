package com.amcsoftware.student.service;

import com.amcsoftware.student.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    boolean existsStudentByEmail(String email);
    Optional<Student> findStudentByEmail(String email);
}
