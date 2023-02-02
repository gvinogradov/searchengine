package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.SearchCfg;
import searchengine.dto.search.PageRelevanceResponse;
import searchengine.dto.search.SearchError;
import searchengine.dto.search.SearchItem;
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
    private final SiteService siteService;


    private SearchResponse createResponse(List<PageRelevanceResponse> pagesRelevance, List<String> lemmas) {
        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(pagesRelevance.size());

        List<SearchItem> data = new ArrayList<>();
        for (PageRelevanceResponse pageRelevance: pagesRelevance) {
            Page page = pageRelevance.getPage();
            SearchItem searchItem = new SearchItem();
            searchItem.setSite(page.getSite().getUrl());
            searchItem.setSiteName(page.getSite().getName());
            //            todo: getTitle via JSoup
            searchItem.setTitle("test title");
            searchItem.setUri(page.getPath());
//            todo: makeSnippet() via JSoup
            searchItem.setSnippet("test snippettest snippettest snippettest snippettest snippettest snippettest snippettest snippettest snippettest snippet");
            searchItem.setRelevance(pageRelevance.getRelevance());
            data.add(searchItem);
        }
        response.setData(data);
        return response;
    }

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

        Integer siteId = searchCfg.getSite() == null ? null
                : siteService.getByUrl(searchCfg.getSite()).getId();

        Map<String, Integer> lemmasFrequency = lemmaService.collectLemmaFrequency(searchCfg, siteId);
        List<String> sortedLemmas = lemmasFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(l -> l.getKey()).toList();

        List<PageRelevanceResponse> pagesRelevance = pageService.getPagesRelevance(sortedLemmas, siteId);
        SearchResponse response = createResponse(pagesRelevance, sortedLemmas);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
