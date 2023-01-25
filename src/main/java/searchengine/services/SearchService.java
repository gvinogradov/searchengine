package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.config.SearchCfg;
import searchengine.dto.search.SearchResponse;

public interface SearchService {
    ResponseEntity<?> search(SearchCfg searchCfg);
}
