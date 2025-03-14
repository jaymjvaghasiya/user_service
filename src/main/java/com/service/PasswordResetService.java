package com.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entity.UserEntity;
import com.repository.UserRepository;
import com.util.TokenGenerator;

@Service
public class PasswordResetService {

	@Autowired
	private UserRepository userRepo;

	public String createResetToken(String email) {
        Optional<UserEntity> userOpt = userRepo.findUserByEmail(email);
        
        if (userOpt.isEmpty()) {
            return null; 
        }
        
        UserEntity user = userOpt.get();
        String token;
		try {
			token = TokenGenerator.generateToken();
			user.setReset_token(token);
			user.setReset_token_expiry(LocalDateTime.now().plusMinutes(15));
			userRepo.save(user);
			
			return token;
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
        
    }
}
