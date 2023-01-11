package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import searchengine.config.ParserCfg;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SiteParser extends RecursiveAction {

    private Site site;
    private String url;

    private static ParserCfg parserCfg;
    private static Set parsedUrls = ConcurrentHashMap.newKeySet();;
    private static PageService pageService;
    private static AtomicBoolean isCanceled = new AtomicBoolean();

    public SiteParser(Site site, String url, PageService pageService, ParserCfg parserCfg) {
        this(site, url);
        SiteParser.pageService = pageService;
        SiteParser.parserCfg = parserCfg;
    }

    public SiteParser(Site site, String url) {
        this.site = site;
        this.url = url;
    }

    public static void setIsCanceled(boolean isCanceled) {
        SiteParser.isCanceled.set(isCanceled);
    }

    private List<String> getUrls(Document document) {
        Elements elements = document.select("a");
        return elements.stream()
                .map(e -> e.absUrl("href"))
                .distinct()
                .collect(Collectors.toList());
    }

    private Document getDocument(Connection connection) {
        try {
            return connection
                    .userAgent(SiteParser.parserCfg.getUserAgent())
                    .referrer(SiteParser.parserCfg.getReferer())
                    .timeout(SiteParser.parserCfg.getTimeout())
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private boolean isCorrectUrl(String url) throws IOException {
        String regex = site.getUrl() + "/[-a-zA-Z0-9()@:%_\\+.~#?&//=]*(/|.html)";
        return url.matches(regex);
    }

    private boolean addNewUrl(String url) {
        synchronized (SiteParser.parsedUrls) {
            return SiteParser.parsedUrls.add(url);
        }
    }
    
    @Override
    protected void compute() {
        if (SiteParser.isCanceled.get() || !addNewUrl(url)) {
            return;
        }
        List<SiteParser> tasks = new ArrayList<>();
        try {

            Connection connection = Jsoup.connect(url);
            int statusCode = connection.execute().statusCode();
            if (statusCode != HttpStatus.OK.value()) {
                return;
            }

            Document document = getDocument(connection);
            String path = url.substring(site.getUrl().length());
            Page page = new Page(site, path, statusCode, document.toString());

            SiteParser.pageService.add(page);
            System.out.println(url);

            for (String child : getUrls(document)) {
                if (isCorrectUrl(child)) {
                    SiteParser siteParser = new SiteParser(site, child);
                    siteParser.fork();
                    tasks.add(siteParser);
                    Thread.sleep(SiteParser.parserCfg.getThreadDelay());
                }
            }

            tasks.forEach(ForkJoinTask::join);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
