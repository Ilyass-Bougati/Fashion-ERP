package com.sefault.server.sales.service.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.entity.Transaction;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.service.TransactionService;
import com.sefault.server.hr.entity.Employee;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.sales.dto.projection.SaleProjection;
import com.sefault.server.sales.dto.record.SaleRecord;
import com.sefault.server.sales.entity.Sale;
import com.sefault.server.sales.entity.SaleLine;
import com.sefault.server.sales.mapper.SaleMapper;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.storage.entity.ProductVariation;
import com.sefault.server.storage.repository.ProductVariationRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        sale.setSaleLines(new ArrayList<>());
        sale.setTransactions(new ArrayList<>());

        saleRecord = new SaleRecord(saleId, 0.1, employeeId, false, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
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

    @Nested
    @DisplayName("getById() and getAll()")
    class GetTests {

        @Test
        void getById_returnsRecord() {
            SaleProjection projection = mock(SaleProjection.class);

            when(saleRepository.getSaleProjectionById(saleId)).thenReturn(Optional.of(projection));
            when(saleMapper.projectionToRecord(projection)).thenReturn(saleRecord);

            SaleRecord result = saleService.getById(saleId);

            assertThat(result).isEqualTo(saleRecord);
        }

        @Test
        void getById_throwsNotFoundException() {
            when(saleRepository.getSaleProjectionById(saleId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> saleService.getById(saleId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(saleId.toString());
        }

        @Test
        void getAll_returnsPagedRecords() {
            Pageable pageable = PageRequest.of(0, 10);
            SaleProjection projection = mock(SaleProjection.class);

            when(saleRepository.findAllBy(pageable)).thenReturn(new PageImpl<>(List.of(projection)));
            when(saleMapper.projectionToRecord(projection)).thenReturn(saleRecord);

            Page<SaleRecord> result = saleService.getAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(saleRepository).findAllBy(pageable);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        void delete_delegatesToRepository() {
            when(saleRepository.existsById(saleId)).thenReturn(true);

            saleService.delete(saleId);

            verify(saleRepository).deleteById(saleId);
        }

        @Test
        void delete_throwsNotFound() {
            when(saleRepository.existsById(saleId)).thenReturn(false);

            assertThatThrownBy(() -> saleService.delete(saleId)).isInstanceOf(NotFoundException.class);

            verify(saleRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("checkout()")
    class CheckoutTests {

        @Test
        void checkout_CalculatesTotalAndCreatesTransaction() {
            SaleLine line = new SaleLine();
            line.setQuantity(2);
            line.setSaleAtPrice(1500.0);
            sale.setSaleLines(List.of(line));

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));

            TransactionRecord expectedTransaction = new TransactionRecord(
                    UUID.randomUUID(), TransactionType.RECEIVED, saleId, 2700.0, LocalDateTime.now());

            when(transactionService.createTransaction(any(TransactionRecord.class)))
                    .thenReturn(expectedTransaction);

            TransactionRecord result = saleService.checkout(saleId);

            assertThat(result).isNotNull();
            assertThat(result.amount()).isEqualTo(2700.0);
            verify(transactionService).createTransaction(any());
        }

        @Test
        void checkout_ThrowsException_WhenAlreadyCheckedOut() {
            sale.setTransactions(List.of(new Transaction()));

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));

            assertThatThrownBy(() -> saleService.checkout(saleId)).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("refund()")
    class RefundTests {

        @Test
        void refund_CalculatesTotalRestoresStockAndCreatesTransaction() {
            ProductVariation product = new ProductVariation();
            product.setId(UUID.randomUUID());

            SaleLine line = new SaleLine();
            line.setQuantity(2);
            line.setSaleAtPrice(1500.0);
            line.setProductVariation(product);
            sale.setSaleLines(List.of(line));

            Transaction paymentTransaction = new Transaction();
            paymentTransaction.setType(TransactionType.RECEIVED);
            sale.setTransactions(List.of(paymentTransaction));

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));

            TransactionRecord expectedTransaction =
                    new TransactionRecord(UUID.randomUUID(), TransactionType.PAID, saleId, 2700.0, LocalDateTime.now());

            when(transactionService.createTransaction(any(TransactionRecord.class)))
                    .thenReturn(expectedTransaction);

            TransactionRecord result = saleService.refund(saleId);

            assertThat(result).isNotNull();
            assertThat(result.amount()).isEqualTo(2700.0);
            assertThat(sale.getRefunded()).isTrue();

            verify(productVariationRepository).incrementStock(product.getId(), 2);
            verify(saleRepository).save(sale);
            verify(transactionService).createTransaction(any());
        }

        @Test
        void refund_ThrowsException_WhenAlreadyRefunded() {
            sale.setRefunded(true);

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));

            assertThatThrownBy(() -> saleService.refund(saleId)).isInstanceOf(RuntimeException.class);
        }

        @Test
        void refund_ThrowsException_WhenNotCheckedOut() {
            sale.setTransactions(new ArrayList<>());

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));

            assertThatThrownBy(() -> saleService.refund(saleId)).isInstanceOf(RuntimeException.class);
        }
    }
}
