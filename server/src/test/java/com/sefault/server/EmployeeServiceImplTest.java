package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.hr.dto.projection.EmployeeProjection;
import com.sefault.server.hr.dto.record.EmployeeRecord;
import com.sefault.server.hr.entity.Employee;
import com.sefault.server.hr.mapper.EmployeeMapper;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.hr.service.Impl.EmployeeServiceImpl;
import com.sefault.server.image.repository.ImageRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    private UUID imageId; // Ajout pour gérer le @NonNull
    private Employee employee;
    private EmployeeRecord employeeRecord;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        imageId = UUID.randomUUID();

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
    }

    // ==========================================
    // CREATE TESTS
    // ==========================================

    @Test
    void create_Success_ReturnsRecord() {
        // Arrange
        when(employeeMapper.toEntity(any())).thenReturn(employee);
        when(imageRepository.getReferenceById(imageId)).thenReturn(null);
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeMapper.entityToRecord(any())).thenReturn(employeeRecord);

        EmployeeRecord result = employeeService.create(employeeRecord);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("jean.dupont@email.com");
        assertThat(result.imageId()).isEqualTo(imageId);

        verify(imageRepository).getReferenceById(imageId);
        verify(employeeRepository).save(employee);
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
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void getAll_ReturnsPagedRecords() {
        Pageable pageable = mock(Pageable.class);
        EmployeeProjection projection = mock(EmployeeProjection.class);
        Page<EmployeeProjection> projectionPage = new PageImpl<>(List.of(projection));

        when(employeeRepository.findAllBy(pageable)).thenReturn(projectionPage);
        when(employeeMapper.projectionToRecord(projection)).thenReturn(employeeRecord);

        Page<EmployeeRecord> result = employeeService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(employeeRepository).findAllBy(pageable);
    }

    @Test
    void getActive_ReturnsPagedActiveEmployees() {
        Pageable pageable = mock(Pageable.class);
        EmployeeProjection activeProjection = mock(EmployeeProjection.class);
        Page<EmployeeProjection> projectionPage = new PageImpl<>(List.of(activeProjection));

        when(employeeRepository.findAllByActiveTrue(pageable)).thenReturn(projectionPage);
        when(employeeMapper.projectionToRecord(activeProjection)).thenReturn(employeeRecord);

        Page<EmployeeRecord> result = employeeService.getActive(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    // ==========================================
    // UPDATE TESTS
    // ==========================================

    @Test
    void update_Success_ReturnsUpdatedRecord() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        // Ajout du mock obligatoire pour la mise à jour de l'image
        when(imageRepository.getReferenceById(imageId)).thenReturn(null);
        when(employeeRepository.save(any())).thenReturn(employee);
        when(employeeMapper.entityToRecord(any())).thenReturn(employeeRecord);

        // Act
        EmployeeRecord result = employeeService.update(employeeId, employeeRecord);

        // Assert
        assertThat(result).isNotNull();
        verify(employeeMapper).updateEntityFromRecord(employeeRecord, employee);
        verify(imageRepository).getReferenceById(imageId); // Vérifie que l'image est bien récupérée
        verify(employeeRepository).save(employee);
    }

    @Test
    void update_NotFound_ThrowsException() {
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.update(employeeId, employeeRecord))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    // ==========================================
    // TERMINATE & DELETE TESTS
    // ==========================================

    @Test
    void terminate_Success_ReturnsUpdatedRecord() {

        when(employeeRepository.terminateEmployee(eq(employeeId), any(LocalDateTime.class)))
                .thenReturn(1);

        EmployeeProjection projection = mock(EmployeeProjection.class);
        when(employeeRepository.getEmployeeProjectionById(employeeId)).thenReturn(Optional.of(projection));
        when(employeeMapper.projectionToRecord(projection)).thenReturn(employeeRecord);

        EmployeeRecord result = employeeService.terminate(employeeId);

        assertThat(result).isNotNull();
        verify(employeeRepository).terminateEmployee(eq(employeeId), any(LocalDateTime.class));
    }

    @Test
    void terminate_NotFound_ThrowsException() {

        when(employeeRepository.terminateEmployee(eq(employeeId), any(LocalDateTime.class)))
                .thenReturn(0);

        assertThatThrownBy(() -> employeeService.terminate(employeeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void delete_CallsRepositoryDirectly() {
        employeeService.delete(employeeId);

        verify(employeeRepository).deleteById(employeeId);
    }
}
