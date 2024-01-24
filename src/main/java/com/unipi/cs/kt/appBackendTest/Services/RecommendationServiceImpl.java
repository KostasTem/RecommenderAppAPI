package com.unipi.cs.kt.appBackendTest.Services;

import com.unipi.cs.kt.appBackendTest.DataClasses.Recommendation;
import com.unipi.cs.kt.appBackendTest.Repositories.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationRepository recommendationRepository;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository recommendationRepository){
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public Recommendation saveRecommendation(Recommendation recommendation) {
        return recommendationRepository.save(recommendation);
    }

    @Override
    public Recommendation getRecommendationByID(Long id) {
        Optional<Recommendation> recommendation = recommendationRepository.findById(id);
        return recommendation.orElse(null);
    }

    @Override
    public List<Recommendation> getRecommendationsByUID(String uid) {
        return recommendationRepository.findByUserDataUid(uid);
    }

    @Override
    public List<Recommendation> getRecommendations() {
        return recommendationRepository.findAll();
    }
}
