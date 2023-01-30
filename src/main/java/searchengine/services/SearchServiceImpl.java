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
import searchengine.model.Page;

import java.util.*;
import java.util.stream.Collectors;

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
        if (searchCfg.getQuery() == null) {
            return new ResponseEntity<>(new SearchError(false, "Задан пустой поисковый запрос"),
                    HttpStatus.NOT_FOUND);
        }
        if (searchCfg.getLimit() == 0) {
            searchCfg.setLimit(defaultSearchCfg.getLimit());
        }

//        todo: переписать на получение MAP<String, Integer>
//        List<Lemma> lemmas = lemmaService.getSortedLemmas(searchCfg);

        Map<String, Integer> lemmasFrequency = lemmaService.collectLemmaFrequency(searchCfg);

        List<String> sortedLemmas = lemmasFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(l -> l.getKey()).toList();

        List<Page> pages = pageService.getPages(sortedLemmas);

        pages.forEach(p -> System.out.printf("%s%s \n", p.getSite().getUrl(), p.getPath()));

        SearchResponse response = new SearchResponse();
        response.setResult(true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }




}
