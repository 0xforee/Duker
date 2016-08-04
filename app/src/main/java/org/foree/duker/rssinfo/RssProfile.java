package org.foree.duker.rssinfo;

/**
 * Created by foree on 16-7-23.
 */
public class RssProfile {
    private String locale;
    private String gender;
    private String givenName;
    private String familyName;
    private String fullName;
    private String id;
    private String picture;
    private String email;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getId() {
        return id;
    }

    public String getPicture() {
        return picture;
    }

    public String getEmail() {
        return email;
    }
}
