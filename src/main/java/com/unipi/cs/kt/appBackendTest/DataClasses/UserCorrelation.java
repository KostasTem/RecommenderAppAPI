package com.unipi.cs.kt.appBackendTest.DataClasses;

import org.springframework.security.core.userdetails.User;

import javax.persistence.*;

@Entity
public class UserCorrelation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /** @noinspection JpaDataSourceORMInspection*/
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "primary_user_id")
    private UserData primaryUser;
    /** @noinspection JpaDataSourceORMInspection*/
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "other_user_id")
    private UserData otherUser;
    private float correlation;

    public UserCorrelation() {
    }

    public UserCorrelation(Long id,UserData primaryUser, UserData otherUser, float correlation) {
        this.id = id;
        this.primaryUser = primaryUser;
        this.otherUser = otherUser;
        this.correlation = correlation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserData getPrimaryUser() {
        return primaryUser;
    }

    public void setPrimaryUser(UserData primaryUser) {
        this.primaryUser = primaryUser;
    }

    public UserData getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(UserData otherUser) {
        this.otherUser = otherUser;
    }

    public double getCorrelation() {
        return correlation;
    }

    public void setCorrelation(float correlation) {
        this.correlation = correlation;
    }
}
