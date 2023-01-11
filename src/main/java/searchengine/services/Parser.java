package searchengine.services;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import searchengine.config.ParserCfg;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class Parser extends RecursiveAction {

    private Site site;
    private String path;
    private static ParserCfg parserCfg;
    private static PageService pageService;
    private static Set<String> parsedUrls = ConcurrentHashMap.newKeySet();
    private static AtomicBoolean isCanceled = new AtomicBoolean();

    public Parser(Site site, String path) {
        this.site = site;
        this.path = path;
    }

    public Parser(Site site, String path, PageService pageService, ParserCfg parserCfg) {
        this(site, path);
        Parser.parserCfg = parserCfg;
        Parser.pageService = pageService;
    }

    public static void setIsCanceled(boolean isCanceled) {
        Parser.isCanceled.set(isCanceled);
    }

    public boolean isSubURL(String URL, String subURL) {
        String regex = URL + "[-a-zA-Z0-9()@:%_\\+.~#?&//=]+(/|.html)";
        return subURL.matches(regex);
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
                    .userAgent(Parser.parserCfg.getUserAgent())
                    .referrer(Parser.parserCfg.getReferer())
                    .timeout(Parser.parserCfg.getTimeout())
                    .get();
        } catch (IOException e) {
            return null;
        }
    }

    private int getStatus(Connection connection) {
        try {
            return connection
                    .userAgent(Parser.parserCfg.getUserAgent())
                    .referrer(Parser.parserCfg.getReferer())
                    .timeout(Parser.parserCfg.getTimeout())
                    .execute().statusCode();
        } catch (IOException e) {
            return HttpStatus.NOT_FOUND.value();
        }
    }


    private boolean addNewUrl(String url) {
        synchronized (Parser.parsedUrls) {
            return Parser.parsedUrls.add(url);
        }
    }

    @Override
    protected void compute() {
        if (Parser.isCanceled.get() || !addNewUrl(site.getUrl() + path)) {
            return;
        }

        List<Parser> tasks = new ArrayList<>();
        try {
            Connection connection = Jsoup.connect(site.getUrl() + path);
            int status = getStatus(connection);
            if (status != HttpStatus.OK.value()) {
                return;
            }
            Document document = getDocument(connection);

            Page page = new Page(site, path, status, document.toString());
            Parser.pageService.add(page);
            System.out.println(site.getUrl() + path);
            log.info("urls = " + parsedUrls.size());


            for (String url : getUrls(document)) {
                if (!isSubURL(site.getUrl(), url) ||
                        parsedUrls.contains(url)) {
                    continue;
                }
                String urlsPath = url.substring(site.getUrl().length());
                Parser newTask = new Parser(site, urlsPath);
                tasks.add(newTask);
            }
            Thread.sleep(Parser.parserCfg.getThreadDelay());
            ForkJoinTask.invokeAll(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
