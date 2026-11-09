package dasturlash.uz.service;

import dasturlash.uz.dto.request.*;
import dasturlash.uz.dto.response.*;
import dasturlash.uz.entity.*;
import dasturlash.uz.enums.ArticleStatus;
import dasturlash.uz.enums.Language;
import dasturlash.uz.enums.ProfileRole;
import dasturlash.uz.enums.ProfileStatus;
import dasturlash.uz.exception.BadRequestException;
import dasturlash.uz.exception.ResourceNotFoundException;
import dasturlash.uz.repository.*;
import dasturlash.uz.specification.ArticleSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleTranslationRepository articleTranslationRepository;
    private final ArticleTypeLinkRepository articleTypeLinkRepository;
    private final RegionRepository regionRepository;
    private final CategoryRepository categoryRepository;
    private final ProfileRepository profileRepository;
    private final ArticleTypeRepository articleTypeRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    // Save a draft together with its translations, types, and tags.
    @Transactional
    public ArticleResponseDTO create(ArticleCreateRequestDTO request, Integer moderatorId) {

        Profile moderator = requireActiveRole(moderatorId, ProfileRole.MODERATOR);

        Region region = regionRepository.findByIdAndVisibleTrue(request.getRegionId()).orElseThrow(() -> new ResourceNotFoundException("Region not found: " + request.getRegionId()));

        Category category = categoryRepository.findByIdAndVisibleTrue(request.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));

        List<ArticleType> articleTypes = getVisibleArticleTypes(request.getArticleTypeIds());

        validateTranslationLanguages(request.getTranslations());

        Article article = new Article();
        article.setImageId(request.getImageId());
        article.setModerator(moderator);
        article.setRegion(region);
        article.setCategory(category);
        article.setStatus(ArticleStatus.NOT_PUBLISHED);
        article.setVisible(true);
        article.setSharedCount(0L);
        article.setViewCount(0L);
        article.setCreatedDate(LocalDateTime.now());

        article = articleRepository.save(article);

        for (ArticleTranslationRequestDTO translationRequest : request.getTranslations()) {

            ArticleTranslation translation = new ArticleTranslation();
            translation.setArticle(article);
            translation.setLanguage(translationRequest.getLanguage());
            translation.setTitle(translationRequest.getTitle());
            translation.setDescription(translationRequest.getDescription());
            translation.setContent(translationRequest.getContent());

            articleTranslationRepository.save(translation);
        }

        for (ArticleType type : articleTypes) {
            ArticleTypeLink link = new ArticleTypeLink();
            link.setArticle(article);
            link.setArticleType(type);

            articleTypeLinkRepository.save(link);
        }

        updateTags(article, request.getTagNames());

        return toDTO(article);
    }

    // Return edited articles to draft and synchronize their related records.
    @Transactional
    public ArticleResponseDTO update(String id, ArticleUpdateRequestDTO request, Integer moderatorId) {

        requireActiveRole(moderatorId, ProfileRole.MODERATOR);

        Article article = getVisibleArticle(id);

        Region region = regionRepository.findByIdAndVisibleTrue(request.getRegionId()).orElseThrow(() -> new ResourceNotFoundException("Region not found: " + request.getRegionId()));

        Category category = categoryRepository.findByIdAndVisibleTrue(request.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));

        List<ArticleType> requestedTypes = getVisibleArticleTypes(request.getArticleTypeIds());

        validateTranslationLanguages(request.getTranslations());

        article.setImageId(request.getImageId());
        article.setRegion(region);
        article.setCategory(category);
        article.setStatus(ArticleStatus.NOT_PUBLISHED);
        article.setPublisher(null);
        article.setPublishedDate(null);

        // Old-image removal requires Attach integration.
        articleRepository.save(article);

        updateTranslations(article, request.getTranslations());
        updateArticleTypes(article, requestedTypes);
        updateTags(article, request.getTagNames());

        return toDTO(article);
    }

    // Require a moderator and hide the article without removing its row.
    @Transactional
    public void delete(String id, Integer moderatorId) {
        requireActiveRole(moderatorId, ProfileRole.MODERATOR);

        Article article = getVisibleArticle(id);
        article.setVisible(false);

        articleRepository.save(article);
    }

    // Set publisher and publication time only when publishing.
    @Transactional
    public ArticleResponseDTO changeStatus(String id, ArticleStatusUpdateRequestDTO request, Integer publisherId) {

        Profile publisher = requireActiveRole(publisherId, ProfileRole.PUBLISHER);

        Article article = getVisibleArticle(id);

        if (article.getStatus() == request.getStatus()) {
            return toDTO(article);
        }

        article.setStatus(request.getStatus());

        if (request.getStatus() == ArticleStatus.PUBLISHED) {
            article.setPublisher(publisher);
            article.setPublishedDate(LocalDateTime.now());
        } else {
            article.setPublisher(null);
            article.setPublishedDate(null);
        }

        articleRepository.save(article);

        return toDTO(article);
    }

    // Limit the public type list to five articles.
    @Transactional(readOnly = true)
    public List<ArticleShortResponseDTO> getLatest5ByType(Integer typeId, Language language) {

        return getLatestByType(typeId, language, 5);
    }

    // Limit the public type list to three articles.
    @Transactional(readOnly = true)
    public List<ArticleShortResponseDTO> getLatest3ByType(Integer typeId, Language language) {

        return getLatestByType(typeId, language, 3);
    }

    // Select up to eight public articles outside the supplied ID set.
    @Transactional(readOnly = true)
    public List<ArticleShortResponseDTO> getLatest8Excluding(ArticleExcludeRequestDTO request, Language language) {

        PageRequest pageable = PageRequest.of(0, 8);
        List<Article> articles;

        if (request.getExcludedIds().isEmpty()) {
            articles = articleRepository.findLatestPublished(language, ArticleStatus.PUBLISHED, pageable);
        } else {
            articles = articleRepository.findLatestPublishedExcludingIds(request.getExcludedIds(), language, ArticleStatus.PUBLISHED, pageable);
        }

        return articles.stream().map(article -> toShortDTO(article, language)).toList();
    }

    // Require a published article and its requested translation.
    @Transactional(readOnly = true)
    public ArticleFullResponseDTO getByIdAndLanguage(String id, Language language) {

        Article article = articleRepository.findByIdAndVisibleTrueAndStatus(id, ArticleStatus.PUBLISHED).orElseThrow(() -> new ResourceNotFoundException("Article not found: " + id));

        ArticleTranslation translation = articleTranslationRepository.findByArticle_IdAndLanguage(id, language).orElseThrow(() -> new ResourceNotFoundException("Article translation not found: " + language));

        return toFullDTO(article, translation, language);
    }

    // Exclude the current article from related articles of the same type.
    @Transactional(readOnly = true)
    public List<ArticleShortResponseDTO> getLatest4ByTypeExcluding(Integer typeId, String excludedId, Language language) {

        articleTypeRepository.findByIdAndVisibleTrue(typeId).orElseThrow(() -> new ResourceNotFoundException("Article type not found: " + typeId));

        List<Article> articles = articleRepository.findLatestByTypeExcludingId(typeId, excludedId, language, ArticleStatus.PUBLISHED, PageRequest.of(0, 4));

        return articles.stream().map(article -> toShortDTO(article, language)).toList();
    }

    // Rank public articles by views, then creation time and ID.
    @Transactional(readOnly = true)
    public List<ArticleShortResponseDTO> getMostRead4(Language language) {

        List<Article> articles = articleRepository.findMostRead(language, ArticleStatus.PUBLISHED, PageRequest.of(0, 4));

        return articles.stream().map(article -> toShortDTO(article, language)).toList();
    }

    // Validate the type before querying its localized public articles.
    private List<ArticleShortResponseDTO> getLatestByType(Integer typeId, Language language, int limit) {

        articleTypeRepository.findByIdAndVisibleTrue(typeId).orElseThrow(() -> new ResourceNotFoundException("Article type not found: " + typeId));

        List<Article> articles = articleRepository.findLatestByType(typeId, language, ArticleStatus.PUBLISHED, PageRequest.of(0, limit));

        return articles.stream().map(article -> toShortDTO(article, language)).toList();
    }

    // Require a visible, active profile with the specified role.
    private Profile requireActiveRole(Integer profileId, ProfileRole role) {

        Profile profile = profileRepository.findByIdAndVisibleTrue(profileId).orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + profileId));

        if (profile.getStatus() != ProfileStatus.ACTIVE || profile.getRole() != role) {
            throw new BadRequestException("An active " + role.name().toLowerCase(Locale.ROOT) + " profile is required.");
        }

        return profile;
    }

    // Exclude hidden articles from management operations.
    private Article getVisibleArticle(String id) {
        return articleRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Article not found: " + id));
    }

    // Resolve every supplied type and reject hidden or missing types.
    private List<ArticleType> getVisibleArticleTypes(Set<Integer> typeIds) {

        List<ArticleType> types = new ArrayList<>();

        for (Integer typeId : typeIds) {
            ArticleType type = articleTypeRepository.findByIdAndVisibleTrue(typeId).orElseThrow(() -> new ResourceNotFoundException("Article type not found: " + typeId));

            types.add(type);
        }

        return types;
    }

    // Reject repeated languages before saving translation rows.
    private void validateTranslationLanguages(List<ArticleTranslationRequestDTO> translations) {

        Set<Language> languages = new HashSet<>();

        for (ArticleTranslationRequestDTO translation : translations) {
            if (!languages.add(translation.getLanguage())) {
                throw new BadRequestException("Duplicate translation language: " + translation.getLanguage());
            }
        }
    }

    // Reuse existing language rows and remove translations omitted by the update.
    private void updateTranslations(Article article, List<ArticleTranslationRequestDTO> requestedTranslations) {

        List<ArticleTranslation> existingTranslations = articleTranslationRepository.findAllByArticle_Id(article.getId());

        Set<Language> requestedLanguages = new HashSet<>();

        for (ArticleTranslationRequestDTO request : requestedTranslations) {
            requestedLanguages.add(request.getLanguage());
        }

        for (ArticleTranslation existing : existingTranslations) {
            if (!requestedLanguages.contains(existing.getLanguage())) {
                articleTranslationRepository.delete(existing);
            }
        }

        for (ArticleTranslationRequestDTO request : requestedTranslations) {
            ArticleTranslation translation = existingTranslations.stream().filter(existing -> existing.getLanguage() == request.getLanguage()).findFirst().orElseGet(() -> {
                ArticleTranslation created = new ArticleTranslation();
                created.setArticle(article);
                created.setLanguage(request.getLanguage());
                return created;
            });

            translation.setTitle(request.getTitle());
            translation.setDescription(request.getDescription());
            translation.setContent(request.getContent());

            articleTranslationRepository.save(translation);
        }
    }

    // Retain matching links and add only missing type links.
    private void updateArticleTypes(Article article, List<ArticleType> requestedTypes) {

        Set<Integer> requestedIds = new HashSet<>();

        for (ArticleType type : requestedTypes) {
            requestedIds.add(type.getId());
        }

        List<ArticleTypeLink> existingLinks = articleTypeLinkRepository.findAllByArticle_Id(article.getId());

        Set<Integer> retainedIds = new HashSet<>();

        for (ArticleTypeLink link : existingLinks) {
            Integer typeId = link.getArticleType().getId();

            if (requestedIds.contains(typeId)) {
                retainedIds.add(typeId);
            } else {
                articleTypeLinkRepository.delete(link);
            }
        }

        for (ArticleType type : requestedTypes) {
            if (!retainedIds.contains(type.getId())) {
                ArticleTypeLink link = new ArticleTypeLink();
                link.setArticle(article);
                link.setArticleType(type);

                articleTypeLinkRepository.save(link);
            }
        }
    }

    // Normalize tag names, reuse tags, and synchronize article links.
    private void updateTags(Article article, Set<String> tagNames) {
        Set<String> normalizedNames = new HashSet<>();

        for (String name : tagNames) {
            String normalized = name.strip().toLowerCase(Locale.ROOT);

            if (normalized.isBlank() || normalized.length() > 100) {
                throw new BadRequestException("Tag names must contain between 1 and 100 characters.");
            }

            normalizedNames.add(normalized);
        }

        List<ArticleTag> existingLinks = articleTagRepository.findAllByArticle_Id(article.getId());

        Set<String> retainedNames = new HashSet<>();

        for (ArticleTag link : existingLinks) {
            String name = link.getTag().getName();

            if (normalizedNames.contains(name)) {
                retainedNames.add(name);
            } else {
                articleTagRepository.delete(link);
            }
        }

        for (String name : normalizedNames) {
            if (retainedNames.contains(name)) {
                continue;
            }

            Tag tag = tagRepository.findByName(name).orElseGet(() -> {
                Tag created = new Tag();
                created.setName(name);
                return tagRepository.save(created);
            });

            ArticleTag link = new ArticleTag();
            link.setArticle(article);
            link.setTag(tag);

            articleTagRepository.save(link);
        }
    }

    // Build the management response with type IDs and all translations.
    private ArticleResponseDTO toDTO(Article article) {
        ArticleResponseDTO response = new ArticleResponseDTO();

        response.setId(article.getId());
        response.setImageId(article.getImageId());
        response.setStatus(article.getStatus());
        response.setSharedCount(article.getSharedCount());
        response.setViewCount(article.getViewCount());
        response.setVisible(article.getVisible());
        response.setRegionId(article.getRegion().getId());
        response.setCategoryId(article.getCategory().getId());
        response.setModeratorId(article.getModerator().getId());
        response.setCreatedDate(article.getCreatedDate());
        response.setPublishedDate(article.getPublishedDate());

        if (article.getPublisher() != null) {
            response.setPublisherId(article.getPublisher().getId());
        }

        List<ArticleTypeLink> links = articleTypeLinkRepository.findAllByArticle_Id(article.getId());

        Set<Integer> typeIds = new HashSet<>();

        for (ArticleTypeLink link : links) {
            typeIds.add(link.getArticleType().getId());
        }

        response.setArticleTypeIds(typeIds);

        List<ArticleTranslation> translations = articleTranslationRepository.findAllByArticle_Id(article.getId());

        List<ArticleTranslationResponseDTO> translationResponses = new ArrayList<>();

        for (ArticleTranslation translation : translations) {
            ArticleTranslationResponseDTO translationResponse = new ArticleTranslationResponseDTO();

            translationResponse.setLanguage(translation.getLanguage());
            translationResponse.setTitle(translation.getTitle());
            translationResponse.setDescription(translation.getDescription());
            translationResponse.setContent(translation.getContent());

            translationResponses.add(translationResponse);
        }

        response.setTranslations(translationResponses);

        return response;
    }

    // Select the requested translation for a compact public response.
    private ArticleShortResponseDTO toShortDTO(Article article, Language language) {

        ArticleTranslation translation = articleTranslationRepository.findByArticle_IdAndLanguage(article.getId(), language).orElseThrow(() -> new ResourceNotFoundException("Article translation not found: " + language));

        ArticleImageResponseDTO image = new ArticleImageResponseDTO();
        image.setId(article.getImageId());
        // Image URL requires Attach integration.

        ArticleShortResponseDTO response = new ArticleShortResponseDTO();

        response.setId(article.getId());
        response.setTitle(translation.getTitle());
        response.setDescription(translation.getDescription());
        response.setImage(image);
        response.setPublishedDate(article.getPublishedDate());

        return response;
    }

    // Build localized article details and include linked tag names.
    private ArticleFullResponseDTO toFullDTO(Article article, ArticleTranslation translation, Language language) {

        ArticleFullResponseDTO response = new ArticleFullResponseDTO();

        response.setId(article.getId());
        response.setTitle(translation.getTitle());
        response.setDescription(translation.getDescription());
        response.setContent(translation.getContent());
        response.setPublishedDate(article.getPublishedDate());
        response.setSharedCount(article.getSharedCount());
        response.setViewCount(article.getViewCount());

        ArticleImageResponseDTO image = new ArticleImageResponseDTO();
        image.setId(article.getImageId());
        response.setImage(image);

        Region region = article.getRegion();

        ArticleLocationResponseDTO regionResponse = new ArticleLocationResponseDTO();

        regionResponse.setId(region.getId());
        regionResponse.setName(switch (language) {
            case UZ -> region.getNameUz();
            case RU -> region.getNameRu();
            case EN -> region.getNameEn();
        });

        response.setRegion(regionResponse);

        Category category = article.getCategory();

        ArticleLocationResponseDTO categoryResponse = new ArticleLocationResponseDTO();

        categoryResponse.setId(category.getId());
        categoryResponse.setName(switch (language) {
            case UZ -> category.getNameUz();
            case RU -> category.getNameRu();
            case EN -> category.getNameEn();
        });

        response.setCategory(categoryResponse);

        // TODO ArticleLike integration.
        response.setLikeCount(null);

        List<String> tagNames = articleTagRepository.findAllByArticle_Id(article.getId()).stream().map(link -> link.getTag().getName()).sorted().toList();

        response.setTagList(tagNames);

        return response;
    }

    // Normalize the tag before selecting up to four public articles.
    @Transactional(readOnly = true)
    public List<ArticleShortResponseDTO> getLatest4ByTag(String tagName, Language language) {
        if (tagName == null || tagName.isBlank() || tagName.strip().length() > 100) {
            throw new BadRequestException("Tag name must contain between 1 and 100 characters.");
        }
        return publicPage(language, null, null, null, tagName.strip().toLowerCase(Locale.ROOT), 0, 4).getContent();
    }

    // Require visible references and limit the combined public list to five.
    @Transactional(readOnly = true)
    public List<ArticleShortResponseDTO> getLatest5ByTypeAndRegion(Integer typeId, Integer regionId, Language language) {
        getVisibleArticleTypes(Set.of(typeId));
        requireRegion(regionId);
        return publicPage(language, typeId, regionId, null, null, 0, 5).getContent();
    }

    // Return a localized public page for a visible region.
    @Transactional(readOnly = true)
    public Page<ArticleShortResponseDTO> getByRegion(Integer regionId, Language language, int page, int size) {
        requireRegion(regionId);
        return publicPage(language, null, regionId, null, null, page, size);
    }

    // Select up to five localized public articles for a visible category.
    @Transactional(readOnly = true)
    public List<ArticleShortResponseDTO> getLatest5ByCategory(Integer categoryId, Language language) {
        requireCategory(categoryId);
        return publicPage(language, null, null, categoryId, null, 0, 5).getContent();
    }

    // Return a localized public page for a visible category.
    @Transactional(readOnly = true)
    public Page<ArticleShortResponseDTO> getByCategory(Integer categoryId, Language language, int page, int size) {
        requireCategory(categoryId);
        return publicPage(language, null, null, categoryId, null, page, size);
    }

    // Increment in the database to avoid losing concurrent view updates.
    @Transactional
    public void increaseViewCount(String id) {
        if (articleRepository.incrementViews(id, ArticleStatus.PUBLISHED) == 0) {
            throw new ResourceNotFoundException("Published article not found: " + id);
        }
    }

    // Increment in the database to avoid losing concurrent share updates.
    @Transactional
    public void increaseShareCount(String id) {
        if (articleRepository.incrementShares(id, ArticleStatus.PUBLISHED) == 0) {
            throw new ResourceNotFoundException("Published article not found: " + id);
        }
    }

    // Require a publisher and reject reversed date ranges before filtering.
    @Transactional(readOnly = true)
    public Page<ArticleShortResponseDTO> filter(ArticleFilterRequestDTO request, Language language, int page, int size, Integer publisherId) {
        requireActiveRole(publisherId, ProfileRole.PUBLISHER);
        if (request.getCreatedDateFrom() != null && request.getCreatedDateTo() != null && request.getCreatedDateFrom().isAfter(request.getCreatedDateTo())) {
            throw new BadRequestException("Created date from must not be after date to.");
        }
        if (request.getPublishedDateFrom() != null && request.getPublishedDateTo() != null && request.getPublishedDateFrom().isAfter(request.getPublishedDateTo())) {
            throw new BadRequestException("Published date from must not be after date to.");
        }
        return articleRepository.findAll(ArticleSpecifications.filter(request, language), articlePage(page, size)).map(article -> toShortDTO(article, language));
    }

    // Reuse public eligibility conditions for localized paginated lists.
    private Page<ArticleShortResponseDTO> publicPage(Language language, Integer typeId, Integer regionId, Integer categoryId, String tagName, int page, int size) {
        Specification<Article> spec = ArticleSpecifications.publicArticles(language, typeId, regionId, categoryId, tagName);
        return articleRepository.findAll(spec, articlePage(page, size)).map(article -> toShortDTO(article, language));
    }

    // Validate page bounds and sort deterministically by creation time and ID.
    private PageRequest articlePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("Page must be 1 or greater, and size must be between 1 and 100.");
        }
        return PageRequest.of(page, size, Sort.by("createdDate").descending().and(Sort.by("id").descending()));
    }

    // Reject hidden or missing regions.
    private void requireRegion(Integer id) {
        regionRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Region not found: " + id));
    }

    // Reject hidden or missing categories.
    private void requireCategory(Integer id) {
        categoryRepository.findByIdAndVisibleTrue(id).orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

}
