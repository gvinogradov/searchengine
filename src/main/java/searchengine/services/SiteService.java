package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SiteCfg;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;

import java.util.List;

@Service
public interface SiteService {
    Site add(Site site);
    Site addBySiteCfg(SiteCfg siteCfg, Status status);
    Site get(int siteId);
    void update(int siteId, Site site);
    void delete(int siteId);
    void deleteAll();
}
