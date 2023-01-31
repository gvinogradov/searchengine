package searchengine.services;

import org.jsoup.Connection;
import searchengine.dto.search.IPageRank;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.List;

public interface PageService {

    Page save(Page page);
    Page get(int pageId);
    List<Page> getPagesRelevance(List<String> lemmas);
    List<IPageRank> getPagesByLemma(String lemma);
    List<IPageRank> findPagesByIdAndLemma(String lemma, List<Integer> pageIndexes);
    List<IPageRank> mergeAndIncrementRank(List<IPageRank> totalRankPages, List<IPageRank> lemmaRankPages);
    IPageRank findExistItem(List<IPageRank> currentList, IPageRank findItem);
    void deleteAll();
    boolean existPagePath(int siteId, String path);
    int getPagesCount(int siteId);
    Page addPage(Site site, Connection.Response response) throws IOException;
}
