package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.SearchCfg;
import searchengine.dto.search.SearchError;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;
import searchengine.utils.LemmaFinder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SearchCfg defaultSearchCfg;
    private final LemmaRepository lemmaRepository;
    private final SiteService siteService;

    @Override
    public ResponseEntity<?> search(SearchCfg searchCfg) {
        searchCfg.setTreshhold(defaultSearchCfg.getTreshhold());
        if (searchCfg.getQuery() == null) {
            return new ResponseEntity<>(new SearchError(false, "Задан пустой поисковый запрос"),
                    HttpStatus.NOT_FOUND);
        }
        if (searchCfg.getLimit() == 0) {
            searchCfg.setLimit(defaultSearchCfg.getLimit());
        }
        try {
            LemmaFinder lemmaFinder = LemmaFinder.getInstance();
            Set<String> lemmaSet = lemmaFinder.getLemmaSet(searchCfg.getQuery());

            Site site = (searchCfg.getSite() != null) ?
                    siteService.getByUrl(searchCfg.getSite()) : null;
            List<Lemma> lemmas = (site == null) ?
                    lemmaRepository.getLemmasByArray(lemmaSet, searchCfg.getTreshhold()) :
                    lemmaRepository.getLemmasByArrayAndSite(lemmaSet, searchCfg.getTreshhold(), site.getId());

            lemmas.sort(Lemma::compareTo);

            lemmas.forEach(System.out::println);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        SearchResponse response = new SearchResponse();
        response.setResult(true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
