package searchengine.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import searchengine.config.ParserCfg;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.FactoryService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public class Parser extends RecursiveTask<Boolean> {

    private Site site;
    private String url;
    private Set<String> parsedUrls;
    @Getter
    private Map<Lemma, Integer> lemmaFrequency;
    private static ParserCfg parserCfg;
    private static FactoryService factoryService;
    private static AtomicBoolean isCanceled = new AtomicBoolean();
    private static LemmaFinder lemmaFinder;
    private final static boolean PARSE_SUCCESS = true;
    private final static boolean PARSE_FAIL = false;

    public Parser(Site site, String url, Set<String> parsedUrls, Map<Lemma, Integer> lemmaFrequency) {
        this.site = site;
        this.url = url;
        this.parsedUrls = parsedUrls;
        this.lemmaFrequency = lemmaFrequency;
    }

    public Parser(Site site, String url, FactoryService factoryService, LemmaFinder lemmaFinder, ParserCfg parserCfg) {
        this(site, url, new HashSet<>(), new ConcurrentHashMap<>());
        Parser.parserCfg = parserCfg;
        Parser.factoryService = factoryService;
        Parser.lemmaFinder = lemmaFinder;
    }

    public static void setIsCanceled(boolean isCanceled) {
        Parser.isCanceled.set(isCanceled);
    }

//    todo: переделать regex
    public boolean isSubURL(String URL, String subURL) {
        String regex = URL + "/[-a-zA-Z0-9()@:%_\\+.~#?&//=]*(/|.html)";;
//        String regex = URL + "/[-a-zA-Z0-9()@:%_\\+.~#?&//=]+";
        return subURL.matches(regex);
//        return  (subURL.startsWith(URL) || subURL.startsWith("/")) && !subURL.endsWith("#");
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

    private void addLemmas() {
        synchronized (lemmaFrequency) {
            factoryService.getLemmaService().mergeFrequency(lemmaFrequency);
            lemmaFrequency.clear();
        }
    }

    private void calculateLemmas(Set<String> lemmaSet) {
        for (String lemmaName : lemmaSet) {
            Lemma lemma = new Lemma();
            lemma.setLemma(lemmaName);
            lemma.setSite(site);
            if (lemmaFrequency.putIfAbsent(lemma,1) != null) {
                lemmaFrequency.compute(lemma, (key, value) -> value + 1);
            }
        }
    }

    private int getLemmasSum(){
        return lemmaFrequency.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    protected Boolean compute() {
        if (Parser.isCanceled.get())
        {
            return PARSE_FAIL;
        }
        if (!addNewUrl(url))
        {
            return PARSE_SUCCESS;
        }

        boolean parseSubTasks = PARSE_SUCCESS;
        List<Parser> tasks = new ArrayList<>();
        try {
            Connection.Response response = factoryService.getNetworkService().getResponse(url);
            if ((response == null)
                    || (response.statusCode() != HttpStatus.OK.value())
                    || (!response.contentType().equalsIgnoreCase(parserCfg.getContentType())))
            {
                return PARSE_SUCCESS;
            }

            Page page = addPage(response);
            factoryService.getSiteService().updateSiteStatus(site, Status.INDEXING, "");
            log.info(url + " - " + parsedUrls.size());

            Set<String> lemmaSet = lemmaFinder.getLemmaSet(page.getContent());
            calculateLemmas(lemmaSet);
            if (getLemmasSum() > Parser.parserCfg.getLemmasTreshhold()) {
                addLemmas();
            }

            for (String child : getUrls(response.parse())) {
                if (isSubURL(site.getUrl(), child) &&
                        !parsedUrls.contains(child)) {
                    Parser newTask = new Parser(site, child, parsedUrls, lemmaFrequency);
                    tasks.add(newTask);
                }
            }
            Thread.sleep(parserCfg.getThreadDelay());

            for (ForkJoinTask task: tasks) {
                parseSubTasks = parseSubTasks && (Boolean) task.invoke();
            }
        } catch (Exception e) {
            log.error("error - " + e.getMessage());
        }
        return parseSubTasks;
    }
}
