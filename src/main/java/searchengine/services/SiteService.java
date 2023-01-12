package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SiteCfg;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;

import java.util.List;

@Service
public interface SiteService {
    Site save(Site site);
    void deleteAll();
}
