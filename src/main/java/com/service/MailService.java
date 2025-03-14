package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	@Autowired
	private JavaMailSender mailSender;
	
	 public void sendEmail(String to, String subject, String body) {
	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(to);
	        message.setSubject(subject);
	        message.setText(body);
	        message.setFrom("noreply.hackathon@gmail.com");

	        mailSender.send(message);
	    }
	 
	 public String signUpMailBody(String name) {
		 return "Hi " + name + ",\n\n"
			        + "Welcome to [Your Website Name] – the ultimate platform for organizing and participating in online & offline hackathons! 🚀\n\n"
			        + "🔹 Find exciting hackathons – Compete in online and offline events.\n"
			        + "🔹 Create your own hackathons – Organize events and invite participants.\n"
			        + "🔹 Connect with the community – Network with fellow innovators and tech enthusiasts.\n\n"
			        + "Your journey starts here!\n"
			        + "👉 Login and explore: [Website Link]\n\n"
			        + "Happy Hacking!\n"
			        + "[Your Website Team]";  
	 }
	 
	 public String resetPassMailBody(String name, String token) {
		
		return "Hello " + name + ",\n\n"
		         + "We received a request to reset your password for your Hackathon Portal account. "
		         + "Click the link below to reset your password:\n\n"
		         + "<a href='" + "http://localhost:9898/member/setpassword?token=" + token + "'>Reset Password</a>\n\n"
		         + "If the above link doesn’t work, copy and paste the following URL into your browser:\n"
		         + "http://localhost:9898/member/setpassword" + "?token=" + token + "\n\n"
		         + "This link is valid for only **15 minutes**.\n\n"
		         + "If you did not request a password reset, please ignore this email or contact our support team.\n\n"
		         + "Thank you,\n"
		         + "Hackathon Team";

	 }
	 
	 
}
