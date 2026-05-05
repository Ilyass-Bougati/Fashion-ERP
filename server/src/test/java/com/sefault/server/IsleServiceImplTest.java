package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.hr.dto.projection.IsleProjection;
import com.sefault.server.hr.dto.record.IsleRecord;
import com.sefault.server.hr.entity.Isle;
import com.sefault.server.hr.mapper.IsleMapper;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.hr.repository.IsleRepository;
import com.sefault.server.hr.service.Impl.IsleServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class IsleServiceImplTest {

    @Mock
    private IsleRepository isleRepository;

    @Mock
    private IsleMapper isleMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private IsleServiceImpl isleService;

    private UUID isleId;
    private UUID employeeId;
    private Isle isle;
    private IsleRecord isleRecord;

    @BeforeEach
    void setUp() {

        isleId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        isle = new Isle();
        isle.setId(isleId);
        isle.setCode("A1");

        isleRecord = new IsleRecord(isleId, employeeId, "A1");
    }

    // ==========================================
    // CREATE TESTS
    // ==========================================

    @Test
    void create_WithEmployee_Success() {
        when(isleMapper.toEntity(any())).thenReturn(isle);
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(null);
        when(isleRepository.save(any())).thenReturn(isle);
        when(isleMapper.entityToRecord(any())).thenReturn(isleRecord);

        IsleRecord result = isleService.create(isleRecord);

        assertThat(result).isNotNull();
        verify(employeeRepository).getReferenceById(employeeId);
    }

    // ==========================================
    // GET TESTS
    // ==========================================

    @Test
    void getById_Success_ReturnsRecord() {
        IsleProjection projection = mock(IsleProjection.class);
        when(isleRepository.getIsleProjectionById(isleId)).thenReturn(Optional.of(projection));
        when(isleMapper.projectionToRecord(projection)).thenReturn(isleRecord);

        IsleRecord result = isleService.getById(isleId);

        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("A1");
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(isleRepository.getIsleProjectionById(isleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> isleService.getById(isleId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Isle not found");
    }

    @Test
    void getAll_ReturnsPageOfRecords() {
        Pageable pageable = PageRequest.of(0, 10);
        IsleProjection projection = mock(IsleProjection.class);

        Page<IsleProjection> projectionPage = new PageImpl<>(List.of(projection));

        when(isleRepository.findAllBy(pageable)).thenReturn(projectionPage);
        when(isleMapper.projectionToRecord(projection)).thenReturn(isleRecord);

        Page<IsleRecord> result = isleService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).code()).isEqualTo("A1");
    }

    @Test
    void getByEmployee_ReturnsIslesForEmployee() {
        IsleProjection projection = mock(IsleProjection.class);
        when(isleRepository.findAllByEmployeeId(employeeId)).thenReturn(List.of(projection));
        when(isleMapper.projectionToRecord(projection)).thenReturn(isleRecord);

        List<IsleRecord> result = isleService.getByEmployee(employeeId);

        assertThat(result).hasSize(1);
    }

    // ==========================================
    // UPDATE TESTS
    // ==========================================

    @Test
    void update_Success_ReturnsUpdatedRecord() {
        isle.setEmployeeId(employeeId);
        when(isleRepository.findById(isleId)).thenReturn(Optional.of(isle));
        when(isleRepository.save(any())).thenReturn(isle);
        when(isleMapper.entityToRecord(any())).thenReturn(isleRecord);

        IsleRecord result = isleService.update(isleId, isleRecord);

        assertThat(result).isNotNull();
        verify(isleRepository).save(isle);
    }

    @Test
    void update_NotFound_ThrowsException() {
        when(isleRepository.findById(isleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> isleService.update(isleId, isleRecord))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Isle not found");
    }

    // ==========================================
    // DELETE TESTS
    // ==========================================

    @Test
    void delete_CallsRepository() {
        isleService.delete(isleId);

        verify(isleRepository).deleteById(isleId);
    }
}
