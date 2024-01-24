package com.unipi.cs.kt.appBackendTest.Repositories;

import com.unipi.cs.kt.appBackendTest.DataClasses.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDataRepository extends JpaRepository<UserData,String> {
    UserData findByUid(String uid);
}
