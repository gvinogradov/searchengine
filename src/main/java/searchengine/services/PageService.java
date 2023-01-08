package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.model.Site;

public interface PageService {

    void add(Page page);
    void deleteAll();
    boolean existPagePath(int siteId, String path);
}
