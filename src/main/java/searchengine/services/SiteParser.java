package searchengine.services;

import lombok.Setter;
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
    private static Set existSitePath = ConcurrentHashMap.newKeySet();;
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

    private Document getDocument(Connection connection) throws IOException {
        return connection
                .userAgent(parserCfg.getUserAgent())
                .referrer(parserCfg.getReferer())
                .timeout(parserCfg.getTimeout())
                .get();
    }


    private boolean isCorrectUrl(String url) throws IOException {
        String regex = site.getUrl() + "/[-a-zA-Z0-9()@:%_\\+.~#?&//=]*(/|.html)";
//        Connection connection = Jsoup.connect(url);
        if (!url.matches(regex)
                || url.equals(site.getUrl())
                || SiteParser.existSitePath.contains(url)
//                || !(connection.execute().statusCode() == HttpStatus.OK.value())
                )
        {
            return false;
        }
        return true;
    }

    private Page addPage(Connection connection, Document document) throws IOException {
        SiteParser.existSitePath.add(url);
        int statusCode = connection.execute().statusCode();
        String path = url.substring(site.getUrl().length());
        return new Page(site, path, statusCode, document.toString());
    }

    @Override
    protected void compute() {
        if (SiteParser.isCanceled.get()) {
            return;
        }
        List<SiteParser> tasks = new ArrayList<>();
        try {
            Connection connection = Jsoup.connect(url);
            Document document = getDocument(connection);
            Page page = addPage(connection, document);
            pageService.add(page);
            System.out.println(url);

            for (String child : getUrls(document)) {
                if (isCorrectUrl(child)) {
                    SiteParser task = new SiteParser(site, child);
                    tasks.add(task);
                }
            }

            Thread.sleep(parserCfg.getThreadDelay());
            ForkJoinTask.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
