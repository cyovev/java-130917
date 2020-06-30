package user;
import java.sql.Timestamp;

public class User {
    
    private final String dateFormat = "yyyy-MM-dd HH:mm:ss";

    private Integer   id;
    private String    first_name;
    private String    last_name;
    private String    email;
    private Timestamp created;
    private Timestamp modified;
    
    ///////////////////////////////////////////////////////////////////////////
    public User(Integer id, String first_name, String last_name, String email, Timestamp created, Timestamp modified) {
        this.id         = id;
        this.first_name = first_name;
        this.last_name  = last_name;
        this.email      = email;
        this.created    = created;
        this.modified   = modified;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public Integer getId() {
        return id;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public String getFirstName() {
        return this.first_name;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public String getLastName() {
        return this.last_name;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public void setLastName(String last_name) {
        this.last_name = last_name;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public String getFullName() {
        return this.getFirstName() + " " + this.getLastName();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public String getEmail() {
        return this.email;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public void setEmail(String email) {
        this.email = email;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Timestamp gets converted to String for the TableView
    public String getCreated() {
        return this._convertToDateString(this.created);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Timestamp gets converted to String for the TableView
    public String getModified() {
        return this._convertToDateString(this.modified);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void setModified(Timestamp datetime) {
        this.modified = datetime;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected String _convertToDateString(Timestamp date) {
        return new java.text.SimpleDateFormat(this.dateFormat).format(date);
    }

}