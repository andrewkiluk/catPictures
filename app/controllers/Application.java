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

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void attachLink(String contact_email, String contact_fullname) {
        String username = session.get("username");
        String password = session.get("password");
        User currentUser = User.connect(username, password);

        String newUrl = params.get("body");

        Contact currentContact = Contact.find("byFullnameAndEmail", contact_fullname, contact_email).first();

        currentContact.addLink(newUrl);
    }

    public static void upload(String contact_email, String contact_fullname, String filename) {
        if (request.isNew) {

            String username = session.get("username");
            String tmpPath = Play.getFile("tmp").getAbsolutePath();

            FileOutputStream moveTo = null;

            new File(tmpPath + "/uploads").mkdir();
            new File(tmpPath + "/uploads/" + username).mkdir();
            new File(tmpPath + "/uploads/" + username +"/"+ contact_email).mkdir();

            Logger.info("Absolute path to send %s", Play.getFile("").getAbsolutePath() + File.separator + "uploads" + File.separator);
            try {

                InputStream data = request.body;

                String filePath = tmpPath + File.separator + "uploads" + File.separator + username + File.separator + contact_email + File.separator + filename;
                File fileToWrite = new File(filePath);

                moveTo = new FileOutputStream(fileToWrite);
                IOUtils.copy(data, moveTo);


                // Scanner fileScanner = new Scanner(fileToWrite);
                // fileScanner.nextLine();
                // fileScanner.nextLine();
                // fileScanner.nextLine();

                // FileWriter fileStream = new FileWriter(filePath);
                // BufferedWriter out = new BufferedWriter(fileStream);
                // while(fileScanner.hasNextLine()) {
                //     String next = fileScanner.nextLine();
                //     if(next.equals("\n")) {
                //         out.newLine();
                //     }
                //     else out.write(next);
                //     out.newLine();   
                // }
                // out.close();



            } catch (Exception ex) {

                // catch file exception
                // catch IO Exception later on
                renderJSON("{success: false}");
            }
        }
    }

    public static void email(String contact_email, String contact_fullname) {
        String username = session.get("username");
        String password = session.get("password");
        User currentUser = User.connect(username, password);

        Contact currentContact = Contact.find("byFullnameAndEmail", contact_fullname, contact_email).first();

        try{
            HtmlEmail email = new HtmlEmail();
            email.setHostName("smtp.mandrillapp.com");
            email.addTo(contact_email, "Test");
            email.setFrom("catPictures@andrewkiluk.com", "catPictures");
            //email.addReplyTo(currentUser.email);
            email.setSubject("Animal pictures from " + currentUser.fullname);
            String htmlMessage = "Hi " + contact_fullname + ", <br/> Your friend "+ currentUser.fullname + " has sent you animal pictures!";


            // Add linked images to the htmlMessage
            for( String link : currentContact.pendingLinks){
                htmlMessage += "<br/><br/><img src=" + link +"/>";
            }
            currentContact.resetLinks();

            email.setHtmlMsg(htmlMessage);
            Logger.info(htmlMessage);

            // Now attach uploaded files

            String filesPath = Play.getFile("").getAbsolutePath() + "/tmp/uploads/" + username +"/"+ contact_email;
            File dir = new File(filesPath);
            File[] directoryListing = dir.listFiles();

            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (!child.getName().substring(0, 1).equals(".")){
                        Logger.info("Sending " + child.getName());

                        // Create an attachment for the file
                        EmailAttachment attachment = new EmailAttachment();
                        attachment.setPath(child.getAbsolutePath());
                        attachment.setDisposition(EmailAttachment.ATTACHMENT);
                        attachment.setName(child.getName());
                        email.attach(attachment);
                    }
                }
            }

            else {
                Logger.info("No files found to attach to email.");
            }

            Mail.send(email);
            Logger.info("Email sent");

        } catch (EmailException e){
            Logger.info(e.getMessage());
        }

        index();
    } 

}