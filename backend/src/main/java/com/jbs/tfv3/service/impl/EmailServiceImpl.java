package com.jbs.tfv3.service.impl;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.jbs.tfv3.dto.EmailDetails;
import com.jbs.tfv3.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
	private String sender;

	@Override
	public String sendSimpleEmail(EmailDetails ed) {
		// Try block to check for exceptions
		try {

			// Creating a simple mail message
			SimpleMailMessage mailMessage = new SimpleMailMessage();

			// Setting up necessary details
			mailMessage.setFrom(sender);
			mailMessage.setTo(ed.getRecipient());
			mailMessage.setText(ed.getMsgBody());
			mailMessage.setSubject(ed.getSubject());

			// Sending the mail
			javaMailSender.send(mailMessage);
			return "Mail Sent Successfully...";
		}

		// Catch block to handle the exceptions
		catch (Exception e) {
			e.printStackTrace();
            // Display message when exception occurred
			return "Error while Sending Mail";
		}
	}

	@Override
	public String sendEmailWithAttachment(EmailDetails ed) {
		
		// Creating a mime message
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        
        MimeMessageHelper mimeMessageHelper;

        try {
            // Setting multipart as true for attachments to be send
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(ed.getRecipient());
            mimeMessageHelper.setText(ed.getMsgBody());
            mimeMessageHelper.setSubject(ed.getSubject());

            // Adding the attachment
            FileSystemResource file = new FileSystemResource(new File(ed.getAttachment()));

            mimeMessageHelper.addAttachment(file.getFilename(), file);

            // Sending the mail
            javaMailSender.send(mimeMessage);
            return "Mail sent Successfully with Attachment...";
        }

        // Catch block to handle MessagingException
        catch (MessagingException e) {
        	e.printStackTrace();
            // Display message when exception occurred
            return "Error while sending mail!!!";
        }
	}

}
