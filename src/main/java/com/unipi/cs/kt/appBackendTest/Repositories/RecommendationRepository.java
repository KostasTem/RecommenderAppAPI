package com.unipi.cs.kt.appBackendTest.Repositories;

import com.unipi.cs.kt.appBackendTest.DataClasses.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation,Long> {
    List<Recommendation> findByUserDataUid(String uid);
}
