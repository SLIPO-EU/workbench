package eu.slipo.workbench.web.model.admin;

import java.util.EnumSet;
import java.util.Set;

import eu.slipo.workbench.common.model.EnumRole;

public class AccountCreateRequest {

    private String email;

    private String givenName;

    private String familyName;

    private String password;

    private Set<EnumRole> roles = EnumSet.noneOf(EnumRole.class);

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<EnumRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<EnumRole> roles) {
        this.roles = roles;
    }

}
