package user;
import java.util.Map;

import javafx.collections.ObservableList;

public interface UserDao {
    ObservableList<User> getAllUsers();
    ObservableList<User> getAllUsers(Map<String, String> params);
    User addUser(Map<String, String> params);
    boolean updateUser(User user, String field, String value);
    boolean deleteUser(int id);
    User getById(int id);
}