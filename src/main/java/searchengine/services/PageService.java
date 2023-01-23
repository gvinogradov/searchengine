package searchengine.services;

import org.jsoup.Connection;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;

public interface PageService {

    Page save(Page page);
    void deleteAll();
    boolean existPagePath(int siteId, String path);
    int getPagesCount(int siteId);
    Page addPage(Site site, Connection.Response response) throws IOException;
}
