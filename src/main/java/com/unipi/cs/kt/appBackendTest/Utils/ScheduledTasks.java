package com.unipi.cs.kt.appBackendTest.Utils;

import com.unipi.cs.kt.appBackendTest.DataClasses.Recommendation;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserCorrelation;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import com.unipi.cs.kt.appBackendTest.Services.RecommendationService;
import com.unipi.cs.kt.appBackendTest.Services.UserCorrelationService;
import com.unipi.cs.kt.appBackendTest.Services.UserDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//Task that runs every 24 hours to update
//the user correlations for every user that
//has used the app in the last 24 hours
@Component
public class ScheduledTasks{

    private final UserDataService userDataService;
    private final UserCorrelationService userCorrelationService;
    private final RecommendationService recommendationService;
    private static final Logger Log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    public ScheduledTasks(UserDataService userDataService, UserCorrelationService userCorrelationService, RecommendationService recommendationService){
        this.userDataService = userDataService;
        this.userCorrelationService = userCorrelationService;
        this.recommendationService = recommendationService;
    }
    //Update UserCorrelations of users that have used the app today every hour
    @Scheduled(fixedDelay = 3600000, initialDelay = 60000)
    public void updateUserCorrelations(){
        List<UserData> userDataList = userDataService.getAllData();
        for(UserData targetUser: userDataList){
            if(targetUser.seenToday()){
                List<UserCorrelation> userCorrelations = userCorrelationService.getCorrelationsForUser(targetUser);
                for(UserCorrelation userCorrelation: userCorrelations){
                    UserData otherUser = userCorrelation.getPrimaryUser().getUid().equals(targetUser.getUid()) ? userCorrelation.getOtherUser() : userCorrelation.getPrimaryUser();
                    float updatedCorrelation = RecommendationLogic.updateUserCorrelation(targetUser,otherUser);
                    if(updatedCorrelation!=userCorrelation.getCorrelation()) {
                        userCorrelation.setCorrelation(updatedCorrelation);
                        userCorrelationService.saveUserCorrelation(userCorrelation);
                    }
                }
            }
        }
        Log.info("Updated user correlations at: " + LocalDateTime.now());
    }
    //Calculate percentage of users using each of the tracked settings once a day
    @Scheduled(fixedDelay = 86460000, initialDelay = 70000)
    public void updateSettingsStatistics(){
        List<UserData> userDataList = userDataService.getAllData();
        List<Integer> sumOfRatings = Arrays.asList(0,0,0,0,0,0,0);
        List<Integer> countOfRatings = Arrays.asList(0,0,0,0,0,0,0);
        for(UserData userData:userDataList){
            for(int i=0;i<6;i++){
                String setting = GlobalVars.settings.get(i);
                Integer rating = userData.getRecommendations().stream().filter(recommendation -> recommendation.getRecommendation().equals(setting)).findFirst().orElse(null).getUserRating();
                if(rating!=null){
                    Integer num = rating == 3 ? 1 : 0;
                    sumOfRatings.set(i, sumOfRatings.get(i) + num);
                    countOfRatings.set(i, countOfRatings.get(i) + 1);
                }
            }
        }
        for(int i=0;i<6;i++){
            double percent = (double) sumOfRatings.get(i)/countOfRatings.get(i);
            GlobalVars.percentageOfUsersWithSetting.set(i, (int) Math.round(percent * 100));
            Log.info("{}% of users have {} enabled",GlobalVars.percentageOfUsersWithSetting.get(i),GlobalVars.settings.get(i));
        }
    }
    //Update percentage of similar users using a recommended app
    @Scheduled(fixedDelay = 3600000, initialDelay = 90000)
    public void updateRecommendationUsagePercent(){
        List<UserData> userDataList = userDataService.getAllData();
        for(UserData userData:userDataList){
            if(!userData.seenToday()){
                continue;
            }
            List<UserData> correlatedUsers = new ArrayList<>();
            List<UserCorrelation> userCorrelations = userCorrelationService.getCorrelationsForUser(userData);
            for(UserCorrelation userCorrelation:userCorrelations){
                if(userCorrelation.getCorrelation()>=0.6){
                    correlatedUsers.add(userCorrelation.getPrimaryUser().getUid().equals(userData.getUid()) ? userCorrelation.getOtherUser() : userCorrelation.getPrimaryUser());
                }
            }
            List<Recommendation> recommendationList = userData.getRecommendations();

            for(Recommendation recommendation: recommendationList){
                if(correlatedUsers.size()==0 || recommendation.getRecommendationType().equals("Setting") || GlobalVars.excludedApps.contains(recommendation.getRecommendation())){
                    continue;
                }
                int usersHave = correlatedUsers.stream().filter(userData1 -> userData1.getApps().contains(recommendation.getRecommendation())).collect(Collectors.toList()).size();
                int allCorrUsers = correlatedUsers.size();
                float percent = (float) usersHave / allCorrUsers;
                recommendation.setPercentOfSimilarUsersWithApp(Math.round(percent*100));
                recommendationService.saveRecommendation(recommendation);
            }
        }
        Log.info("Updated percentage of recommendation usage");
    }

    //Remove any duplicate user correlations
    @Scheduled(fixedDelay = 86520000, initialDelay = 80000)
    public void checkForDuplicateUserCorrelations(){
        List<UserData> userDataList = userDataService.getAllData();
        for(UserData userData: userDataList){
            List<UserCorrelation> userCorrelations = userCorrelationService.getCorrelationsForUser(userData);
            for(int i=0;i<userCorrelations.size();i++){
                UserCorrelation userCorrelation1 = userCorrelations.get(i);
                for(int j=i;j<userCorrelations.size();j++){
                    UserCorrelation userCorrelation2 = userCorrelations.get(j);
                    if(userCorrelation1.getPrimaryUser().equals(userCorrelation2.getOtherUser()) && userCorrelation1.getOtherUser().equals(userCorrelation2.getPrimaryUser())){
                        userCorrelationService.deleteUserCorrelation(userCorrelation1.getId());
                        Log.info("User correlation duplicate data for users {}, {}",userCorrelation1.getPrimaryUser().getUid(),userCorrelation1.getOtherUser().getUid());
                    }
                }
            }
        }
    }
}
