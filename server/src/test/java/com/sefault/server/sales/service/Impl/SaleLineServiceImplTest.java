package com.sefault.server.sales.service.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.exception.InsufficientStockException;
import com.sefault.server.sales.dto.record.SaleLineRecord;
import com.sefault.server.sales.entity.Sale;
import com.sefault.server.sales.entity.SaleLine;
import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.sales.mapper.SaleLineMapper;
import com.sefault.server.sales.repository.SaleLineRepository;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.storage.entity.ProductVariation;
import com.sefault.server.storage.repository.ProductVariationRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleLineServiceImpl Unit Tests")
class SaleLineServiceImplTest {

    @Mock
    private SaleLineRepository saleLineRepository;

    @Mock
    private SaleLineMapper saleLineMapper;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductVariationRepository productVariationRepository;

    @InjectMocks
    private SaleLineServiceImpl saleLineService;

    private SaleLineId id;
    private SaleLine saleLine;
    private SaleLineRecord record;
    private ProductVariation product;
    private Sale sale;

    @BeforeEach
    void setUp() {
        UUID saleId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        id = new SaleLineId(saleId, productId);

        product = new ProductVariation();
        product.setId(productId);
        product.setQuantity(10);
        product.setSku("TEST-SKU");

        sale = new Sale();
        sale.setId(saleId);

        saleLine = new SaleLine();
        saleLine.setId(id);
        saleLine.setQuantity(2);
        saleLine.setProductVariation(product);

        record = new SaleLineRecord(id, 2, saleId, productId, 50.0);
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        void create_success() {
            when(productVariationRepository.findById(id.getProductVariationId()))
                    .thenReturn(Optional.of(product));
            when(saleLineMapper.toEntity(any())).thenReturn(saleLine);
            when(saleRepository.getReferenceById(id.getSaleId())).thenReturn(sale);
            when(saleLineRepository.save(any())).thenReturn(saleLine);
            when(saleLineMapper.entityToRecord(any())).thenReturn(record);

            SaleLineRecord result = saleLineService.create(record);

            assertThat(result).isNotNull();
            assertThat(product.getQuantity()).isEqualTo(8);
            verify(productVariationRepository).save(product);
        }

        @Test
        void create_insufficientStock() {
            SaleLineRecord massiveOrder = new SaleLineRecord(id, 20, id.getSaleId(), id.getProductVariationId(), 50.0);
            when(productVariationRepository.findById(id.getProductVariationId()))
                    .thenReturn(Optional.of(product));

            assertThatThrownBy(() -> saleLineService.create(massiveOrder))
                    .isInstanceOf(InsufficientStockException.class);

            verify(productVariationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        void delete_restoresStock() {
            when(saleLineRepository.findById(id)).thenReturn(Optional.of(saleLine));

            saleLineService.delete(id);

            verify(productVariationRepository).incrementStock(product.getId(), 2);
            verify(saleLineRepository).deleteByCompositeId(any(UUID.class), any(UUID.class));
        }
    }
}
