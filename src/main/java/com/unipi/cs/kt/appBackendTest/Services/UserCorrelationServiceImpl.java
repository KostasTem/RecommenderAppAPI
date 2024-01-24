package com.unipi.cs.kt.appBackendTest.Services;

import com.unipi.cs.kt.appBackendTest.DataClasses.UserCorrelation;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import com.unipi.cs.kt.appBackendTest.Repositories.UserCorrelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class UserCorrelationServiceImpl implements UserCorrelationService {

    private final UserCorrelationRepository userCorrelationRepository;

    @Autowired
    public UserCorrelationServiceImpl(UserCorrelationRepository userCorrelationRepository){
        this.userCorrelationRepository = userCorrelationRepository;
    }

    @Override
    public UserCorrelation saveUserCorrelation(UserCorrelation userCorrelation) {
        return userCorrelationRepository.save(userCorrelation);
    }
    //Get a list of all user correlations userData is part of
    @Override
    public List<UserCorrelation> getCorrelationsForUser(UserData userData) {
        List<UserCorrelation> userCorrelations = userCorrelationRepository.findAllByPrimaryUser(userData);
        userCorrelations.addAll(userCorrelationRepository.findAllByOtherUser(userData));
        return userCorrelations;
    }

    @Override
    public void deleteUserCorrelation(Long id) {
        userCorrelationRepository.deleteById(id);
    }
}
