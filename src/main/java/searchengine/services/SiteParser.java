package searchengine.services;


import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.ParserCfg;
import searchengine.config.SiteCfg;
import searchengine.dao.PageDAO;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;


public class SiteParser extends RecursiveAction {

    private volatile Site site;
    private String currentUrl;

    @Setter
    private static ParserCfg parserCfg;
    private static Set existSitePath = ConcurrentHashMap.newKeySet();;
    @Setter
    private static PageService pageService ;

    public SiteParser(Site site, String currentUrl) {
        this.site = site;
        this.currentUrl = currentUrl;
        SiteParser.existSitePath.add(currentUrl);
    }

    public boolean isSubURL(String URL, String subURL) {
        String regex = URL + "/[-a-zA-Z0-9()@:%_\\+.~#?&//=]*(/|.html)";
        return subURL.matches(regex) &&
                !subURL.equals(URL);
    }

    private List<String> getUrls(Document document) {
        Elements elements = document.select("a");
        return elements.stream()
                .map(e -> e.absUrl("href"))
                .distinct()
                .collect(Collectors.toList());
    }

    private Document getDocument(Connection connection) throws IOException {
        return connection
                .userAgent(parserCfg.getUserAgent())
                .referrer(parserCfg.getReferer())
                .timeout(parserCfg.getTimeout())
                .get();
    }


    @Override
    protected void compute() {
        List<SiteParser> subTasks = new ArrayList<>();
        try {
            Connection connection = Jsoup.connect(currentUrl);
            int statusCode = connection.execute().statusCode();
            String path = currentUrl.substring(site.getUrl().length());
            Document document = getDocument(connection);
            Page page = new Page(site, path, statusCode, document.toString());
//            SiteParser.pageService.add(page);
            System.out.println(currentUrl);

            for (String url : getUrls(document)) {
                if (!isSubURL(site.getUrl(), url)) {
                    continue;
                }
                if (!SiteParser.existSitePath.add(url)) {
                    continue;
                }

                subTasks.add(new SiteParser(site, url));
            }
            Thread.sleep(parserCfg.getThreadDelay());
            subTasks.forEach(ForkJoinTask::invokeAll);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
