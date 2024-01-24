package com.unipi.cs.kt.appBackendTest.Controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.cs.kt.appBackendTest.Services.AppUserService;
import com.unipi.cs.kt.appBackendTest.Services.PasswordTokenService;
import com.unipi.cs.kt.appBackendTest.DataClasses.AppUser;
import com.unipi.cs.kt.appBackendTest.DataClasses.PasswordResetToken;
import com.unipi.cs.kt.appBackendTest.Utils.SendEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
@RestController
@RequestMapping("/api/user")
public class AppUserController {

    private static final Logger Log = LoggerFactory.getLogger(AppUserController.class);
    private final AppUserService appUserService;
    private final PasswordTokenService passwordTokenService;
    private final JavaMailSender javaMailSender;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    public AppUserController(AppUserService appUserService, PasswordTokenService passwordTokenService,JavaMailSender javaMailSender){
        this.appUserService = appUserService;
        this.passwordTokenService = passwordTokenService;
        this.javaMailSender = javaMailSender;
    }
    //Register new AppUser
    @PostMapping("/register")
    public ResponseEntity<Map<String,String>> register(@RequestBody Map<String,String> payload){
        Map<String, String> st = new HashMap<>();
        try {
            AppUser user = new AppUser(null, payload.get("Email"), payload.get("Username"), payload.get("Password"),payload.get("imageName"));
            if(appUserService.getUser(payload.get("Username"))!=null){
                st.put("Error","Username is already in use.");
                Log.error("User tried to register with taken username: {}", payload.get("Username"));
                return ResponseEntity.badRequest().body(st);
            }
            if(appUserService.getUserByEmail(payload.get("Email"))!=null){
                st.put("Error","Email is already in use");
                Log.error("User tried to register with taken email: {}", payload.get("Email"));
                return ResponseEntity.badRequest().body(st);
            }
            if(payload.get("image")!=null){
                byte[] file = Base64.getDecoder().decode(payload.get("image"));
                String p = getClass().getClassLoader().getResource("user_images").getPath();
                p = p + "/" + payload.get("imageName");
                //Windows
                Path filepath = Paths.get(p.substring(p.indexOf("/") + 1));
                //Linux
                //Path filepath = Paths.get(p);
                try (OutputStream os = Files.newOutputStream(filepath)) {
                    os.write(file);
                }
            }
            if(appUserService.saveUser(user,true)==user) {
                st.put("Status","Registration Successful");
                Log.info("User {} registered successfully", user.getUsername());
                return ResponseEntity.ok().body(st);
            }
            else{
                st.put("Error","Error saving user information in the database");
                Log.error("Error registering user {} in the database", user.getUsername());
                return ResponseEntity.internalServerError().body(st);
            }
        }
        catch (Exception exception){
            Log.error("Exception during registration: {}",exception.getMessage());
            st.put("Error","There was an error with your request.");
            return ResponseEntity.internalServerError().body(st);
        }
    }
    //Get logged in user's data to fill UI
    @GetMapping("/getUserData")
    public ResponseEntity<Map<String,String>> getLoggedInUserData(Authentication authentication) {
        Map<String,String> st = new HashMap<>();
        AppUser appUser = appUserService.getUser(authentication.getPrincipal().toString());
        if(appUser.getImagePath()!=null) {
            String p = getClass().getClassLoader().getResource("user_images").getPath();
            p = p + "/" + appUser.getImagePath();
            //Windows
            Path filepath = Paths.get(p.substring(p.indexOf("/") + 1));
            //Linux
            //Path filepath = Paths.get(p);
            try (InputStream is = Files.newInputStream(filepath)) {
                byte[] image = is.readAllBytes();
                String imString = Base64.getEncoder().encodeToString(image);
                st.put("image", imString);
            } catch (IOException e) {
                st.put("image",null);
                e.printStackTrace();
            }
        }
        else st.put("image",null);
        if(appUser.getUserID()!=null) st.put("userID",appUser.getUserID());
        else st.put("userID",null);
        st.put("username",appUser.getUsername());
        st.put("email",appUser.getEmail());
        Log.info("User {} fetched their data after login",appUser.getUsername());
        return ResponseEntity.ok().body(st);
    }
    //Refresh user access token
    @PostMapping("/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();
                AppUser appUser = appUserService.getUser(username);
                String access_token = JWT.create()
                        .withSubject(appUser.getUsername())
                        .withExpiresAt(new Date(System.currentTimeMillis() + 1440 * 60 * 1000))
                        .withIssuer((request.getRequestURL().toString()))
                        .withClaim("roles", List.of(appUser.getRole()))
                        .sign(algorithm);
                Map<String,String> tokens = new HashMap<>();
                tokens.put("access_token",access_token);
                tokens.put("refresh_token",refresh_token);
                response.setContentType(APPLICATION_JSON_VALUE);
                Log.info("User {} refreshed their access token",appUser.getUsername());
                new ObjectMapper().writeValue(response.getOutputStream(),tokens);
            }
            catch(Exception exception) {
                response.setHeader("error","Couldn't refresh token");
                response.setStatus(FORBIDDEN.value());
                Map<String,String> error = new HashMap<>();
                error.put("Error", "Your token has expired.");
                response.setContentType(APPLICATION_JSON_VALUE);
                Log.error("Error refreshing access token. Message: {}", exception.getMessage());
                new ObjectMapper().writeValue(response.getOutputStream(),error);
            }
        }
        else{
            response.setStatus(FORBIDDEN.value());
            Map<String,String> error = new HashMap<>();
            error.put("Error", "Refresh Token Missing");
            response.setContentType(APPLICATION_JSON_VALUE);
            Log.error("Refresh Token Missing");
            new ObjectMapper().writeValue(response.getOutputStream(),error);
        }
    }
    //Send email containing a password reset link to users registered email if a password reset request doesn't already exist for them
    @PostMapping("/sendResetEmail")
    public ResponseEntity<Map<String,String>> createAndSendReset(@RequestBody Map<String,String> payload) {
        Map<String, String> res = new HashMap<>();
        String email = payload.get("email");
        AppUser appUser = appUserService.getUserByEmail(email);
        if (appUser == null) {
            res.put("Error", "No user with this email.");
            Log.error("Tried to reset password for email that isn't registered");
            return ResponseEntity.badRequest().body(res);
        } else {
            List<PasswordResetToken> passwordResetTokens = passwordTokenService.getTokens();
            List<String> tokens = passwordResetTokens.stream().map(PasswordResetToken::getToken).collect(Collectors.toList());
            for (PasswordResetToken prt : passwordResetTokens) {
                if (prt.getUser().getEmail().equals(email) && !prt.isExpired()) {
                    res.put("Error", "Request Already Exists.");
                    Log.error("Tried to reset password when non expired PasswordResetToken already exists for email {}", appUser.getEmail());
                    return ResponseEntity.badRequest().body(res);
                }
            }
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            String token = encoder.encodeToString(bytes);
            while (tokens.contains(token)) {
                random.nextBytes(bytes);
                encoder = Base64.getUrlEncoder().withoutPadding();
                token = encoder.encodeToString(bytes);
            }
            passwordTokenService.createPasswordResetToken(appUser, token);
            res.put("Status", "Request Submitted");
            Log.info("Password reset request submitted successfully for email: {}", appUser.getEmail());
            Thread emailThread = new Thread(new SendEmail(javaMailSender,email,token));
            emailThread.start();
            return ResponseEntity.ok().body(res);
        }
    }
    //GET password reset web page
    @GetMapping("/resetPassword")
    public ModelAndView getResetPasswordPage(@RequestParam("token") String token, Model model,ModelAndView modelAndView){
        PasswordResetToken passwordResetToken = passwordTokenService.getPasswordResetToken(token);
        if(passwordResetToken!=null && !passwordResetToken.isExpired()){
            modelAndView.setViewName("resetPage");
            modelAndView.addObject("token",token);

        }
        else{
            modelAndView.setViewName("errorPage");
        }
        return modelAndView;
    }

    //POST Request from resetPage to complete password reset
    @PostMapping("/resetPassword")
    public ModelAndView resetPasswordPost(@RequestParam String token,@RequestParam String password, ModelAndView modelAndView){
        PasswordResetToken passwordResetToken = passwordTokenService.getPasswordResetToken(token);
        if(passwordResetToken!=null && !passwordResetToken.isExpired()) {
            AppUser appUser = passwordResetToken.getUser();
            appUser.setPassword(password);
            Log.info("User {} reset password successfully",appUser.getUsername());
            appUserService.saveUser(appUser,true);
            passwordTokenService.deleteToken(passwordResetToken);
            modelAndView.setViewName("resetSuccess");
        }
        else{
            Log.error("Error while resetting the password of user {}",passwordResetToken.getUser().getUsername());
            modelAndView.setViewName("errorPage");
        }
        return modelAndView;
    }
    //Sync or unsync AppUser with UserData
    @PostMapping("/syncDataWithUser")
    public ResponseEntity<Map<String,String>> syncDataWithUser(@RequestBody Map<String,String> payload,Authentication authentication){
        Map<String,String> res = new HashMap<>();
        AppUser appUser = appUserService.getUser(authentication.getPrincipal().toString());
        if(payload.containsKey("userID")) {
            String userID = payload.get("userID");
            if (appUser != null) {
                appUser.setUserID(userID);
                if (appUserService.saveUser(appUser,false) == appUser) {
                    res.put("Status", "Data Synced");
                    Log.info("Successfully synced account {} with anonymous data", appUser.getUsername());
                    return ResponseEntity.ok().body(res);
                } else {
                    res.put("Error", "Couldn't save the update to your account. Try again later");
                    Log.error("Error saving user {} to the database after setting their user id", appUser.getUsername());
                    return ResponseEntity.internalServerError().body(res);
                }
            } else {
                res.put("Error", "Couldn't find your user account. Try again later");
                Log.error("Couldn't find the user account for the user trying to sync their data with their account");
                return ResponseEntity.internalServerError().body(res);
            }
        }
        else{
            if (appUser != null) {
                appUser.setUserID(null);
                if (appUserService.saveUser(appUser,false) == appUser) {
                    res.put("Status", "Data Unsynced");
                    Log.info("Successfully unsynced account {} with anonymous data", appUser.getUsername());
                    return ResponseEntity.ok().body(res);
                } else {
                    res.put("Error", "Couldn't save the update to your account. Try again later");
                    Log.error("Error saving user {} to the database after removing their user id", appUser.getUsername());
                    return ResponseEntity.internalServerError().body(res);
                }
            } else {
                res.put("Error", "Couldn't find your user account. Try again later");
                Log.error("Couldn't find the user account for the user trying to unsync their data from their account");
                return ResponseEntity.internalServerError().body(res);
            }
        }
    }
}
