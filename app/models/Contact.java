package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;
 
@Entity
public class Contact extends Model {
 
    public String email;
    public String fullname;
    public ArrayList<String> pendingLinks;
    
	@ManyToOne
	public User user;

    public Contact(String email, String fullname, User user) {
        this.email = email;
        this.fullname = fullname;
        this.user = user;
        this.pendingLinks = new ArrayList<String>();
    }

    public void addLink(String Url) {
    	this.pendingLinks.add(Url);
    	this.save();
    }

    public void resetLinks() {
    	this.pendingLinks = new ArrayList<String>();
    	this.save();
    }
 
}