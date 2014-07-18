package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;

public final class currentUser {
	public User user;
	currentUser(){
		user = new User("tester", "secure", "Testy McTesterson").save();
		user.addContact("sloth@slothville.com", "Jon");
		user.addContact("sloth@slothville.com", "Sam");
		user.addContact("sloth@slothville.com", "Ygritte");
	}
}