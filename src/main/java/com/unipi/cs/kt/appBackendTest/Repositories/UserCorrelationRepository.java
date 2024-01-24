package com.unipi.cs.kt.appBackendTest.Repositories;

import com.unipi.cs.kt.appBackendTest.DataClasses.UserCorrelation;
import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCorrelationRepository extends JpaRepository<UserCorrelation,Long> {
    List<UserCorrelation> findAllByPrimaryUser(UserData primaryUser);
    List<UserCorrelation> findAllByOtherUser(UserData otherUser);
}
