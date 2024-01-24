package com.unipi.cs.kt.appBackendTest.Services;

import com.unipi.cs.kt.appBackendTest.DataClasses.Recommendation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface RecommendationService {
    Recommendation saveRecommendation(Recommendation recommendation);
    Recommendation getRecommendationByID(Long id);
    List<Recommendation> getRecommendationsByUID(String uid);
    List<Recommendation> getRecommendations();
}