package com.unipi.cs.kt.appBackendTest.DataClasses;

import javax.persistence.*;

@Entity
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /** @noinspection JpaDataSourceORMInspection*/
    @ManyToOne
    @JoinColumn(name = "user_data_uid")
    private UserData userData;
    private String recommendation;
    private String recommendationType;
    private Integer userRating;
    private Integer percentOfSimilarUsersWithApp;

    public Recommendation(Long id,UserData userData, String recommendation, String recommendationType, Integer userRating, Integer percentOfSimilarUsersWithApp) {
        this.id = id;
        this.userData = userData;
        this.recommendation = recommendation;
        this.recommendationType = recommendationType;
        this.userRating = userRating;
        this.percentOfSimilarUsersWithApp = percentOfSimilarUsersWithApp;
    }

    public Recommendation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserData getUserData(){
        return userData;
    }

    public void setUserData(UserData userData){
        this.userData = userData;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }

    public Integer getUserRating() {
        return userRating;
    }

    public void setUserRating(Integer userLiked) {
        this.userRating = userLiked;
    }

    public Integer getPercentOfSimilarUsersWithApp(){
        return percentOfSimilarUsersWithApp;
    }

    public void setPercentOfSimilarUsersWithApp(Integer percentOfSimilarUsersWithApp){
        this.percentOfSimilarUsersWithApp = percentOfSimilarUsersWithApp;
    }
}
