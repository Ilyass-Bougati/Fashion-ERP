package com.sefault.server.sales.service.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.finance.service.TransactionService;
import com.sefault.server.hr.entity.Employee;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.sales.dto.projection.SaleProjection;
import com.sefault.server.sales.dto.record.SaleRecord;
import com.sefault.server.sales.entity.Sale;
import com.sefault.server.sales.mapper.SaleMapper;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.storage.repository.ProductVariationRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("SaleServiceImpl Unit Tests")
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleMapper saleMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ProductVariationRepository productVariationRepository;

    @InjectMocks
    private SaleServiceImpl saleService;

    private UUID saleId;
    private UUID employeeId;
    private Sale sale;
    private SaleRecord saleRecord;
    private Employee employee;

    @BeforeEach
    void setUp() {
        saleId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        employee = new Employee();
        employee.setId(employeeId);

        sale = new Sale();
        sale.setId(saleId);
        sale.setDiscount(0.1);
        sale.setRefunded(false);

        saleRecord = new SaleRecord(saleId, 0.1, employeeId, false, LocalDateTime.now(), LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // Create Tests
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("create   persists sale and maps employee correctly")
        void create_persistsSale() {
            when(saleMapper.toEntity(any())).thenReturn(sale);
            when(employeeRepository.getReferenceById(employeeId)).thenReturn(employee);
            when(saleRepository.save(any())).thenReturn(sale);
            when(saleMapper.entityToRecord(any())).thenReturn(saleRecord);

            SaleRecord result = saleService.create(saleRecord);

            assertThat(result).isNotNull();
            verify(employeeRepository).getReferenceById(employeeId);
            verify(saleRepository).save(sale);
        }
    }

    // -------------------------------------------------------------------------
    // Get Tests
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getById() and getAll()")
    class GetTests {

        @Test
        @DisplayName("getById   returns mapped record when sale exists")
        void getById_returnsRecord() {
            SaleProjection projection = mock(SaleProjection.class);
            when(saleRepository.getSaleProjectionById(saleId)).thenReturn(Optional.of(projection));
            when(saleMapper.projectionToRecord(projection)).thenReturn(saleRecord);

            SaleRecord result = saleService.getById(saleId);

            assertThat(result).isEqualTo(saleRecord);
        }

        @Test
        @DisplayName("getById   throws NotFoundException when missing")
        void getById_throwsNotFoundException() {
            when(saleRepository.getSaleProjectionById(saleId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.getById(saleId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(saleId.toString());
        }

        @Test
        @DisplayName("getAll   returns paginated results")
        void getAll_returnsPagedRecords() {
            Pageable pageable = PageRequest.of(0, 10);
            SaleProjection projection = mock(SaleProjection.class);
            when(saleRepository.findAllBy(pageable)).thenReturn(new PageImpl<>(java.util.List.of(projection)));
            when(saleMapper.projectionToRecord(projection)).thenReturn(saleRecord);

            Page<SaleRecord> result = saleService.getAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(saleRepository).findAllBy(pageable);
        }
    }

    // -------------------------------------------------------------------------
    // Delete Tests
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("delete   delegates to repository when sale exists")
        void delete_delegatesToRepository() {
            when(saleRepository.existsById(saleId)).thenReturn(true);
            saleService.delete(saleId);
            verify(saleRepository).deleteById(saleId);
        }

        @Test
        @DisplayName("delete   throws NotFoundException when sale does not exist")
        void delete_throwsNotFound() {
            when(saleRepository.existsById(saleId)).thenReturn(false);

            assertThatThrownBy(() -> saleService.delete(saleId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(saleId.toString());
            verify(saleRepository, never()).deleteById(any());
        }
    }
}
