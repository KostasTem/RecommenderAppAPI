package com.unipi.cs.kt.appBackendTest.Controllers;

import com.unipi.cs.kt.appBackendTest.DataClasses.Recommendation;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserCorrelation;
import com.unipi.cs.kt.appBackendTest.Services.RecommendationService;
import com.unipi.cs.kt.appBackendTest.Services.UserCorrelationService;
import com.unipi.cs.kt.appBackendTest.Services.UserDataService;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import com.unipi.cs.kt.appBackendTest.Utils.RecommendationLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.unipi.cs.kt.appBackendTest.Utils.GlobalVars;

@RestController
@RequestMapping("/api/data")
public class UserDataController {

    private static final Logger Log = LoggerFactory.getLogger(UserDataController.class);
    private final UserDataService userDataService;
    private final RecommendationService recommendationService;
    private final UserCorrelationService userCorrelationService;


    @Autowired
    public UserDataController(UserDataService userDataService, RecommendationService recommendationService,UserCorrelationService userCorrelationService){
        this.userDataService = userDataService;
        this.recommendationService = recommendationService;
        this.userCorrelationService = userCorrelationService;
    }
    //Upload user data every time a user logs in
    @PostMapping("/updateApps")
    public ResponseEntity<Map<String,String>> updateApps(@RequestBody Map<String,Object> payload) {
        Map<String,String> res = new HashMap<>();
        String userID = (String)payload.get("userID");
        List<String> apps = (List<String>) payload.get("appList");
        List<String> newApps = new ArrayList<>();
        for (String app : apps) {
            if (!GlobalVars.excludedApps.contains(app) && checkIfEnglish(app)) {
                newApps.add(app);
            }
        }
        UserData userData = userDataService.getData(userID);
        updateUserLastInteraction(userData);
        userData.setApps(newApps);
        if(userDataService.saveData(userData)==userData) {
            for (String setting : GlobalVars.settings) {
                Recommendation rec = userData.getRecommendations().stream().filter(recommendation -> recommendation.getRecommendation().equals(setting)).findFirst().orElse(null);
                if(rec!=null){
                    Integer rating = null;
                    if(payload.containsKey(setting)) {
                        if(payload.get(setting).equals("null")) {
                            Log.info("Setting {} was null for user {}",setting,userData.getUid());
                            rating = 0;
                        }
                        else {
                            rating = (Integer) payload.get(setting);
                        }
                    }
                    else{
                        rating = 0;
                    }
                    rec.setUserRating(rating);
                    if(recommendationService.saveRecommendation(rec)==rec){
                        Log.info("Setting {} for user {} stored successfully",setting,userData.getUid());
                    }
                    else{
                        Log.error("Setting {} for user {} wasn't stored",setting,userData.getUid());
                    }
                }
                else{
                    Log.error("Setting {} doesn't exist for user {}",setting,userData.getUid());
                }
            }
            //This works because when calculating the correlation of a user with other
            //users, any users who haven't finished the initial rating process are ignored.
            //So the correlation of those users will be calculated only when they finish their
            //initial ratings since after they finish that process they send a request to this
            //endpoint and this check is run.
            if(userCorrelationService.getCorrelationsForUser(userData).size()==0) {
                generateFirstCorrelationsForUser(userData);
            }
            res.put("Status","Success");
            Log.info("User apps saved successfully. User: {}",userID);
            return ResponseEntity.ok().body(res);
        }
        else{
            res.put("Error","User Not Saved");
            Log.error("Error saving apps for user {}",userID);
            return ResponseEntity.badRequest().body(res);
        }
    }
    //Insert a new user data object into the database when a new user registers
    @PostMapping("/insertNewUser")
    public ResponseEntity<Map<String,String>> insertNewUser(@RequestBody Map<String,String> payload){
        Map<String,String> res = new HashMap<>();
        String uid = clearSpecialCharacters(payload.get("userID"));
        if(userDataService.getData(uid)==null){
            UserData userData = new UserData(uid);
            if(userDataService.saveData(userData)!=null){
                Log.info("User {} inserted successfully", userData.getUid());
                res.put("Status","Success");
                return ResponseEntity.ok().body(res);
            }
            else {
                Log.error("Couldn't insert user {}", uid);
                res.put("Error","Couldn't save user");
                return ResponseEntity.internalServerError().body(res);
            }
        }
        else {
            res.put("Error","User already exists");
            Log.error("Error during insertion of new user with uid: {}",uid);
            return ResponseEntity.badRequest().body(res);
        }
    }
    //Get a list of all the recommendations of a specific user data object
    @GetMapping("/getAllRecommendations/{uid}")
    public ResponseEntity<Map<String,Object>> getRecommendations(@PathVariable String uid){
        Map<String,Object> response = new HashMap<>();
        List<Recommendation> recommendations;
        UserData thisUser = userDataService.getData(uid);
        if(thisUser!=null) {
            updateUserLastInteraction(thisUser);
            recommendations = thisUser.getRecommendations();
            recommendations.forEach(recommendation -> recommendation.setUserData(null));
            response.put("Recommendations", recommendations.stream().filter(recommendation -> recommendation.getRecommendationType().equals("App")).collect(Collectors.toList()));
            Log.info("Recommendations for user {} retrieved",thisUser.getUid());
            return ResponseEntity.ok().body(response);
        }
        else{
            response.put("Error","User Not Found");
            Log.error("Error getting all recommendations. User not found. User ID {}", clearSpecialCharacters(uid));
            return ResponseEntity.badRequest().body(response);
        }
    }
    //Update the rating of a specific recommendation for a user
    @PatchMapping("/updateRating")
    public ResponseEntity<Map<String,String>> updateRecommendationStatus(@RequestBody Map<String,Integer> payload){
        Map<String,String> res = new HashMap<>();
        Integer temp = payload.get("id");
        Long recommendationID = temp.longValue();
        Recommendation recommendation = recommendationService.getRecommendationByID(recommendationID);
        if(recommendation!=null) {
            updateUserLastInteraction(recommendation.getUserData());
            Integer userRating = payload.get("rating");
            recommendation.setUserRating(userRating);
            if(recommendationService.saveRecommendation(recommendation)!=null) {
                res.put("Status", "Updated");
                Log.info("Updated rating for recommendation. ID: {}, Recommendation: {}, Rating: {}", recommendation.getId(), recommendation.getRecommendation(),userRating);
                return ResponseEntity.ok().body(res);
            }
            else {
                res.put("Error","Error updating recommendation rating");
                Log.error("Error updating recommendation rating in DB ID: {}, Recommendation: {}, Rating: {}", recommendation.getId(), recommendation.getRecommendation(),userRating);
                return ResponseEntity.badRequest().body(res);
            }
        }
        else{
            res.put("Error","Recommendation Not Found");
            Log.error("Error updating recommendation rating. Recommendation not found. ID: {}",recommendationID);
            return ResponseEntity.badRequest().body(res);
        }
    }
    //Get count of recommendations user has rated to figure
    // out if they still need to rate the base recommendations
    @GetMapping("/getRecommendationCount/{uid}")
    public ResponseEntity<Map<String,Object>> getRecommendationCountForUser(@PathVariable String uid){
        Map<String,Object> response = new HashMap<>();
        UserData userData = userDataService.getData(uid);
        if(userData!=null) {
            updateUserLastInteraction(userData);
            int count = 0;
            if (userData.getRecommendations().size() == GlobalVars.BASE_RECOMMENDATION_APP_COUNT) {
                for (int i=GlobalVars.BASE_RECOMMENDATION_APP_COUNT-10;i<GlobalVars.BASE_RECOMMENDATION_APP_COUNT;i++) {
                    Recommendation rec = userData.getRecommendations().get(i);
                    if (rec.getUserRating() == null) {
                        count += 1;
                    }
                }
                count = 10 - count;
            } else {
                count = userData.getRecommendations().size();
            }
            response.put("count", count);
            Log.info("Counted recommendations for User: {}. Recommendation Count: {}",userData.getUid(),count);
            return ResponseEntity.ok().body(response);
        }
        else{
            response.put("Error","User Not Found");
            Log.error("Error counting recommendations User: {} not found",clearSpecialCharacters(uid));
            return ResponseEntity.badRequest().body(response);
        }
    }
    //Insert the initial ratings for the base applications the user rates on first login
    @PatchMapping("/insertInitialRatings")
    public ResponseEntity<Map<String,String>> insertRating(@RequestBody Map<String,Object> payload){
        int rating = (int) payload.get("rating");
        Map<String,String> res = new HashMap<>();
        String app = (String) payload.get("appName");
        String userID = (String) payload.get("userID");
        UserData thisUser = userDataService.getData(userID);
        Recommendation rec = thisUser!=null ? thisUser.getRecommendations().stream().filter(recommendation -> recommendation.getRecommendation().equals(app)).findFirst().orElse(null) : null;
        if(rec!=null){
            updateUserLastInteraction(thisUser);
            rec.setUserRating(rating);
            if(recommendationService.saveRecommendation(rec)!=null) {
                res.put("Status","Success");
                Log.info("Initial Recommendation inserted successfully. User: {}, App Name: {}, Rating: {}", clearSpecialCharacters(userID), clearSpecialCharacters(app), rating);
                return ResponseEntity.ok().body(res);
            }
            else{
                res.put("Error","Failed to update the recommendation rating");
                Log.error("Error updating rating for initial recommendation. User: {}, App Name: {}, Rating: {}",clearSpecialCharacters(userID), clearSpecialCharacters(app), rating);
                return ResponseEntity.badRequest().body(res);
            }
        }
        else{
            res.put("Error","Recommendation Not Found");
            Log.error("Error inserting initial recommendation. User: {}, App Name: {}, Rating: {}",clearSpecialCharacters(userID), clearSpecialCharacters(app), rating);
            return ResponseEntity.badRequest().body(res);
        }
    }
    //Generate new recommendation for a user
    @PostMapping("/getNewRecommendation")
    public ResponseEntity<Map<String,Object>> getNewRecommendation(@RequestBody Map<String,String> payload){
        Map<String,Object> res = new HashMap<>();
        String uid = payload.get("userID");
        UserData userData = userDataService.getData(uid);
        if(userData!=null){
            updateUserLastInteraction(userData);
            Recommendation newRecommendation = null;
            try {
                List<UserCorrelation> userCorrelationList = userCorrelationService.getCorrelationsForUser(userData);
                newRecommendation = RecommendationLogic.createRecommendation(userCorrelationList, userData);
                //String appName = newRecommendation.getRecommendation();
                //double amountOfUsersWithApp = (double) userDataService.getAllData().stream().filter(userData1 -> userData1.getApps().contains(appName)).count();
                //double percent = amountOfUsersWithApp / userDataService.getAllData().size();
            }
            catch (Exception e){
                Log.error("Exception in RecommendationLogic {}",e.getMessage());
            }
            if(newRecommendation!=null){
                res.put("Status","Created");
                res.put("Recommendation",newRecommendation);
                Log.info("Created new recommendation: {} for user: {}",newRecommendation.getRecommendation(),userData.getUid());
                recommendationService.saveRecommendation(newRecommendation);
            }
            else{
                res.put("Status","None");
                Log.error("No recommendations available for user {}", userData.getUid());
            }
            return ResponseEntity.ok().body(res);
        }
        else{
            res.put("Error","User ID isn't assigned to any users");
            Log.error("Can't create recommendation because user with user id {} doesn't exist", clearSpecialCharacters(uid));
            return ResponseEntity.badRequest().body(res);
        }
    }
    //Endpoint to insert 250 random users for testing
    @GetMapping("/test/createTestUsers")
    public void createUsers(){
        List<String> appNames = Arrays.asList("Instagram","Messenger","Netflix","WhatsApp Messenger","Disney+","Snapchat","Discord","PayPal","ZOOM Cloud Meetings","Booking.com","Ticketmaster","eBay","Uber","Venmo","eFood","Duolingo","Viber","Skype","Signal","VLC","LinkedIn","Glassdoor","Sololearn","Firefox","MyFitnessPal","IMDB","Shazam","Skroutz","Amazon","Viva Wallet","TikTok","Xbox","Revolut","GitHub","LastPass","Word","Airbnb","Audible","Clash of Clans","RAID: Shadow Legends","Candy Crush","League Of Legends: Wild Rift","Teamfight Tactics","Excel","Powerpoint","Photoshop");
        for(int i=0;i<250;i++){
            List<String> tempAppNames = new ArrayList<>();
            for(int j = 0; j< nextInt(10,25); j++){
                List<String> res = getAppAndPackageNames(appNames);
                while (tempAppNames.contains(res.get(0))){
                    res = getAppAndPackageNames(appNames);
                }
                tempAppNames.add(res.get(0));
            }
            UserData temp = new UserData(UUID.randomUUID().toString());
            temp.setApps(tempAppNames);
            for(Recommendation rec: temp.getRecommendations()){
                if(rec.getRecommendationType().equals("Setting")){
                    rec.setUserRating(nextInt(0,2) == 0 ? 1 : 3);
                }
                else {
                    rec.setUserRating(nextInt(0, 4));
                }
            }
            generateFirstCorrelationsForUser(temp);
            userDataService.saveData(temp);
        }
    }
    private List<String> getAppAndPackageNames(List<String> appNames){
        List<String> last = new ArrayList<>();
        int count = nextInt(0,46);
        last.add(appNames.get(count));
        return last;
    }
    private int nextInt(int low,int high){
        Random r = new Random();
        return r.nextInt(high-low) + low;
    }
    //Update the last interaction of a user data object with the API
    private void updateUserLastInteraction(UserData userData){
        userData.setLastSeen(System.currentTimeMillis());
        userDataService.saveData(userData);
    }
    //Generate correlations of targetUser with every other user after they finish the initial ratings
    private void generateFirstCorrelationsForUser(UserData targetUser){
        List<UserData> otherUsers = userDataService.getAllData();
        otherUsers.remove(otherUsers.stream().filter(userData -> userData.getUid().equals(targetUser.getUid())).findFirst().orElse(null));
        if(otherUsers.size()==0) {
            return;
        }
        List<UserCorrelation> userCorrelations = RecommendationLogic.findCorrelations(otherUsers, targetUser);
        for (UserCorrelation userCorrelation : userCorrelations) {
            userCorrelationService.saveUserCorrelation(userCorrelation);
        }
    }

    private boolean checkIfEnglish(String app){
        return app.matches("^[A-Za-z1-9 ]*$");
    }

    private String clearSpecialCharacters(String target){
        return target.replaceAll("[!@#$%^&*();{}\"~`'/.,]","");
    }
}
