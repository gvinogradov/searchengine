package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.search.IPageRank;
import searchengine.dto.search.PageRankImpl;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;


@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService, Serializable {

    private final PageRepository pageRepository;
    private final IndexService indexService;

    @Override
    public Page save(Page page) {
        return pageRepository.saveAndFlush(page);
    }

    @Override
    public Page get(int pageId) {
        return pageRepository.findById(pageId).get();
    }

    private List<Page> makeSortedPages(List<IPageRank> totalRankPages) {

//        todo: сделать сортировку
//        todo: убрать лишние сетоды из интерфейсов
        List<Integer> sortedPagesId = totalRankPages.stream()
                .sorted(Comparator.comparing(IPageRank::getLemmaRank))
                .map(item -> item.getPageId())
                .toList();
        List<Page> pages = pageRepository.getPagesById(sortedPagesId);

        return pages;
    }

    @Override
    public List<Page> getPagesRelevance(List<String> lemmas) {
        List<IPageRank> totalRankPages = null;
        for (String lemma: lemmas) {
            if (totalRankPages == null) {
                totalRankPages = getPagesByLemma(lemma);
                continue;
            }
            List<Integer> pageIndexes = totalRankPages.stream().map(item -> item.getPageId()).toList();
            List<IPageRank> lemmaRankPages = findPagesByIdAndLemma(lemma, pageIndexes);
            totalRankPages = mergeAndIncrementRank(totalRankPages, lemmaRankPages);

            if (totalRankPages.isEmpty()) {
                return Collections.emptyList();
            }
        }

        List<Page> pagesByRelevance = makeSortedPages(totalRankPages);

        return pagesByRelevance;
    }

    @Override
    public List<IPageRank> getPagesByLemma(String lemma) {
        return pageRepository.getPagesByLemma(lemma);
    }

    @Override
    public List<IPageRank> findPagesByIdAndLemma(String lemma, List<Integer> pageIndexes) {
        return pageRepository.findPagesByIdAndLemma(lemma, pageIndexes);
    }

    @Override
    public IPageRank findExistItem(List<IPageRank> currentList, IPageRank findItem) {
        for (IPageRank item: currentList) {
            if (item.getPageId().equals(findItem.getPageId())) {
                return item;
            }
        }
        return null;
    }

    @Override
    public List<IPageRank> mergeAndIncrementRank(List<IPageRank> totalRankPages,
                                                        List<IPageRank> lemmaRankPages) {
        List<IPageRank> result = new ArrayList<>();
        for (IPageRank newPageRank: lemmaRankPages) {
            IPageRank foundItem = findExistItem(totalRankPages, newPageRank);
            if (foundItem != null) {
                IPageRank sumRankItem = new PageRankImpl(foundItem.getPageId(),
                        foundItem.getLemmaRank() + newPageRank.getLemmaRank());
                result.add(sumRankItem);
            }
        }
        return result;
    }

    @Override
    public void deleteAll() {
        pageRepository.deleteAll();
    }

    @Override
    public boolean existPagePath(int siteId, String path) {
        return pageRepository.getPagesByPath(siteId, path) != null;
    }

    @Override
    public int getPagesCount(int siteId) {
        return pageRepository.getPagesCount(siteId);
    }

    @Override
    public Page addPage(Site site, Connection.Response response) throws IOException {
        Document document = response.parse();
        String url = response.url().toString();
        String path = url.substring(site.getUrl().length());
        Page page = pageRepository.getPagesByPath(site.getId(), path);
        if (page == null) {
            page = new Page();
            page.setPath(path);
            page.setSite(site);
        }
        page.setCode(response.statusCode());
        page.setContent(document.toString());
        return save(page);
    }
}
