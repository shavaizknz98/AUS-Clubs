package com.example.ausclubs;

public class Users {

    private String Email;
    private String Name;
    private boolean Admin;

    public Users() {
    }

    public Users(String email, String name, boolean Admin) {
        Email = email;
        Name = name;
        this.Admin = Admin;
    }

    public String getEmail() {
        return Email;
    }


    public void setEmail(String email) {
        Email = email;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean isAdmin() {
        return Admin;
    }


    public void setAdmin(boolean Admin) {
        this.Admin = Admin;
    }


}
