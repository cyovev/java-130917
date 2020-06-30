package user;

// some basic validation rules and error handling belong here 
public class Model {
    
    String errorMessage;
    
    ///////////////////////////////////////////////////////////////////////////
    public boolean isEmpty (String string) {
        return (string == null || string.isEmpty());
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public boolean isEmailValid(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    public String getErrorMessage() {
        return this.errorMessage;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    protected void setErrorMessage(String message) {
        this.errorMessage = message;
    }
    
}
