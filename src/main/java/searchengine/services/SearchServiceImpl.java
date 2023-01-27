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

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SearchCfg defaultSearchCfg;
    private final LemmaService lemmaService;
    private final SiteService siteService;
    private final MorphologyService morphologyService;

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

        List<Lemma> lemmas = getLemmas(searchCfg);

        SearchResponse response = new SearchResponse();
        response.setResult(true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public List<Lemma> getLemmas(SearchCfg searchCfg) {
        List<Lemma> lemmas;
        try {
            Map<String, Integer> queryLemmas = morphologyService.collectLemmas(searchCfg.getQuery());

            if (searchCfg.getSite() == null) {
                lemmas = lemmaService.getSortedFoundList(queryLemmas.keySet(), searchCfg.getTreshhold());
            } else {
                Site site = siteService.getByUrl(searchCfg.getSite());
                lemmas = lemmaService.getSortedFoundList(queryLemmas.keySet(), searchCfg.getTreshhold(), site.getId());
            }

            lemmas.forEach(l -> System.out.println(l.getLemma() + " - " + l.getFrequency()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
        return lemmas;
    }
}
