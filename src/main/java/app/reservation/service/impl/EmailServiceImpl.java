package app.reservation.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import app.reservation.model.Appointment;
import app.reservation.model.Mail;
import app.reservation.model.User;
import app.reservation.service.AppointmentService;
import app.reservation.service.EmailService;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Service
@EnableScheduling
public class EmailServiceImpl implements EmailService {
	@Autowired
	JavaMailSender mailSender;

	@Autowired
	VelocityEngine velocityEngine;

	@Autowired
	AppointmentService appointmentService;

	public void sendEmail(Mail mail) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();

		try {

			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

			mimeMessageHelper.setSubject(mail.getMailSubject());
			mimeMessageHelper.setFrom(mail.getMailFrom());
			mimeMessageHelper.setTo(mail.getMailTo());
			mail.setMailContent(geContentFromTemplate(mail.getModel(), mail.getMailTemplate()));
			mimeMessageHelper.setText(mail.getMailContent(), true);

			mailSender.send(mimeMessageHelper.getMimeMessage());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public String geContentFromTemplate(Map<String, Object> model, String template) {
		StringBuffer content = new StringBuffer();
		try {
			// templates/email-template.vm
			content.append(
					VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "/templates/" + template, model));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content.toString();
	}

//	 @Scheduled(cron = "0/60 * * * * *")
	public void sendReminder() {
		System.out.println("scheduler is running xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		Date date = new Date();
		Date time = new Date();
		date.setDate(date.getDate() + 1);
		time.setSeconds(0);
		time.setHours(time.getHours() + 12);
		List<Appointment> li = appointmentService.getAllAppointment();
		List<String> listEmail = new ArrayList<>();
		Appointment app = null;
		for (Appointment a : li) {
			if (a.getSession().getStartDate().getDate() == date.getDate() && a.getSession().getStartTime().getHours() == time.getHours()
					&& a.getSession().getStartTime().getMinutes() == time.getMinutes()) {
				listEmail.add(a.getPerson().getEmail());
				System.out.println("=====================================================");
				System.out.println(a.toString());
				app = a;
			}
		}
		
		if (listEmail.size() > 0) {
			String emailTo = listEmail.stream()	                
	                .collect(Collectors.joining(","));
			System.out.println(emailTo);
			Mail mail = new Mail();
			mail.setMailTemplate("email-template.vm");
			mail.setMailFrom("tm.reservation.checking@gmail.com");
			mail.setMailTo(emailTo);
			mail.setMailSubject("This is an reminder email from TM Checking Online Reservation System");

			Map<String, Object> modelMail = new HashMap<String, Object>();
//			modelMail.put("firstName", user.getPerson().getFirstname());
//			modelMail.put("lastName", user.getPerson().getLastname());
			modelMail.put("date", app.getSession().getStartDate());
			modelMail.put("time", app.getSession().getStartTime());
			modelMail.put("location", app.getSession().getLocation());
			mail.setModel(modelMail);
			sendEmail(mail);
		}
		
	}

	public void sendMailAfterCreateAppointment(User user, Appointment appointment, String action) {
		Mail mail = new Mail();
		mail.setMailTemplate("email-template.vm");
		mail.setMailFrom("tm.reservation.checking@gmail.com");
		mail.setMailTo(user.getPerson().getEmail());

		mail.setMailSubject("This is an email from TM Checking Online Reservation System");

		Map<String, Object> modelMail = new HashMap<String, Object>();
		modelMail.put("action", action);
		modelMail.put("firstName", user.getPerson().getFirstname());
		modelMail.put("lastName", user.getPerson().getLastname());
		modelMail.put("date", appointment.getSession().getStartDate());
		modelMail.put("time", appointment.getSession().getStartTime());
		modelMail.put("location", appointment.getSession().getLocation());
		mail.setModel(modelMail);
		sendEmail(mail);
	}

}
