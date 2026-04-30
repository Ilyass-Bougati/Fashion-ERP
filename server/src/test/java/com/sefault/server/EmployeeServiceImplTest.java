package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.hr.dto.projection.EmployeeProjection;
import com.sefault.server.hr.dto.record.EmployeeRecord;
import com.sefault.server.hr.entity.Employee;
import com.sefault.server.hr.mapper.EmployeeMapper;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.hr.service.Impl.EmployeeServiceImpl;
import com.sefault.server.image.repository.ImageRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
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
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private UUID employeeId;
    private Employee employee;
    private EmployeeRecord employeeRecord;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();

        employee = new Employee();
        employee.setId(employeeId);
        employee.setFirstName("Jean");
        employee.setLastName("Dupont");
        employee.setEmail("jean.dupont@email.com");
        employee.setPhoneNumber("0612345678");
        employee.setCIN("AB123456");
        employee.setActive(true);
        employee.setSalary(5000.0);
        employee.setCommission(0.5);
        employee.setHiredAt(LocalDateTime.now());

        employeeRecord = new EmployeeRecord(
                employeeId,
                null,
                "Jean",
                "Dupont",
                "0612345678",
                "AB123456",
                "jean.dupont@email.com",
                true,
                5000.0,
                0.5,
                LocalDateTime.now(),
                null,
                null,
                null);
    }

    // ==========================================
    // CREATE TESTS
    // ==========================================

    @Test
    void create_Success_ReturnsRecord() {
        when(employeeRepository.existsByEmail(any())).thenReturn(false);
        when(employeeRepository.existsByCIN(any())).thenReturn(false);
        when(employeeRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(employeeMapper.toEntity(any())).thenReturn(employee);
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeMapper.entityToRecord(any())).thenReturn(employeeRecord);

        EmployeeRecord result = employeeService.create(employeeRecord);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("jean.dupont@email.com");
        verify(employeeRepository).save(employee);
    }

    @Test
    void create_DuplicateEmail_ThrowsException() {
        when(employeeRepository.existsByEmail(any())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(employeeRecord))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_DuplicateCIN_ThrowsException() {
        when(employeeRepository.existsByEmail(any())).thenReturn(false);
        when(employeeRepository.existsByCIN(any())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(employeeRecord))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CIN already exists");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_DuplicatePhoneNumber_ThrowsException() {
        when(employeeRepository.existsByEmail(any())).thenReturn(false);
        when(employeeRepository.existsByCIN(any())).thenReturn(false);
        when(employeeRepository.existsByPhoneNumber(any())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(employeeRecord))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phone number already exists");

        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_WithImageId_SetsImage() {
        UUID imageId = UUID.randomUUID();
        EmployeeRecord recordWithImage = new EmployeeRecord(
                employeeId,
                imageId,
                "Jean",
                "Dupont",
                "0612345678",
                "AB123456",
                "jean.dupont@email.com",
                true,
                5000.0,
                0.5,
                LocalDateTime.now(),
                null,
                null,
                null);

        when(employeeRepository.existsByEmail(any())).thenReturn(false);
        when(employeeRepository.existsByCIN(any())).thenReturn(false);
        when(employeeRepository.existsByPhoneNumber(any())).thenReturn(false);
        when(employeeMapper.toEntity(any())).thenReturn(employee);
        when(imageRepository.getReferenceById(imageId)).thenReturn(null);
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeMapper.entityToRecord(any())).thenReturn(recordWithImage);

        EmployeeRecord result = employeeService.create(recordWithImage);

        assertThat(result.imageId()).isEqualTo(imageId);
        verify(imageRepository).getReferenceById(imageId);
    }

    // ==========================================
    // GET TESTS
    // ==========================================

    @Test
    void getById_Success_ReturnsRecord() {
        EmployeeProjection projection = mock(EmployeeProjection.class);
        when(employeeRepository.getEmployeeProjectionById(employeeId)).thenReturn(Optional.of(projection));
        when(employeeMapper.projectionToRecord(projection)).thenReturn(employeeRecord);

        EmployeeRecord result = employeeService.getById(employeeId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(employeeId);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(employeeRepository.getEmployeeProjectionById(employeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(employeeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Employee not found");
    }

    @Test
    void getAll_ReturnsListOfRecords() {
        EmployeeProjection projection = mock(EmployeeProjection.class);
        when(employeeRepository.findAllBy()).thenReturn(List.of(projection));
        when(employeeMapper.projectionToRecord(projection)).thenReturn(employeeRecord);

        List<EmployeeRecord> result = employeeService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void getActive_ReturnsOnlyActiveEmployees() {
        EmployeeProjection activeProjection = mock(EmployeeProjection.class);
        when(employeeRepository.findAllByActiveTrue()).thenReturn(List.of(activeProjection));
        when(employeeMapper.projectionToRecord(activeProjection)).thenReturn(employeeRecord);

        List<EmployeeRecord> result = employeeService.getActive();

        assertThat(result).hasSize(1);
    }

    // ==========================================
    // UPDATE TESTS
    // ==========================================

    @Test
    void update_Success_ReturnsUpdatedRecord() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByCINAndIdNot(any(), any())).thenReturn(false);
        when(employeeRepository.existsByPhoneNumberAndIdNot(any(), any())).thenReturn(false);
        when(employeeRepository.existsByEmailAndIdNot(any(), any())).thenReturn(false);
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeMapper.entityToRecord(any())).thenReturn(employeeRecord);

        EmployeeRecord result = employeeService.update(employeeId, employeeRecord);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(employee);
    }

    @Test
    void update_NotFound_ThrowsException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.update(employeeId, employeeRecord))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_DuplicateCIN_ThrowsException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByCINAndIdNot(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.update(employeeId, employeeRecord))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CIN already exists");
    }

    @Test
    void update_DuplicatePhone_ThrowsException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByCINAndIdNot(any(), any())).thenReturn(false);
        when(employeeRepository.existsByPhoneNumberAndIdNot(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.update(employeeId, employeeRecord))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Phone number already exists");
    }

    @Test
    void update_DuplicateEmail_ThrowsException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByCINAndIdNot(any(), any())).thenReturn(false);
        when(employeeRepository.existsByPhoneNumberAndIdNot(any(), any())).thenReturn(false);
        when(employeeRepository.existsByEmailAndIdNot(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.update(employeeId, employeeRecord))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
    }

    // ==========================================
    // TERMINATE & DELETE TESTS
    // ==========================================

    @Test
    void terminate_Success_SetsActiveAndTerminatedAt() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeMapper.entityToRecord(any())).thenReturn(employeeRecord);

        employeeService.terminate(employeeId);

        assertThat(employee.getActive()).isFalse();
        assertThat(employee.getTerminatedAt()).isNotNull();
        verify(employeeRepository).save(employee);
    }

    @Test
    void terminate_NotFound_ThrowsException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.terminate(employeeId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_CallsRepository() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        employeeService.delete(employeeId);
        verify(employeeRepository).deleteById(employeeId);
    }
}
