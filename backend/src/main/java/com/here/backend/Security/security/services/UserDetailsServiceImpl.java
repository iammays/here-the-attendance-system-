
package com.here.backend.Security.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.here.backend.Teacher.TeacherEntity;
import com.here.backend.Teacher.TeacherRepository;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private TeacherRepository teacherRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Optional<TeacherEntity> teacher = teacherRepository.findByName(name);
        
        if (teacher.isEmpty()) {
            throw new UsernameNotFoundException("User Not Found with username: " + name);
        }
        
        return UserDetailsImpl.build(teacher.get());
    }

    public UserDetails loadUserByName(String name) throws UsernameNotFoundException {
        Optional<TeacherEntity> teacher = teacherRepository.findByName(name);
        
        if (teacher.isEmpty()) {
            throw new UsernameNotFoundException("User Not Found with username: " + name);
        }
        
        return UserDetailsImpl.build(teacher.get());
    }
}