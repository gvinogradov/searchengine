package searchengine.services;

import org.jsoup.Connection;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.List;

public interface PageService {

    Page save(Page page);
    Page get(int pageId);
    List<Page> getPages(List<String> lemmas);
    List<Page> getPagesByLemma(String lemma);
    List<Page> findPagesByIdAndLemma(String lemma, List<Integer> pageIndexes);
    void deleteAll();
    boolean existPagePath(int siteId, String path);
    int getPagesCount(int siteId);
    Page addPage(Site site, Connection.Response response) throws IOException;
}
