package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MorphologyServiceImpl implements MorphologyService {
    private final LuceneMorphology luceneMorphology;
    private static final String[] PARTICLES_NAMES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    public List<String> morphologyForms(String word) {
        if (!luceneMorphology.checkString(word)) {
            return Collections.emptyList();
        }
        return luceneMorphology.getMorphInfo(word);
    }

    private String htmlToText(String html) {
        return Jsoup.clean(html, Whitelist.none());
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : PARTICLES_NAMES) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private String[] arrayContainsRussianWords(String html) {
        String text = htmlToText(html);
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    @Override
    public Set<String> getLemmaSet(String html) {
        String[] text = arrayContainsRussianWords(html);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : text) {
            List<String> wordBaseForms = morphologyForms(word);
            if (wordBaseForms.isEmpty() || anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }
            lemmaSet.addAll(luceneMorphology.getNormalForms(word));
        }
        return lemmaSet;
    }

    @Override
    public Map<String, Integer> collectLemmas(String html) {
        String[] words = arrayContainsRussianWords(html);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for (String word : words) {
            List<String> wordBaseForms = morphologyForms(word);
            if (wordBaseForms.isEmpty() || anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);
            if (lemmas.putIfAbsent(normalWord, 1) != null) {
                lemmas.compute(normalWord, (key, value) -> value + 1);
            }
        }
        return lemmas;
    }

}
