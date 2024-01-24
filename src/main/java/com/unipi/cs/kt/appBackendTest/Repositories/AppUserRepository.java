package com.unipi.cs.kt.appBackendTest.Repositories;

import com.unipi.cs.kt.appBackendTest.DataClasses.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByUsername(String username);
    AppUser findByEmail(String email);
    AppUser findByid(Long id);
}
