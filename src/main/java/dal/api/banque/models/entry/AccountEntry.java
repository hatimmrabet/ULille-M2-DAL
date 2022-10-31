package dal.api.banque.models.entry;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class AccountEntry {

    @NotEmpty(message = "name is required")
    String name;
    @NotEmpty(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    String password;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    
    
}
