package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.SearchCfg;
import searchengine.dto.search.SearchError;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Page;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SearchCfg defaultSearchCfg;
    private final LemmaService lemmaService;
    private final PageService pageService;

    @Override
    public ResponseEntity<?> search(SearchCfg searchCfg) {
        searchCfg.setTreshhold(defaultSearchCfg.getTreshhold());
        if (searchCfg.getQuery() == "") {
            return new ResponseEntity<>(new SearchError(false, "Задан пустой поисковый запрос"),
                    HttpStatus.NO_CONTENT);
        }
        if (searchCfg.getLimit() == 0) {
            searchCfg.setLimit(defaultSearchCfg.getLimit());
        }

        Map<String, Integer> lemmasFrequency = lemmaService.collectLemmaFrequency(searchCfg);

        List<String> sortedLemmas = lemmasFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(l -> l.getKey()).toList();

        List<Page> pagesRelevance = pageService.getPagesRelevance(sortedLemmas);

//        pages.forEach(p -> System.out.printf("%s%s \n", p.getSite().getUrl(), p.getPath()));

        SearchResponse response = new SearchResponse();
        response.setResult(true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }




}
