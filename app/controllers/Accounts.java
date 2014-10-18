package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Scanner;

import models.Contact;
import models.User;

import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.libs.Mail;
import play.mvc.Before;
import play.mvc.Controller;

public class Accounts extends Controller {

	@OnApplicationStart
	public class Bootstrap extends Job {

		public void doJob() {
			User currentUser = new User("tester", "secure", "Testy McTesterson").save();
			currentUser.addContact("iknow@nothing.com", "Jon");
			currentUser.addContact("samandgilly@4eva.com", "Sam");
			currentUser.addContact("xnoscope420x@northofthewall.com", "Ygritte");
		}    
	}

	@Before
	static void addDefaults() {
		String username = session.get("username");
		String loggedIn = session.get("loggedIn");
		User currentUser = User.connect(session.get("username"), session.get("password"));
		if(currentUser != null){
			renderArgs.put("username", username);
			renderArgs.put("fullname", currentUser.fullname);
			renderArgs.put("currentUser", currentUser);
		}
		renderArgs.put("loggedIn", loggedIn);

	}

	public static void accountPage() {
		render();
	}


	public static void newContact() {
		String fullname = params.get("contact_name");
		String email = params.get("contact_email");
		validation.clear();
		validation.required(fullname).message("A name is required.");
		validation.required(email).message("An email is required.");

		// Could add more validation to check for a valid email address, for example.

		if(validation.hasErrors()) {
			for(play.data.validation.Error error : validation.errors()) {
				System.out.println(error.message());
			}
			params.flash();
			validation.keep();
		} else{
			User currentUser = User.connect(session.get("username"), session.get("password"));
			Contact newContact = new Contact(email, fullname, currentUser).save();
			Logger.info("contact added!");
			flash("success", "Contact Added!");
		}
		accountPage();
	}

	public static void deleteContact(String email, String fullname) {
		Logger.info("Deleting "+fullname + " at " + email);
		User currentUser = User.connect(session.get("username"), session.get("password"));
		currentUser.deleteContact(email, fullname);
	}



	public static void login() {
		String username = params.get("username");
		String password = params.get("password");
		User currentUser = User.connect(username, password);
		if(currentUser == null){
			// Error, authentication failed.
		} else if(currentUser != null){
			Logger.info(currentUser.fullname);
			session.put("loggedIn", "true");
			session.put("username", username);
			session.put("password", password);
			renderArgs.put("loggedIn", "true");
			renderArgs.put("username", username);
			renderArgs.put("password", password);
			renderArgs.put("currentUser", currentUser);
		}
		index();
	}

	public static void logout() {
		session.put("loggedIn", "false");
		renderArgs.put("loggedIn", "false");
		index();
	}

	public static void register() {
		render();
	}

	// Create a new account
	public static void newRegister() {
		String username = params.get("username");
		String fullname = params.get("fullname");
		String password = params.get("password");
		String password_confirm = params.get("password_confirm");
		validation.clear();
		validation.required(username).message("Choose a username.");
		validation.required(fullname).message("We need your name.");
		validation.required(password).message("Please enter a password");
		validation.required(password_confirm).message("Please confirm your password");
		if(User.find("byUsername", username).first() != null) {
			validation.addError("username_taken", "This username already in use.");
		}
		if(!password.equals(password_confirm)){
			validation.addError("password_match", "The passwords given do not match.");
		}
		if(validation.hasErrors()) {
			for(play.data.validation.Error error : validation.errors()) {
				System.out.println(error.message());
			}
			params.flash();
			validation.keep();
			register();
		} else{
			User currentUser = new User(username, password, fullname).save();
			Logger.info(currentUser.fullname);
			session.put("loggedIn", "true");
			session.put("username", username);
			session.put("password", password);
			renderArgs.put("loggedIn", "true");
			renderArgs.put("username", username);
			renderArgs.put("password", password);
			renderArgs.put("currentUser", currentUser);
		}
		index();
	}

}