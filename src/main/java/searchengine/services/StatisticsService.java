package searchengine.services;

import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Site;

import java.util.List;

public interface StatisticsService {
    StatisticsResponse getStatistics();
}
