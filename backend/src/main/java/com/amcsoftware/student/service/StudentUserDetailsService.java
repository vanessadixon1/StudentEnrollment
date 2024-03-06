package com.amcsoftware.student.service;

import com.amcsoftware.student.dao.StudentDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StudentUserDetailsService implements UserDetailsService {
    private final StudentDao studentDao;

    public StudentUserDetailsService(@Qualifier("jpa") StudentDao studentDao) {
        this.studentDao = studentDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        return studentDao.selectUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Username " + username + " not found"));
    }
}
