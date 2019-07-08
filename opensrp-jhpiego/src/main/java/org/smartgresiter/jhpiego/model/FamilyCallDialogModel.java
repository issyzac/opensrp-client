package org.smartgresiter.jhpiego.model;

import org.smartgresiter.jhpiego.contract.FamilyCallDialogContract;

public class FamilyCallDialogModel implements FamilyCallDialogContract.Model {

    private String Name;
    private String Role;
    private String PhoneNumber;

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public void setName(String name) {
        Name = name;
    }

    @Override
    public String getRole() {
        return Role;
    }

    @Override
    public void setRole(String role) {
        Role = role;
    }

    @Override
    public String getPhoneNumber() {
        return PhoneNumber;
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

}