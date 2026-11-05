package dasturlash.uz.service;

import dasturlash.uz.dto.request.ArticleFilterRequestDTO;
import dasturlash.uz.entity.*;
import dasturlash.uz.enums.*;
import dasturlash.uz.exception.*;
import dasturlash.uz.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @Mock ArticleRepository articles;
    @Mock ArticleTranslationRepository translations;
    @Mock ArticleTypeLinkRepository typeLinks;
    @Mock RegionRepository regions;
    @Mock CategoryRepository categories;
    @Mock ProfileRepository profiles;
    @Mock ArticleTypeRepository types;
    @Mock TagRepository tags;
    @Mock ArticleTagRepository tagLinks;
    @InjectMocks ArticleService service;

    @Test void viewIncrementUsesAtomicPublishedOnlyUpdate() {
        when(articles.incrementViews("id", ArticleStatus.PUBLISHED)).thenReturn(1);
        assertDoesNotThrow(() -> service.increaseViewCount("id"));
        verify(articles).incrementViews("id", ArticleStatus.PUBLISHED);
        verify(articles, never()).save(any());
    }

    @Test void missingOrUnpublishedCounterTargetReturnsNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.increaseViewCount("missing"));
        assertThrows(ResourceNotFoundException.class, () -> service.increaseShareCount("missing"));
    }

    @Test void shareIncrementUsesItsOwnAtomicUpdate() {
        when(articles.incrementShares("id", ArticleStatus.PUBLISHED)).thenReturn(1);
        service.increaseShareCount("id");
        verify(articles).incrementShares("id", ArticleStatus.PUBLISHED);
    }

    @Test void tagLookupRejectsBlankNames() {
        assertThrows(BadRequestException.class, () -> service.getLatest4ByTag("  ", Language.UZ));
        verifyNoInteractions(articles);
    }

    @Test void regionPaginationRejectsInvalidInternalPage() {
        when(regions.findByIdAndVisibleTrue(2)).thenReturn(Optional.of(new Region()));
        assertThrows(BadRequestException.class, () -> service.getByRegion(2, Language.UZ, -1, 10));
        verifyNoInteractions(articles);
    }

    @Test void filterRejectsUserWithoutPublisherRole() {
        Profile user = new Profile();
        user.setStatus(ProfileStatus.ACTIVE);
        user.setRole(ProfileRole.USER);
        when(profiles.findByIdAndVisibleTrue(1)).thenReturn(Optional.of(user));
        assertThrows(BadRequestException.class,
                () -> service.filter(new ArticleFilterRequestDTO(), Language.UZ, 0, 10, 1));
        verifyNoInteractions(articles);
    }

    @Test void filterRejectsReversedDates() {
        Profile publisher = new Profile();
        publisher.setStatus(ProfileStatus.ACTIVE);
        publisher.setRole(ProfileRole.PUBLISHER);
        when(profiles.findByIdAndVisibleTrue(1)).thenReturn(Optional.of(publisher));
        ArticleFilterRequestDTO filter = new ArticleFilterRequestDTO();
        filter.setCreatedDateFrom(LocalDate.of(2026, 11, 10));
        filter.setCreatedDateTo(LocalDate.of(2026, 11, 1));
        assertThrows(BadRequestException.class,
                () -> service.filter(filter, Language.UZ, 0, 10, 1));
        verifyNoInteractions(articles);
    }

    @Test void regionPagePreservesRequestedPageSizeAndStableSorting() {
        when(regions.findByIdAndVisibleTrue(2)).thenReturn(Optional.of(new Region()));
        when(articles.findAll(ArgumentMatchers.<Specification<Article>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        service.getByRegion(2, Language.UZ, 1, 3);
        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);
        verify(articles).findAll(ArgumentMatchers.<Specification<Article>>any(), page.capture());
        assertEquals(1, page.getValue().getPageNumber());
        assertEquals(3, page.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, page.getValue().getSort().getOrderFor("createdDate").getDirection());
        assertNotNull(page.getValue().getSort().getOrderFor("id"));
    }
}
