package com.unipi.cs.kt.appBackendTest.Services;

import com.unipi.cs.kt.appBackendTest.Repositories.UserDataRepository;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class UserDataServiceImpl implements UserDataService{

    private final UserDataRepository userDataRepository;

    @Autowired
    public UserDataServiceImpl(UserDataRepository userDataRepository){
        this.userDataRepository = userDataRepository;
    }

    @Override
    public UserData saveData(UserData userData) {
        return userDataRepository.save(userData);
    }

    @Override
    public UserData getData(String userID) {
        return userDataRepository.findByUid(userID);
    }

    @Override
    public List<UserData> getAllData() {
        return userDataRepository.findAll();
    }
}
