package com.unipi.cs.kt.appBackendTest.Services;

import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserDataService {
    UserData saveData(UserData userData);
    UserData getData(String userID);
    List<UserData> getAllData();
}
