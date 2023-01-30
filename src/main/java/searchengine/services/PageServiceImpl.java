package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;


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

    @Override
    public List<Page> getPages(List<String> lemmas) {
        List<Page> pages = null;
        for (String lemma: lemmas) {
            if (pages == null) {
                pages = getPagesByLemma(lemma);
                continue;
            }
            List<Integer> pageIndexes = pages.stream().map(p -> p.getId()).toList();
            pages = findPagesByIdAndLemma(lemma, pageIndexes);
            if (pages.isEmpty()) {
                return Collections.emptyList();
            }
        }
        return pages == null ? Collections.emptyList() : pages;
    }

    @Override
    public List<Page> getPagesByLemma(String lemma) {
        return pageRepository.getPagesByLemma(lemma);
    }

    @Override
    public List<Page> findPagesByIdAndLemma(String lemma, List<Integer> pageIndexes) {
        return pageRepository.findPagesByIdAndLemma(lemma, pageIndexes);
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
