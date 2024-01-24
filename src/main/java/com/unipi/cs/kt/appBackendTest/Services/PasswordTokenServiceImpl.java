package com.unipi.cs.kt.appBackendTest.Services;

import com.unipi.cs.kt.appBackendTest.Repositories.PasswordTokenRepository;
import com.unipi.cs.kt.appBackendTest.DataClasses.AppUser;
import com.unipi.cs.kt.appBackendTest.DataClasses.PasswordResetToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class PasswordTokenServiceImpl implements PasswordTokenService {
    private final PasswordTokenRepository passwordTokenRepository;

    @Autowired
    public PasswordTokenServiceImpl(PasswordTokenRepository passwordTokenRepository){
        this.passwordTokenRepository = passwordTokenRepository;
    }

    @Override
    public void createPasswordResetToken(AppUser appUser, String token) {
        PasswordResetToken passwordToken = new PasswordResetToken(token,appUser);
        passwordTokenRepository.save(passwordToken);
    }

    @Override
    public PasswordResetToken getPasswordResetToken(String token) {
        return passwordTokenRepository.findByToken(token);
    }

    @Override
    public List<PasswordResetToken> getTokens() {
        return passwordTokenRepository.findAll();
    }

    @Override
    public void deleteToken(PasswordResetToken passwordResetToken) {
        passwordTokenRepository.deleteById(passwordResetToken.getId());
    }
}
