package models;
 
import java.util.*;
import javax.persistence.*;

import java.util.*;
import java.util.logging.Logger;
 
import play.db.jpa.*;
 
@Entity
public class User extends Model {
 
    public String username;
    public String fullname;
    public boolean loggedIn;
    public String password;
    

    @OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	public List<Contact> contacts;
    
    public User(String username, String password, String fullname) {
        this.contacts = new ArrayList<Contact>();
        this.username = username;
        this.fullname = fullname;
        this.loggedIn = false;
        this.password = password;

    }

    public static User connect(String username, String password) {
	    return find("byUsernameAndPassword", username, password).first();
	}

	public User addContact(String email, String fullname) {
	    Contact newContact = new Contact(email, fullname, this).save();
	    this.contacts.add(newContact);
	    this.save();
	    return this;
	}

    public User deleteContact(String email, String fullname) {
        Contact oldContact = Contact.find("byEmailAndFullname", email, fullname).first();
        this.contacts.remove(oldContact);
        if(oldContact != null){
            oldContact.delete();
        }
        this.save();
        return this;
    }
 
}