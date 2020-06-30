package user;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import database.DBConnection;

public class UserJDBMCDAO extends Model implements UserDao {
    Connection connection = null;
    PreparedStatement ps  = null;
    ResultSet rs          = null;

    public UserJDBMCDAO() throws Exception {
        this.connection = DBConnection.getConnection();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public User getById(int id) {
        try {
            this.ps = this.connection.prepareStatement("SELECT * FROM `users` WHERE `id` = ?");
            this.ps.setInt(1, id);
            this.rs = this.ps.executeQuery();
            
            if (this.rs.next()) {
                User data = new User(
                    this.rs.getInt("id"),
                    this.rs.getString("first_name"),
                    this.rs.getString("last_name"),
                    this.rs.getString("email"),
                    this.rs.getTimestamp("created"),
                    this.rs.getTimestamp("modified")
                );
                
                return data;
            }
        }
        catch (SQLException e) {
            setErrorMessage(e.getMessage());
        }
        
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    @Override
    public ObservableList<User> getAllUsers() {
        Map<String, String> params = null;
        return FXCollections.observableArrayList(this.getAllUsers(params));
    }

    ///////////////////////////////////////////////////////////////////////////
    @Override
    public ObservableList<User> getAllUsers(Map<String, String> params) {
        try {
            List<User> data = new ArrayList<User>();
            
            // declare base query for all users
            String query    = "SELECT * FROM `users`";
            
            // if filter data was sent, use it to narrow down results
            if (params != null) {
                query += "  WHERE `first_name` LIKE ? AND `last_name` LIKE ? AND `email` LIKE ?";
            }
            
            // send query to prepared statement
            this.ps = this.connection.prepareStatement(query);
            
            // send parameters to prepared statement
            if (params != null) {
                this.ps.setString(1, "%" + params.get("first_name") + "%");
                this.ps.setString(2, "%" + params.get("last_name")  + "%");
                this.ps.setString(3, "%" + params.get("email")      + "%");    
            }
            
            this.rs = this.ps.executeQuery();
            
            while (this.rs.next()) {
                data.add(
                    new User(
                        this.rs.getInt("id"),
                        this.rs.getString("first_name"),
                        this.rs.getString("last_name"),
                        this.rs.getString("email"),
                        this.rs.getTimestamp("created"),
                        this.rs.getTimestamp("modified")
                    )
                );
            }
            
            return FXCollections.observableArrayList(data);

        }
        catch (SQLException e) {
            setErrorMessage(e.getMessage());
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    @Override
    public User addUser(Map<String, String> params) {
        if (isDataValid(params)) {
            try {
                int new_id = createUserDBRecord(params);
                
                // on successful DB creation, the result is the new id (int > 0)
                // if that's the case, fetch the user with this id and return it
                if (new_id > 0) {
                    return this.getById(new_id);
                }
                else {
                    setErrorMessage("An SQL error occurred, the new user could not be added.");
                }
            }
            catch (Exception e) {
                setErrorMessage(e.getMessage());
            }
        }
        
        return null;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public boolean updateFirstName(User user, String value) {
        if (updateUser(user, "first_name", value)) {
            user.setFirstName(value);
            return true;
        }
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public boolean updateLastName(User user, String value) {
        if (updateUser(user, "last_name", value)) {
            user.setLastName(value);
            return true;
        }
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public boolean updateEmail(User user, String value) {
        int id = user.getId();
        
        // check email validity
        if (!this.isEmailValid(value)) {
            setErrorMessage("Please provide a valid email address");
            return false;
        }
        
        // check whether the email is already assigned to another user
        if (!this.isFieldUnique("email", value, id)) {
            setErrorMessage("The email address " + value + " is already assigned to another user.");
            return false;
        }
        
        // check if the SQL update was successful, and if so, update the user object
        if (this.updateUser(user, "email", value)) {
            user.setEmail(value);
            return true;
        }
        
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean updateUser(User user, String field, String value) {
        if (isEmpty(value)) {
            setErrorMessage("This field cannot be empty.");
            return false;
        }

        try {
            String query = "UPDATE `users` SET `" + field + "` = ?, `modified` = NOW() WHERE `id` = ?";
            this.ps = this.connection.prepareStatement(query);
            this.ps.setString(1, value);
            this.ps.setInt(2, user.getId());
            this.ps.executeUpdate();
            
            this.updateModifiedField(user);            

            return true;
        }
        catch (SQLException e) {
            setErrorMessage(e.getMessage());
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    protected void updateModifiedField(User user) {
        int id = user.getId();
        try {
            String query = "SELECT * FROM `users` WHERE `id` = ?";
            this.ps = this.connection.prepareStatement(query);
            this.ps.setInt(1, id);    
            this.rs = this.ps.executeQuery();

            if (this.rs.next()) {
                user.setModified(this.rs.getTimestamp("modified"));
            }
        }
        catch (SQLException e) {
            // this is not a critical operation, don't acknowledge failure
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean deleteUser(int id) {
        try {
            
            this.ps = this.connection.prepareStatement("DELETE FROM `users` WHERE `id` = ?");
            this.ps.setInt(1, id);
            int count = this.ps.executeUpdate();
            if (count == 0) {
                this.setErrorMessage("The user could not be deleted from the database.");
            }
            
            return (count > 0);
        }
        catch (SQLException e) {
            setErrorMessage(e.getMessage());
        }
        
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected int createUserDBRecord(Map<String, String> fields) throws SQLException {
        String query = "INSERT INTO `users` (`first_name`, `last_name`, `email`, `created`, `modified`) VALUES (?, ?, ?, NOW(), NOW())";
        this.ps = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        this.ps.setString(1, fields.get("first_name"));
        this.ps.setString(2, fields.get("last_name"));
        this.ps.setString(3, fields.get("email"));
        this.ps.executeUpdate();
        
        // get last inserted ID
        this.rs = this.ps.getGeneratedKeys();
        if (this.rs.next()) {
            return this.rs.getInt(1);
        }
        
        return 0;
    }

    ///////////////////////////////////////////////////////////////////////////
    protected boolean isDataValid(Map<String, String> params) {
        String first_name = params.get("first_name"),
               last_name  = params.get("last_name"),
               email      = params.get("email");
        
        if (isEmpty(first_name)) {
            setErrorMessage("Please enter first name.");
        }
        else if (isEmpty(last_name)) {
            setErrorMessage("Please enter last name.");
        }
        else if (!isEmailValid(email)) {
            setErrorMessage("Please provide a valid email address");
        }
        else if (!isFieldUnique("email", email)) {
            setErrorMessage("User with this email address already exists.");
        }
        else {
            return true;
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // without checking for current user ID
    protected boolean isFieldUnique(String field, String value) {
        return isFieldUnique(field, value, 0);
    }

    ///////////////////////////////////////////////////////////////////////////
    // when editing user, the uniqueness of their email needs to exclude their own record
    protected boolean isFieldUnique(String field, String value, int id) {
        try {
            String query = "SELECT * FROM `users` WHERE `" + field + "` = ?";

            if (id > 0) {
                query += " AND `id` != ?";
            }

            this.ps = this.connection.prepareStatement(query);
            this.ps.setString(1, value);

            if (id > 0) {
                this.ps.setInt(2,  id);    
            }

            this.rs = this.ps.executeQuery();

            return !(this.rs.next());
        }
        catch (SQLException e) {
            setErrorMessage(e.getMessage());
        }

        return false;
    }

}