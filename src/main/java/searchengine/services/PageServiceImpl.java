package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.dao.PageDAO;

import java.io.Serializable;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService, Serializable {

    private final PageDAO pageDAO;

    @Override
    public void save(Page page) {
        pageDAO.saveAndFlush(page);
    }

    @Override
    public void deleteAll() {
        pageDAO.deleteAll();
    }

    @Override
    public boolean existPagePath(int siteId, String path) {
        List<Page> pageList = pageDAO.getPagesByPath(siteId, path);
        if (pageList.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public int getPagesCount(int siteId) {
        return pageDAO.getPagesCount(siteId);
    }
}
