package com.unipi.cs.kt.appBackendTest.DataClasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unipi.cs.kt.appBackendTest.Utils.StringListConverter;

import javax.persistence.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class UserData {
    @Id
    @Column(unique = true,nullable = false)
    private String uid;
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition="TEXT")
    private List<String> apps;
    @OneToMany(fetch = FetchType.EAGER,mappedBy = "userData",cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Recommendation> recommendations;
    private long lastSeen;

    public UserData(String uid) {
        this.uid = uid;
        this.lastSeen = System.currentTimeMillis();
        initializeRecommendations();
    }

    public UserData() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<String> getApps() {
        return apps;
    }

    public void setApps(List<String> apps) {
        this.apps = apps;
    }

    public List<Recommendation> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }
    //Check if the user has interacted with the API today
    public boolean seenToday() {
        LocalDate today = LocalDate.now();
        Instant lastSeenInstant = Instant.ofEpochMilli(lastSeen);
        LocalDate lastSeenDate = lastSeenInstant.atZone(ZoneId.systemDefault()).toLocalDate();
        return today.equals(lastSeenDate);
    }
    public long getLastSeen(){return lastSeen;}

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public void addRecommendation(Recommendation recommendation){
        this.recommendations.add(recommendation);
    }
    //Generate the initial recommendations everyone has to rate
    private void initializeRecommendations(){
        this.recommendations = new ArrayList<>();
        this.recommendations.add(new Recommendation(null,this,"ACCESSIBILITY_ENABLED","Setting",null,null));
        this.recommendations.add(new Recommendation(null,this,"BLUETOOTH_ON","Setting",null,null));
        this.recommendations.add(new Recommendation(null,this,"ADB_ENABLED","Setting",null,null));
        this.recommendations.add(new Recommendation(null,this,"USB_MASS_STORAGE_ENABLED","Setting",null,null));
        this.recommendations.add(new Recommendation(null,this,"isDeviceSecure","Setting",null,null));
        this.recommendations.add(new Recommendation(null,this,"isDataEnabled","Setting",null,null));
        this.recommendations.add(new Recommendation(null,this,"Youtube","App",null,null));
        this.recommendations.add(new Recommendation(null,this,"Twitter","App",null,null));
        this.recommendations.add(new Recommendation(null,this,"Facebook","App",null,null));
        this.recommendations.add(new Recommendation(null,this,"Twitch","App",null,null));
        this.recommendations.add(new Recommendation(null,this,"Yahoo Finance","App",null,null));
        this.recommendations.add(new Recommendation(null,this,"Spotify","App",null,null));
        this.recommendations.add(new Recommendation(null,this,"Reddit","App",null,null));
        this.recommendations.add(new Recommendation(null,this,"Steam","App",null,null));
        this.recommendations.add(new Recommendation(null,this,"Netflix","App",null,null));
        this.recommendations.add(new Recommendation(null,this,"Microsoft Teams","App",null,null));
    }
}
