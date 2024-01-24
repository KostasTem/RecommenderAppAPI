package com.unipi.cs.kt.appBackendTest.Repositories;

import com.unipi.cs.kt.appBackendTest.DataClasses.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
}
