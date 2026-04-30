package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.hr.dto.projection.IsleProjection;
import com.sefault.server.hr.dto.record.IsleRecord;
import com.sefault.server.hr.entity.Isle;
import com.sefault.server.hr.mapper.IsleMapper;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.hr.repository.IsleRepository;
import com.sefault.server.hr.service.Impl.IsleServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void create_WithoutEmployee_Success() {
        IsleRecord recordNoEmployee = new IsleRecord(null, null, "A1");
        when(isleRepository.existsByCode("A1")).thenReturn(false);
        when(isleMapper.toEntity(any())).thenReturn(isle);
        when(isleRepository.save(any())).thenReturn(isle);
        when(isleMapper.entityToRecord(any())).thenReturn(recordNoEmployee);

        IsleRecord result = isleService.create(recordNoEmployee);

        assertThat(result).isNotNull();
        verify(employeeRepository, never()).existsByIdAndActiveTrue(any());
    }

    @Test
    void create_WithActiveEmployee_Success() {
        when(isleRepository.existsByCode("A1")).thenReturn(false);
        when(employeeRepository.existsByIdAndActiveTrue(employeeId)).thenReturn(true);
        when(isleMapper.toEntity(any())).thenReturn(isle);
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(null);
        when(isleRepository.save(any())).thenReturn(isle);
        when(isleMapper.entityToRecord(any())).thenReturn(isleRecord);

        IsleRecord result = isleService.create(isleRecord);

        assertThat(result).isNotNull();
        verify(employeeRepository).getReferenceById(employeeId);
    }

    @Test
    void create_DuplicateCode_ThrowsException() {
        when(isleRepository.existsByCode("A1")).thenReturn(true);

        assertThatThrownBy(() -> isleService.create(isleRecord))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Isle code already exists: A1");

        verify(isleRepository, never()).save(any());
    }

    @Test
    void create_WithTerminatedEmployee_ThrowsException() {
        when(isleRepository.existsByCode("A1")).thenReturn(false);
        when(isleMapper.toEntity(any())).thenReturn(isle);
        when(employeeRepository.existsByIdAndActiveTrue(employeeId)).thenReturn(false);

        assertThatThrownBy(() -> isleService.create(isleRecord))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot assign a terminated employee to an isle");

        verify(isleRepository, never()).save(any());
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

        assertThatThrownBy(() -> isleService.getById(isleId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getAll_ReturnsListOfRecords() {
        IsleProjection projection = mock(IsleProjection.class);
        when(isleRepository.findAllBy()).thenReturn(List.of(projection));
        when(isleMapper.projectionToRecord(projection)).thenReturn(isleRecord);

        List<IsleRecord> result = isleService.getAll();

        assertThat(result).hasSize(1);
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
        when(isleRepository.existsByCodeAndIdNot("A1", isleId)).thenReturn(false);
        when(isleRepository.save(any())).thenReturn(isle);
        when(isleMapper.entityToRecord(any())).thenReturn(isleRecord);

        IsleRecord result = isleService.update(isleId, isleRecord);

        assertThat(result).isNotNull();
        verify(isleRepository).save(isle);
    }

    @Test
    void update_NotFound_ThrowsException() {
        when(isleRepository.findById(isleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> isleService.update(isleId, isleRecord)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_DuplicateCode_ThrowsException() {
        when(isleRepository.findById(isleId)).thenReturn(Optional.of(isle));
        when(isleRepository.existsByCodeAndIdNot("A1", isleId)).thenReturn(true);

        assertThatThrownBy(() -> isleService.update(isleId, isleRecord))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Isle code already exists: A1");
    }

    @Test
    void update_ChangeToTerminatedEmployee_ThrowsException() {
        UUID newEmployeeId = UUID.randomUUID();
        isle.setEmployeeId(employeeId); // ancien employé différent
        IsleRecord recordWithNewEmployee = new IsleRecord(isleId, newEmployeeId, "A1");

        when(isleRepository.findById(isleId)).thenReturn(Optional.of(isle));
        when(isleRepository.existsByCodeAndIdNot(any(), any())).thenReturn(false);
        when(employeeRepository.existsByIdAndActiveTrue(newEmployeeId)).thenReturn(false);

        assertThatThrownBy(() -> isleService.update(isleId, recordWithNewEmployee))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot assign a terminated employee to an isle");
    }

    // ==========================================
    // ASSIGN & UNASSIGN TESTS
    // ==========================================

    @Test
    void assignEmployee_Success() {
        when(isleRepository.findById(isleId)).thenReturn(Optional.of(isle));
        when(employeeRepository.existsByIdAndActiveTrue(employeeId)).thenReturn(true);
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(null);
        when(isleRepository.save(any())).thenReturn(isle);
        when(isleMapper.entityToRecord(any())).thenReturn(isleRecord);

        IsleRecord result = isleService.assignEmployee(isleId, employeeId);

        assertThat(result).isNotNull();
        verify(isleRepository).save(isle);
    }

    @Test
    void assignEmployee_TerminatedEmployee_ThrowsException() {
        when(isleRepository.findById(isleId)).thenReturn(Optional.of(isle));
        when(employeeRepository.existsByIdAndActiveTrue(employeeId)).thenReturn(false);

        assertThatThrownBy(() -> isleService.assignEmployee(isleId, employeeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot assign a terminated employee to an isle");
    }

    @Test
    void assignEmployee_IsleNotFound_ThrowsException() {
        when(isleRepository.findById(isleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> isleService.assignEmployee(isleId, employeeId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void unassignEmployee_Success_SetsEmployeeNull() {
        when(isleRepository.findById(isleId)).thenReturn(Optional.of(isle));
        when(isleRepository.save(any())).thenReturn(isle);
        when(isleMapper.entityToRecord(any())).thenReturn(isleRecord);

        isleService.unassignEmployee(isleId);

        assertThat(isle.getEmployee()).isNull();
        verify(isleRepository).save(isle);
    }

    @Test
    void unassignEmployee_IsleNotFound_ThrowsException() {
        when(isleRepository.findById(isleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> isleService.unassignEmployee(isleId)).isInstanceOf(EntityNotFoundException.class);
    }

    // ==========================================
    // DELETE TESTS
    // ==========================================

    @Test
    void delete_CallsRepository() {
        when(isleRepository.findById(isleId)).thenReturn(Optional.of(isle));
        isleService.delete(isleId);
        verify(isleRepository).deleteById(isleId);
    }
}
