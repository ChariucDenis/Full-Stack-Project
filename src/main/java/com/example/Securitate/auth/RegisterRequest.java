package com.example.Securitate.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Boolean isAdmin;  // Flag pentru a specifica dacă utilizatorul este Admin

    // Getter pentru isAdmin
    public Boolean getIsAdmin() {
        return isAdmin;
    }

    // Setter pentru isAdmin
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
