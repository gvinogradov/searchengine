package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.config.SearchCfg;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Lemma;

import java.util.List;

public interface SearchService {
    ResponseEntity<?> search(SearchCfg searchCfg);
    List<Lemma> getLemmas(SearchCfg searchCfg);
}
