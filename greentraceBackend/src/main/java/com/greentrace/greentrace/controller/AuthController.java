package com.greentrace.greentrace.controller;

import com.greentrace.greentrace.model.User;
import com.greentrace.greentrace.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public User signup(@RequestBody User user){
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody User user){

        Optional<User> existing = userRepository.findByEmail(user.getEmail());

        if(existing.isPresent() && existing.get().getPassword().equals(user.getPassword())){
            return existing.get();
        }

        throw new RuntimeException("Invalid credentials");
    }
}