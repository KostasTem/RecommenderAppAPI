package com.unipi.cs.kt.appBackendTest.Utils;

import com.unipi.cs.kt.appBackendTest.DataClasses.Recommendation;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserCorrelation;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;

import java.util.*;
import java.util.stream.Collectors;

public class RecommendationLogic {
    private static final PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
    //Calls generateRecommendation after finding all the users from userCorrelations that have a correlation >0.6 or <-0.6
    public static Recommendation createRecommendation(List<UserCorrelation> userCorrelations,UserData targetUser){
        List<CorrelationObject> correlationObjects = new ArrayList<>();
        for(UserCorrelation userCorrelation:userCorrelations){
            if(userCorrelation.getCorrelation()>.6 || userCorrelation.getCorrelation()<-.6) {
                UserData otherUser = userCorrelation.getPrimaryUser().getUid().equals(targetUser.getUid()) ? userCorrelation.getOtherUser() : userCorrelation.getPrimaryUser();
                correlationObjects.add(new CorrelationObject(otherUser, userCorrelation.getCorrelation()));
            }
        }
        correlationObjects.sort(Comparator.comparing(CorrelationObject::getCorrelation));
        Collections.reverse(correlationObjects);
        return generateRecommendation(correlationObjects,targetUser);
    }
    //Generates fist correlations when a user finishes their initial ratings
    public static List<UserCorrelation> findCorrelations(List<UserData> otherUsers,UserData targetUser){
        List<UserCorrelation> similarUsers = new ArrayList<>();
        //Iterate every user in user data list
        for(UserData userData:otherUsers){
            if(userData.getRecommendations().stream().filter(recommendation -> recommendation.getUserRating()!=null).collect(Collectors.toList()).size()<GlobalVars.BASE_RECOMMENDATION_APP_COUNT){
                continue;
            }
            float correlation = getCorrelation(targetUser,userData);
            similarUsers.add(new UserCorrelation(null,targetUser,userData,correlation));
        }
        return similarUsers;
    }
    //Is called from ScheduledTasks to update the correlation between two users
    public static float updateUserCorrelation(UserData targetUser, UserData otherUser){
        return getCorrelation(targetUser, otherUser);
    }
    //Calculates the correlation between two users based on their ratings of Recommendations and some of their device settings
    private static float getCorrelation(UserData targetUser, UserData userData){
        List<Integer> targetUserRatings = new ArrayList<>();
        List<Integer> userDataRatings = new ArrayList<>();
        List<String> orderedAppNames = new ArrayList<>();
        //Insert target user's recommendation ratings into targetUserRatings and
        //userData user's recommendation ratings into userDataRatings so entry n
        //from both lists is the rating of the same app recommendation. Ignore all
        //recommendations that both users haven't rated in case such recommendations
        //exist. Apps that have been recommended to one user and not the other have a rating
        //of 0 if the other user doesn't have it installed and 2 if they do have it.
        for(int i=0;i< targetUser.getRecommendations().size();i++){
            Recommendation rec = targetUser.getRecommendations().get(i);
            if(rec.getUserRating()==null){
                continue;
            }
            targetUserRatings.add(rec.getUserRating());
            orderedAppNames.add(rec.getRecommendation());
            Recommendation tempRec = userData.getRecommendations().stream().filter(recommendation -> recommendation.getRecommendation().equals(rec.getRecommendation())).findFirst().orElse(null);
            if(tempRec!=null){
                userDataRatings.add(tempRec.getUserRating());
            }
            else{
                String appN = userData.getApps().stream().filter(appName -> appName.equals(rec.getRecommendation())).findFirst().orElse(null);
                if(appN==null) {
                    userDataRatings.add(0);
                }
                else{
                    userDataRatings.add(2);
                }
            }
        }
        //Same as above loop but now we insert the apps that userData has
        //and target user doesn't
        for(int i=0;i< userData.getRecommendations().size();i++){
            Recommendation rec = userData.getRecommendations().get(i);
            if(orderedAppNames.contains(rec.getRecommendation())){
                continue;
            }
            userDataRatings.add(rec.getUserRating());
            orderedAppNames.add(rec.getRecommendation());
            String appN = targetUser.getApps().stream().filter(app -> app.equals(rec.getRecommendation())).findFirst().orElse(null);
            if(appN==null) {
                targetUserRatings.add(0);
            }
            else{
                targetUserRatings.add(2);
            }
        }
        //Find correlation between two users
        //Add the userData into similarUsers if correlation is above a threshold
        Collections.replaceAll(targetUserRatings,null,0);
        Collections.replaceAll(userDataRatings,null,0);
        double[] targetUserRatingsDouble = targetUserRatings.stream().mapToDouble(d -> d).toArray();
        double[] userDataRatingsDouble = userDataRatings.stream().mapToDouble(d -> d).toArray();
        return (float)pearsonsCorrelation.correlation(targetUserRatingsDouble,userDataRatingsDouble);
    }
    //Generate a recommendation from the correlatedUsers that were found
    private static Recommendation generateRecommendation(List<CorrelationObject> correlationObjectList,UserData targetUser){
        //Return null if there are no correlated users
        if(correlationObjectList.size()==0){
            return null;
        }
        Recommendation recommendation = new Recommendation();
        //Make recommendation by getting the sum of the correlations of users for all the apps
        //that the target user doesn't have installed/recommended and find the one that has
        //the highest sum. Apps are only added from users that have a possitive correlation.
        //Users with negative correlations are used to lower the sum of those apps in case
        //they also use the app. This is so we can filter out apps that both similar and not
        //similar users use.
        List<String> appsTargetUserDoesntHave = new ArrayList<>();
        List<Double> sumOfCorrelationsOfUsersForApp = new ArrayList<>();
        List<Integer> countOfAppOccurrences = new ArrayList<>();
        List<String> alreadyRecommended = targetUser.getRecommendations().stream().map(Recommendation::getRecommendation).collect(Collectors.toList());
        for(CorrelationObject co : correlationObjectList){
            for(String appName:co.getUserData().getApps()){
                if(!targetUser.getApps().contains(appName)
                        && !alreadyRecommended.contains(appName)){
                    if(appsTargetUserDoesntHave.contains(appName)){
                        int index = appsTargetUserDoesntHave.indexOf(appName);
                        Double oldCorrSum = sumOfCorrelationsOfUsersForApp.get(index);
                        sumOfCorrelationsOfUsersForApp.set(index, oldCorrSum + co.getCorrelation());
                        countOfAppOccurrences.set(index, countOfAppOccurrences.get(index) + 1);
                    }
                    else {
                        if(co.getCorrelation()>0.0) {
                            appsTargetUserDoesntHave.add(appName);
                            sumOfCorrelationsOfUsersForApp.add(co.getCorrelation());
                            countOfAppOccurrences.add(1);
                        }
                    }
                }
            }
        }
        //If no apps are found, because targetUser already has them all installed or recommended
        //return null
        if(appsTargetUserDoesntHave.size()==0)
            return null;
        String bestMatch = appsTargetUserDoesntHave.get(sumOfCorrelationsOfUsersForApp.indexOf(Collections.max(sumOfCorrelationsOfUsersForApp)));
        double count = correlationObjectList.stream().filter(correlationObject -> correlationObject.getCorrelation() > 0.0 && correlationObject.getUserData().getApps().contains(bestMatch)).count();
        long size = correlationObjectList.stream().filter(correlationObject -> correlationObject.getCorrelation() > 0.0).count();
        long percent = Math.round((count / size)*100);
        if(percent<25)
            return null;
        recommendation.setRecommendation(bestMatch);
        recommendation.setRecommendationType("App");
        recommendation.setUserData(targetUser);
        recommendation.setPercentOfSimilarUsersWithApp(Math.toIntExact(percent));
        return recommendation;
    }

    private static class CorrelationObject{
        private final UserData userData;
        private final double correlation;
        protected CorrelationObject(UserData userData,double correlation){
            this.userData = userData;
            this.correlation = correlation;
        }

        public UserData getUserData() {
            return userData;
        }

        public double getCorrelation() {
            return correlation;
        }
    }
}
