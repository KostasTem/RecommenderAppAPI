package com.unipi.cs.kt.appBackendTest.Services;

import com.unipi.cs.kt.appBackendTest.DataClasses.AppUser;
import com.unipi.cs.kt.appBackendTest.DataClasses.PasswordResetToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PasswordTokenService {
    void createPasswordResetToken(AppUser appUser, String token);
    PasswordResetToken getPasswordResetToken(String token);
    List<PasswordResetToken> getTokens();
    void deleteToken(PasswordResetToken passwordResetToken);
}
