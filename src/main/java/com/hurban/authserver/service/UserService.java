package com.hurban.authserver.service;

import com.hurban.authserver.dto.AuthRequest;
import com.hurban.authserver.entity.User;
import com.hurban.authserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repo;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String authenticate(AuthRequest request) {
        User user = repo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BadCredentialsException("Invalid credentials");

        return jwtService.generateToken(user.getUsername());
    }

    public void registerUser(AuthRequest request) {
        if (repo.findByUsername(request.getUsername()).isPresent())
            throw new RuntimeException("User already exists");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        repo.save(user);
    }

    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), new ArrayList<>());
    }
}

