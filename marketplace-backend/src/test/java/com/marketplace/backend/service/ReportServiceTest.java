package com.marketplace.backend.service;

import com.marketplace.backend.dto.ReportDto;
import com.marketplace.backend.exception.NotFoundException;
import com.marketplace.backend.mapper.PageMapper;
import com.marketplace.backend.mapper.ReportMapper;
import com.marketplace.backend.model.Report.CreateReportRequest;
import com.marketplace.backend.model.Report.Report;
import com.marketplace.backend.model.Report.Status;
import com.marketplace.backend.repository.ReportRepository;
import com.marketplace.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

	@Mock ReportRepository reportRepository;
	@Mock UserRepository userRepository;
	@Mock ReportMapper reportMapper;
	@Mock PageMapper pageMapper;

	@InjectMocks ReportService reportService;

	@Test
	void create_validAuthor_savesActiveReport() {
		CreateReportRequest req = new CreateReportRequest();
		req.setTargetId("target-1");
		req.setDescription("violates rules");

		when(userRepository.existsById("author-1")).thenReturn(true);
		when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
		when(reportMapper.mapToDto(any(Report.class))).thenReturn(new ReportDto());

		reportService.create(req, "author-1");

		ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
		verify(reportRepository).save(captor.capture());
		assertThat(captor.getValue().getStatus()).isEqualTo(Status.ACTIVE);
		assertThat(captor.getValue().getAuthorId()).isEqualTo("author-1");
		assertThat(captor.getValue().getTargetId()).isEqualTo("target-1");
	}

	@Test
	void create_unknownAuthor_throwsNotFound() {
		when(userRepository.existsById("ghost")).thenReturn(false);

		assertThatThrownBy(() -> reportService.create(new CreateReportRequest(), "ghost"))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void solve_setsArchived() {
		Report report = Report.builder().id("r-1").status(Status.ACTIVE).build();
		when(reportRepository.findById("r-1")).thenReturn(Optional.of(report));
		when(reportRepository.save(report)).thenReturn(report);
		when(reportMapper.mapToDto(report)).thenReturn(new ReportDto());

		reportService.solve("r-1");

		assertThat(report.getStatus()).isEqualTo(Status.ARCHIVED);
	}

	@Test
	void delete_existingReport_deletes() {
		Report report = Report.builder().id("r-1").build();
		when(reportRepository.findById("r-1")).thenReturn(Optional.of(report));

		reportService.delete("r-1");

		verify(reportRepository).delete(report);
	}

	@Test
	void delete_missingReport_throwsNotFound() {
		when(reportRepository.findById("ghost")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> reportService.delete("ghost"))
				.isInstanceOf(NotFoundException.class);
	}
}
