package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.dao.PageDAO;

import java.util.List;


@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private PageDAO pageDAO;

    @Override
    public void add(Page page) {
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
}
