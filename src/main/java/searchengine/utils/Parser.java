package searchengine.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.ParserCfg;
import searchengine.model.Site;
import searchengine.services.IndexingService;
import searchengine.services.NetworkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class Parser extends RecursiveTask<Boolean> {

    private Site site;
    private String url;
    private Set<String> parsedUrls;
    private ParserCfg parserCfg;
    private NetworkService networkService;
    private IndexingService indexingService;
    private static AtomicBoolean isCanceled = new AtomicBoolean();
    private final static boolean PARSE_SUCCESS = true;
    private final static boolean PARSE_FAIL = false;

    public Parser(Site site, String url, Set<String> parsedUrls, NetworkService networkService, IndexingService indexingService, ParserCfg parserCfg) {
        this.site = site;
        this.url = url;
        this.parsedUrls = parsedUrls;
        this.parserCfg = parserCfg;
        this.networkService = networkService;
        this.indexingService = indexingService;
    }

    public static void setIsCanceled(boolean isCanceled) {
        Parser.isCanceled.set(isCanceled);
    }

    public boolean isSubURL(String URL, String subURL) {
        String regex = URL + "/[-a-zA-Z0-9()@:%_\\+.~#?&//=]*(/|.html)";
        ;
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
        synchronized (parsedUrls) {
            return parsedUrls.add(url);
        }
    }

    @Override
    protected Boolean compute() {
        if (Parser.isCanceled.get()) {
            return PARSE_FAIL;
        }
        if (!addNewUrl(url)) {
            return PARSE_SUCCESS;
        }

        boolean parseSubTasks = PARSE_SUCCESS;
        List<Parser> tasks = new ArrayList<>();
        try {
            Connection.Response response = networkService.getResponse(url);
            if (!networkService.isAvailableContent(response)) {
                return PARSE_SUCCESS;
            }

            indexingService.parsePage(site, response);
            log.info(url + " - " + parsedUrls.size());

            for (String child : getUrls(response.parse())) {
                if (isSubURL(site.getUrl(), child) &&
                        !parsedUrls.contains(child)) {
                    Parser newTask = new Parser(site, child, parsedUrls, networkService, indexingService, parserCfg);
                    tasks.add(newTask);
                }
            }
            Thread.sleep(parserCfg.getThreadDelay());

            for (ForkJoinTask task : tasks) {
                parseSubTasks = parseSubTasks && (Boolean) task.invoke();
            }
        } catch (Exception e) {
            log.error("error - " + e.getMessage());
        }
        return parseSubTasks;
    }
}
