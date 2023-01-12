package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.repository.PageRepository;

import java.io.Serializable;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService, Serializable {

    private final PageRepository pageRepository;

    @Override
    public void save(Page page) {
        pageRepository.saveAndFlush(page);
    }

    @Override
    public void deleteAll() {
        pageRepository.deleteAll();
    }

    @Override
    public boolean existPagePath(int siteId, String path) {
        List<Page> pageList = pageRepository.getPagesByPath(siteId, path);
        if (pageList.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public int getPagesCount(int siteId) {
        return pageRepository.getPagesCount(siteId);
    }
}
