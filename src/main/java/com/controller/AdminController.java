package com.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entity.UserEntity;
import com.repository.UserRepository;

@RestController
@RequestMapping("admin")
public class AdminController {

	@Autowired
	UserRepository userRepo;
	
	@DeleteMapping("removemember/{memberId}")
	public ResponseEntity<?> deleteUserByEmail(@PathVariable String memberId) {
		Map<String, Object> msg = new HashMap<>();
		Optional<UserEntity> optUser = userRepo.findById(memberId);
		if(optUser.isEmpty()) {
			msg.put("message", "User is not exists.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
		}
		
		try {
			userRepo.deleteById(memberId);
			msg.put("message", "User Deleted Successfully.");
			return ResponseEntity.status(HttpStatus.OK).body(msg);
		} catch(Exception e) {
			e.printStackTrace();
			msg.put("message", "Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}
	}
}
