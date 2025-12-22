package com.home.application.search;

import com.home.annotations.MockTest;
import com.home.domain.complex.ComplexRepository;
import com.home.infrastructure.web.search.dto.ComplexSearchResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@MockTest
class SearchUseCaseTest {

	@Mock
	private ComplexRepository complexRepository;

	@InjectMocks
	private SearchUseCase searchUseCase;

	@Test
	@DisplayName("q가 null이면 빈 리스트를 반환하고 repository를 호출하지 않는다")
	void searchComplexes_nullQuery_returnsEmpty() {
		// when
		List<ComplexSearchResponse> result = searchUseCase.searchComplexes(null);

		// then
		assertThat(result).isNotNull().isEmpty();
		verifyNoInteractions(complexRepository);
	}

	@Test
	@DisplayName("q가 공백/빈문자면 빈 리스트를 반환하고 repository를 호출하지 않는다")
	void searchComplexes_blankQuery_returnsEmpty() {
		// when
		List<ComplexSearchResponse> result1 = searchUseCase.searchComplexes("");
		List<ComplexSearchResponse> result2 = searchUseCase.searchComplexes("   ");

		// then
		assertThat(result1).isEmpty();
		assertThat(result2).isEmpty();
		verifyNoInteractions(complexRepository);
	}

	@Test
	@DisplayName("q가 유효하면 trim 후 keyword로 repository를 PageRequest(0,10)과 함께 호출한다")
	void searchComplexes_validQuery_callsRepositoryWithTrimmedKeyword() {
		// given
		String q = "  래미안  ";
		String keyword = "래미안";
		PageRequest page = PageRequest.of(0, 10);

		List<ComplexSearchResponse> expected = List.of(
			mock(ComplexSearchResponse.class),
			mock(ComplexSearchResponse.class)
		);

		given(complexRepository.searchComplexesWithParcel(keyword, page))
			.willReturn(expected);

		// when
		List<ComplexSearchResponse> result = searchUseCase.searchComplexes(q);

		// then
		assertThat(result).isEqualTo(expected);

		verify(complexRepository, times(1))
			.searchComplexesWithParcel(eq(keyword), eq(page));
		verifyNoMoreInteractions(complexRepository);
	}
}
