package com.amcsoftware.student.service;

import com.amcsoftware.exceptions.uncheckedExceptions.DuplicateRequest;
import com.amcsoftware.exceptions.uncheckedExceptions.RequestValidationException;
import com.amcsoftware.exceptions.uncheckedExceptions.ResourceNotFound;
import com.amcsoftware.student.dao.StudentDao;
import com.amcsoftware.student.model.Student;
import com.amcsoftware.student.model.records.StudentRegistrationRequest;
import com.amcsoftware.student.model.records.StudentUpdateRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentServices {
    private final StudentDao studentDao;
    private final PasswordEncoder passwordEncoder;
    private final StudentDTOMapper studentDTOMapper;

    public StudentServices(@Qualifier("jdbc") StudentDao studentDao, PasswordEncoder passwordEncoder, StudentDTOMapper studentDTOMapper) {
        this.studentDao = studentDao;
        this.passwordEncoder = passwordEncoder;
        this.studentDTOMapper = studentDTOMapper;
    }

    public List<StudentDTO> getStudents() {
        return studentDao.selectAllStudents()
                .stream()
                .map(studentDTOMapper).collect(Collectors.toList());
    }

    public StudentDTO getStudentById(UUID id) {
        return studentDao.findStudentById(id).map(studentDTOMapper)
                .orElseThrow(() -> new ResourceNotFound("student not found with id %s".formatted(id)));
    }

    public void addStudent(StudentRegistrationRequest studentRegistrationRequest) {
        checkEmailAvailability(studentRegistrationRequest.email());
        Student newStudent = new Student(studentRegistrationRequest.firstName(),
                studentRegistrationRequest.lastName(),studentRegistrationRequest.email(), passwordEncoder.encode(studentRegistrationRequest.password()),
                studentRegistrationRequest.phoneNumber(), studentRegistrationRequest.age(), studentRegistrationRequest.gender());
        studentDao.insertStudent(newStudent);
    }

    public void deleteStudent(UUID id) {
        if(!studentDao.existById(id)) {
            throw new ResourceNotFound("%s doesn't exist".formatted(id));
        }
        studentDao.deleteStudentWithId(id);
    }

    public void updateStudent(UUID id, StudentUpdateRequest request) {
        Student student = studentDao.findStudentById(id)
                .orElseThrow(() -> new ResourceNotFound("student not found with id %s".formatted(id)));

        boolean isChanged = false;

        if(!request.firstName().equals("") && !student.getFirstName().equals(request.firstName())) {
            student.setFirstName(request.firstName());
            isChanged = true;
        }

        if(!request.lastName().equals("") && !student.getLastName().equals(request.lastName())) {
            student.setLastName(request.lastName());
            isChanged = true;
        }

        if(!request.email().equals("") && !student.getEmail().equals(request.email())) {
            checkEmailAvailability(request.email());
            student.setEmail(request.email());
            isChanged = true;
        }

        if(!request.phoneNumber().equals("") && !student.getPhoneNumber().equals(request.phoneNumber())) {
            student.setPhoneNumber(request.phoneNumber());
            isChanged = true;
        }

//        if(student.getAge() != request.age()) {
//            throw new RequestValidationException("no changes made - unable to change age");
//        }

        if(!isChanged) {
            throw new RequestValidationException("no changes made");
        }
        studentDao.updateStudentWithId(student);
    }

    private void checkEmailAvailability(String email) {
        if(studentDao.isEmail(email)) {
            throw new DuplicateRequest("%s already taken".formatted(email));
        }
    }
}
