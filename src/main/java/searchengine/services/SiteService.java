package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SiteCfg;
import searchengine.config.SitesList;
import searchengine.model.Site;

import java.util.List;

@Service
public interface SiteService {
    Site save(Site site);
    Site getByUrl(String url);
    Site createSite(SiteCfg siteCfg);
    List<Site> getSitesToParsing(SitesList sites);
    Site addSiteToParsing(Site site);
    List<Site> getAll();
    void deleteAll();
    boolean isIndexing();
    void dropIndexingStatus();
}
