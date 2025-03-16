package com.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.repository.UserRepository;
import com.service.MailService;
import com.service.PasswordResetService;
import com.entity.UserEntity;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("member")
public class UserController {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private PasswordResetService resetPasswordService;
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	@PostMapping(value = "newmember", consumes = "multipart/form-data")
	public ResponseEntity<?> createNewUser(@Validated @RequestBody UserEntity userEntity, @RequestPart MultipartFile profilePic, HttpSession session) {
		Map<String, Object> msg = new HashMap<>();
		try	 {
			StringBuffer sb = new StringBuffer();
			if(!profilePic.isEmpty()) {				
				if(!profilePic.getContentType().startsWith("image")) {
					msg.put("message", "Only image is accepted.");
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
				}
				
				String email = userEntity.getEmail();
				String firstname = userEntity.getFirstname();				
				String filepath = sb.append(uploadDir).append(email).toString();
				sb.setLength(0);
				sb.append(firstname).append("_").append(email.substring(0, email.lastIndexOf("."))).append(".").append(getExtension(profilePic.getOriginalFilename()));
				
				File userFile = new File(filepath, sb.toString()); 
				
				try {
					FileUtils.writeByteArrayToFile(userFile, profilePic.getBytes());
				} catch(IOException e) {
					msg.put("message", "Error In file uploading.");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
				}
				userEntity.setProfile_pic_path("user_profile_pic/"+email+"/"+sb.toString());
				sb.setLength(0);
			}
			
			String encPass = passwordEncoder.encode(userEntity.getPassword());
			userEntity.setPassword(encPass);
			
			userRepo.save(userEntity);
			
			sb.append(userEntity.getFirstname())
			  .append(" ")
			  .append(userEntity.getLastname());
			
			String mailSubject = "Welcome to the CodeWarriors";
			mailService.sendEmail(userEntity.getEmail(), mailSubject, mailService.signUpMailBody(sb.toString()));

			sb.setLength(0);

			msg.put("new_user", userEntity);
			msg.put("message", "User created Successfully");
			return ResponseEntity.status(HttpStatus.CREATED).body(msg);
			
		} catch(DataIntegrityViolationException e) { 
			e.printStackTrace();
			msg.put("message", "Data Violated");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		} catch(Exception e) {
			e.printStackTrace();
			msg.put("message", "Some error occured");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}
	}
	
	@PostMapping("verifymember")
	public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
		Map<String, Object> msg = new HashMap<>();
		if(email.isBlank() || password.isBlank()) {
			msg.put("message", "Please Fill required fields.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
		}
		
		Optional<UserEntity> optUser = userRepo.findUserByEmail(email);
		if(optUser.isEmpty()) {
			msg.put("message", "User not exitst");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
		} else {			
			UserEntity dbuser = optUser.get();
			boolean flag = passwordEncoder.matches(password, dbuser.getPassword());
			if(flag) {
				msg.put("message", "User exists.");
				msg.put("user", dbuser);
				model.addAttribute("user", dbuser);
				return ResponseEntity.status(HttpStatus.OK).body(msg);
			} else {
				msg.put("message", "Email or Password is incorrect, please check !!!");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
			}
		}
	}
	
	
	@GetMapping("editmember")
	public ResponseEntity<?> editUser(HttpSession session, Model model) {
		Map<String, Object> msg = new HashMap<>();
		String email = session.getAttribute("useremail").toString();
		if(email == null) {
			msg.put("message", "Email not found in session");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
		}
		
		try {			
			Optional<UserEntity> optUser = userRepo.findUserByEmail(email);
			if(optUser.isEmpty()) {
				msg.put("message", "User not exitst");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
			}
			
			UserEntity dbuser = optUser.get();
			model.addAttribute("user", dbuser);
			msg.put("user", dbuser);
			msg.put("message", "Data fetched successfully.");

			return ResponseEntity.status(HttpStatus.OK).body(msg);
			
		} catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}
	}
	
	@PatchMapping("updatemember")
	public ResponseEntity<?> updateUser(@RequestBody UserEntity userEntity, HttpSession session, Model model) {
		Map<String, Object> msg = new HashMap<>();
		String email = session.getAttribute("useremail").toString();
		if(email == null) {
			msg.put("message", "Email not found in session");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
		}
		
		try {			
			Optional<UserEntity> optUser = userRepo.findUserByEmail(email);
			UserEntity dbuser = optUser.get();
			
			dbuser.setFirstname(userEntity.getFirstname());
			dbuser.setLastname(userEntity.getLastname());
			dbuser.setPhone(userEntity.getPhone());
			dbuser.setSkill(userEntity.getSkill());
			
			userRepo.save(dbuser);
			
			msg.put("updated_user", dbuser);
			msg.put("message", "Data updated successfully.");
			return ResponseEntity.status(HttpStatus.OK).body(msg);
		
		} catch(Exception e) {
			msg.put("message", "Error In data updatation.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}
	}
	
	@PostMapping("sendresetasswordlink")
	public ResponseEntity<?> sendResetPassLink(HttpSession session, Model model) {
		Map<String, Object> msg = new HashMap<>();
		String email = session.getAttribute("useremail").toString();
		if(email == null) {
			msg.put("message", "Email not found in session");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
		}
		
		try {			
			Optional<UserEntity> optUser = userRepo.findUserByEmail(email);
			UserEntity dbuser = optUser.get();
			
			String token = resetPasswordService.createResetToken(email);
			if(token == null) {
				msg.put("message", "Server Error");
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
			}
			
			String mailSubject = "Reset Your Password - Hackathon Portal";
			StringBuffer sb = new StringBuffer();
			sb.append(dbuser.getFirstname())
			  .append(" ")
			  .append(dbuser.getLastname());
			
			mailService.sendEmail(dbuser.getEmail(), mailSubject, mailService.resetPassMailBody(sb.toString(), token));

			sb.setLength(0);

			msg.put("message", "Reset Password link has been sent successfully.");
			return ResponseEntity.status(HttpStatus.OK).body(msg);
		
		} catch(Exception e) {
			msg.put("message", "Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}
		
		
	}
	
	
	@PostMapping("updateprofilepic")
	public ResponseEntity<?> updateProfileImage(@RequestParam MultipartFile profilePic, HttpSession session) {
		Map<String, Object> msg = new HashMap<>();
		String email = session.getAttribute("useremail").toString();
		if(email == null) {
			msg.put("message", "Email not found in session");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
		}
		
		if(!profilePic.isEmpty()) {
			msg.put("message", "Please upload an Image.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		} else {
			if(!profilePic.getContentType().startsWith("image")) {
				msg.put("message", "Only Image accepted.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
			} else {
				StringBuffer sb = new StringBuffer();
				
				Optional<UserEntity> optUser = userRepo.findUserByEmail(email);
				UserEntity dbuser = optUser.get();
				
				String dbemail = dbuser.getEmail();
				String firstname = dbuser.getFirstname();
				String filepath = sb.append(uploadDir).append(email).toString();
				sb.setLength(0);
				sb.append(firstname).append("_").append(email.substring(0, email.lastIndexOf("."))).append(".").append(getExtension(profilePic.getOriginalFilename()));
				
				File userFile = new File(filepath, sb.toString());
				
				try {
					FileUtils.writeByteArrayToFile(userFile, profilePic.getBytes());
				} catch(IOException e) {
					msg.put("message", "Error In file uploading.");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
				}
				
				dbuser.setProfile_pic_path("user_profile_pic/"+dbemail+"/"+sb.toString());
				userRepo.save(dbuser);
				sb.setLength(0);
				msg.put("updated_user", dbuser);
				msg.put("message", "Data updated successfully.");
				return ResponseEntity.status(HttpStatus.OK).body(msg);
			}
		}
	}
	
	@PostMapping("changepassword")
	public ResponseEntity<?> changePassword(@RequestParam String password, @RequestParam String cnf_password, Model model, HttpSession session) {
		Map<String, Object> msg = new HashMap<>();
		String email = session.getAttribute("useremail").toString();
		if(!password.equals(cnf_password)) {
			msg.put("message", "Password and Confirm password are not matched.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
		 
		}
		
		if(email == null) {
			msg.put("message", "Email not found in session");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
		}
		
		try {			
			
			Optional<UserEntity> user = userRepo.findUserByEmail(email);
			UserEntity userEntity = user.get();
			userEntity.setPassword(passwordEncoder.encode(password));
			userRepo.save(userEntity); 
		
			msg.put("message", "Password Updated successfully.");
			return ResponseEntity.status(HttpStatus.OK).body(msg);
		 
		} catch(Exception e) {
			msg.put("message", "Server Error");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}
	}
	
	public String getExtension(String filename) {
		Map<String, Object> msg = new HashMap<>();
		if(filename != null && filename.contains(".")) {
			return filename.substring(filename.lastIndexOf(".")+1);
		}
		return "";
	}
}
