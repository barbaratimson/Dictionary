package com.example.dictionary.services;

import com.example.dictionary.dto.WordDTO;
import com.example.dictionary.entities.Word;
import com.example.dictionary.repositories.WordRepository;
import com.example.dictionary.util.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.example.dictionary.util.Constants.MAIN_PAGE_CACHE_KEY;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordService {
    private final WordRepository wordRepository;
    private final Converter converter;
    private final RedisService redisService;

    public List<Word> findAll() {
        if (redisService.get(MAIN_PAGE_CACHE_KEY) == null) {
            redisService.add(MAIN_PAGE_CACHE_KEY,
                    wordRepository.findAll(PageRequest.of(0, 10)).getContent());
        }
        List<Word> wordsArray = redisService.get(MAIN_PAGE_CACHE_KEY);
        return Objects.requireNonNullElseGet(wordsArray,
                wordRepository::findAll);
    }

    public List<Word> findAllPagination(int page, int itemsPerPage) {
        return wordRepository.findAll(PageRequest.of(page, itemsPerPage))
                .getContent();
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }

    private List<Word> findByStartsWith(String startsWith, List<Word> words, Function<Word, String> getField) {
        if (isNullOrEmpty(startsWith)) {
            return words;
        }
        return words.stream()
                .filter(w -> getField.apply(w).startsWith(startsWith))
                .toList();
    }

    public List<Word> findBy(String startsWith, List<Word> words, boolean byTranslate) {
        if (byTranslate) {
            return findByStartsWith(startsWith, words, Word::getTranslate);
        }
        else {
            return findByStartsWith(startsWith, words, Word::getValue);
        }
    }

    @Transactional
    public void save(Word word) {
        wordRepository.save(word);
    }

    @Transactional
    public void saveAll(List<Word> words) {
        wordRepository.saveAll(words);
    }
}
