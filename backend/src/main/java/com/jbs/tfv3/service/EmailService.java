package com.jbs.tfv3.service;

import com.jbs.tfv3.dto.EmailDetails;

public interface EmailService {
	String sendSimpleEmail(EmailDetails ed);
	String sendEmailWithAttachment(EmailDetails ed);
}
