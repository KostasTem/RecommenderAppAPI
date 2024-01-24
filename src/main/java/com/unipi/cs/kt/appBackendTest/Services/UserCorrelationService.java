package com.unipi.cs.kt.appBackendTest.Services;

import com.unipi.cs.kt.appBackendTest.DataClasses.UserCorrelation;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserCorrelationService{
    UserCorrelation saveUserCorrelation(UserCorrelation userCorrelation);
    List<UserCorrelation> getCorrelationsForUser(UserData UserData);
    void deleteUserCorrelation(Long id);
}
