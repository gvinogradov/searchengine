package searchengine.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import searchengine.config.ParserCfg;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.FactoryService;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private String url;
    private static ParserCfg parserCfg;
    private static FactoryService factoryService;
    private static Set<String> parsedUrls = ConcurrentHashMap.newKeySet();
    private static AtomicBoolean isCanceled = new AtomicBoolean();

    public Parser(Site site, String url) {
        this.site = site;
        this.url = url;
    }

    public Parser(Site site, String url, FactoryService factoryService, ParserCfg parserCfg) {
        this(site, url);
        Parser.parserCfg = parserCfg;
        Parser.factoryService = factoryService;
    }

    public static void setIsCanceled(boolean isCanceled) {
        Parser.isCanceled.set(isCanceled);
    }

    public boolean isSubURL(String URL, String subURL) {
        String regex = URL + "/[-a-zA-Z0-9()@:%_\\+.~#?&//=]*(/|.html)";
        return subURL.matches(regex);
    }

    private List<String> getUrls(Document document) {
        Elements elements = document.select("a");
        return elements.stream()
                .map(e -> e.absUrl("href"))
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean addNewUrl(String url) {
        synchronized (Parser.parsedUrls) {
            return Parser.parsedUrls.add(url);
        }
    }

    private Page addPage(Connection.Response response) throws IOException {
        Document document = response.parse();
        String path = url.substring(site.getUrl().length());
        Page page = new Page();
        page.setSite(site);
        page.setCode(response.statusCode());
        page.setPath(path);
        page.setContent(document.toString());
        factoryService.getPageService().save(page);
        return page;
    }

    private void updateSiteStatus(Status status, String lastError) {
        site.setStatus(status);
        site.setLastError(lastError);
        site.setStatusTime(LocalDateTime.now());
        factoryService.getSiteService().save(site);
    }

    @Override
    protected void compute() {
        if (Parser.isCanceled.get()) {
            updateSiteStatus(Status.FAILED, "Индексация остановлена пользователем");
            return;
        }
        if (!addNewUrl(url)) {
            return;
        }

        List<Parser> tasks = new ArrayList<>();
        try {

            Connection.Response response = factoryService.getNetworkService().getResponse(url);
            if (response.statusCode() != HttpStatus.OK.value()) {
                return;
            }
            addPage(response);
            updateSiteStatus(Status.INDEXING, "");
            log.info(url + " - " + parsedUrls.size());

            for (String child: getUrls(response.parse())) {
                if (!isSubURL(site.getUrl(), child) ||
                        parsedUrls.contains(child)) {
                    continue;
                }
                Parser newTask = new Parser(site, child);
                tasks.add(newTask);
            }
            Thread.sleep(parserCfg.getThreadDelay());
            ForkJoinTask.invokeAll(tasks);
        } catch (Exception e) {
            log.error("error - " + e.getMessage());
        }
    }
}
