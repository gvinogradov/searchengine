package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.config.SearchCfg;

public interface SearchService {
    ResponseEntity<?> search(SearchCfg searchCfg);

}
