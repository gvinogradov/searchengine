package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.web.util.HtmlUtils.htmlUnescape;

@Slf4j
@Service
@RequiredArgsConstructor
public class MorphologyServiceImpl implements MorphologyService {
    private final LuceneMorphology luceneMorphology;
    private static final String[] PARTICLES_NAMES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ"};

    public List<String> morphologyForms(String word) {
        if (!luceneMorphology.checkString(word)) {
            return Collections.emptyList();
        }
        return luceneMorphology.getMorphInfo(word);
    }


    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : PARTICLES_NAMES) {
            String baseProperty = wordBase.split("\\s+", 2)[1];
            if (baseProperty.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private String[] arrayContainsRussianWords(String html) {
        String text = Jsoup.parse(html).text();
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    @Override
    public Set<String> getLemmaSet(String html) {
        return collectLemmas(html).keySet();
    }

    @Override
    public String getSnippet(String html, List<String> lemmas, int snippetSize) {
        String[] words = arrayContainsRussianWords(html);
        int[][] lemmasMap = new int[words.length][2];

        int max = 0;
        int startSnippet = 0;
        for (int i = 0; i < words.length; i++) {
            String lemma = getNormalWord(words[i].toLowerCase());
            if (lemma != null && lemmas.contains(lemma)) {
                lemmasMap[i][0] = 1;
                words[i] = "<b>" + words[i] + "</b>";
            }
            if (i > 0) {
                lemmasMap[i][1] = lemmasMap[i][0] + lemmasMap[i - 1][1];
            }
            if (i >= snippetSize) {
                lemmasMap[i][1] = lemmasMap[i - snippetSize][0];
                if (max <= lemmasMap[i][1]) {
                    max = lemmasMap[i][1];
                    startSnippet = i - snippetSize;
                }
            }
        }

        Integer endSnippet = startSnippet + snippetSize < words.length - 1 ? startSnippet + snippetSize
                : words.length - 1;
        String[] resultText = words.length > snippetSize ? Arrays.copyOfRange(words, startSnippet, endSnippet)
                : words;
        String snippet = "... ";
        for (String s : resultText) {
            snippet = snippet + s + " ";
        }
        snippet.concat("...");

        return snippet;
    }


    private String getNormalWord(String word) {
        List<String> wordBaseForms = morphologyForms(word);
        if (wordBaseForms.isEmpty()
                || anyWordBaseBelongToParticle(wordBaseForms)) {
            return null;
        }
        List<String> normalForms = luceneMorphology.getNormalForms(word);
        return normalForms.isEmpty() ? null
                : normalForms.get(0);
    }

    @Override
    public Map<String, Integer> collectLemmas(String html) {
        String[] text = arrayContainsRussianWords(html);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for (String word : text) {
            String lemma = getNormalWord(word);
            if (lemma == null) {
                continue;
            }
            if (lemmas.putIfAbsent(lemma, 1) != null) {
                lemmas.compute(lemma, (key, value) -> value + 1);
            }
        }
        return lemmas;
    }

}
