package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServicesFactory {

    @Autowired
    private PageServiceImpl pageService;
    @Autowired
    private SiteServiceImpl siteService;

    public PageServiceImpl getPageService() {
        return pageService;
    }

    public SiteServiceImpl getSiteService() {
        return siteService;
    }
}
