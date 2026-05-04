package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.storage.dto.record.ProductCategoryRecord;
import com.sefault.server.storage.dto.record.ProductRecord;
import com.sefault.server.storage.dto.record.ProductVariationRecord;
import com.sefault.server.storage.dto.record.VendorRecord;
import com.sefault.server.storage.service.ProductCategoryService;
import com.sefault.server.storage.service.ProductService;
import com.sefault.server.storage.service.ProductVariationService;
import com.sefault.server.storage.service.VendorService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("Storage Service Tests")
class StorageServiceTest {

    // =========================================================================
    // ProductCategoryService
    // =========================================================================

    @Nested
    @DisplayName("ProductCategoryService")
    class ProductCategoryServiceTests {

        @Mock
        private ProductCategoryService productCategoryService;

        private UUID existingId;
        private UUID nonExistingId;

        private ProductCategoryRecord sampleRecord;

        @BeforeEach
        void setUp() {
            existingId = UUID.randomUUID();
            nonExistingId = UUID.randomUUID();
            sampleRecord = new ProductCategoryRecord(existingId, "Electronics", "Electronic products");
        }

        // --- getById ---

        @Test
        @DisplayName("getById – returns record with correct fields when category exists")
        void getById_existingId_returnsRecord() {
            when(productCategoryService.getById(existingId)).thenReturn(sampleRecord);

            ProductCategoryRecord result = productCategoryService.getById(existingId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.name()).isEqualTo("Electronics");
            assertThat(result.description()).isEqualTo("Electronic products");
            verify(productCategoryService).getById(existingId);
        }

        @Test
        @DisplayName("getById – throws NotFoundException when category does not exist")
        void getById_nonExistingId_throwsNotFoundException() {
            when(productCategoryService.getById(nonExistingId))
                    .thenThrow(new NotFoundException("Product category not found with id : " + nonExistingId));

            assertThatThrownBy(() -> productCategoryService.getById(nonExistingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }

        // --- save ---

        @Test
        @DisplayName("save – persists and returns record with generated id")
        void save_validRecord_returnsSavedRecord() {
            ProductCategoryRecord input = new ProductCategoryRecord(null, "Electronics", "Electronic products");
            when(productCategoryService.save(input)).thenReturn(sampleRecord);

            ProductCategoryRecord result = productCategoryService.save(input);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.name()).isEqualTo("Electronics");
            assertThat(result.description()).isEqualTo("Electronic products");
            verify(productCategoryService).save(input);
        }

        @Test
        @DisplayName("save – called exactly once with provided record")
        void save_calledOnce() {
            productCategoryService.save(sampleRecord);
            verify(productCategoryService, times(1)).save(sampleRecord);
        }

        // --- update ---

        @Test
        @DisplayName("update – updates and returns record when category exists")
        void update_existingRecord_returnsUpdatedRecord() {
            ProductCategoryRecord updated = new ProductCategoryRecord(existingId, "Updated Name", "Updated Desc");
            when(productCategoryService.update(existingId, sampleRecord)).thenReturn(updated);

            ProductCategoryRecord result = productCategoryService.update(existingId, sampleRecord);

            assertThat(result.name()).isEqualTo("Updated Name");
            assertThat(result.description()).isEqualTo("Updated Desc");
            verify(productCategoryService).update(existingId, sampleRecord);
        }

        @Test
        @DisplayName("update – throws NotFoundException when category does not exist")
        void update_nonExistingId_throwsNotFoundException() {
            ProductCategoryRecord updated = new ProductCategoryRecord(nonExistingId, "Name", "Desc");
            when(productCategoryService.update(nonExistingId, updated))
                    .thenThrow(new NotFoundException("Product category not found with id : " + nonExistingId));

            assertThatThrownBy(() -> productCategoryService.update(nonExistingId, updated))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }

        // --- delete ---

        @Test
        @DisplayName("delete – completes without error when category exists")
        void delete_existingId_deletesSuccessfully() {
            doNothing().when(productCategoryService).delete(existingId);

            productCategoryService.delete(existingId);

            verify(productCategoryService).delete(existingId);
        }

        @Test
        @DisplayName("delete – throws NotFoundException when category does not exist")
        void delete_nonExistingId_throwsNotFoundException() {
            doThrow(new NotFoundException("Product category not found with id : " + nonExistingId))
                    .when(productCategoryService)
                    .delete(nonExistingId);

            assertThatThrownBy(() -> productCategoryService.delete(nonExistingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }
    }

    // =========================================================================
    // ProductService
    // =========================================================================

    @Nested
    @DisplayName("ProductService")
    class ProductServiceTests {

        @Mock
        private ProductService productService;

        private UUID existingId;
        private UUID nonExistingId;
        private UUID categoryId;
        private UUID imageId;
        private LocalDateTime now;

        private ProductRecord sampleRecord;

        @BeforeEach
        void setUp() {
            existingId = UUID.randomUUID();
            nonExistingId = UUID.randomUUID();
            categoryId = UUID.randomUUID();
            imageId = UUID.randomUUID();
            now = LocalDateTime.now();
            sampleRecord = new ProductRecord(existingId, "Laptop", categoryId, imageId, now, now);
        }

        // --- getById ---

        @Test
        @DisplayName("getById – returns record with correct fields when product exists")
        void getById_existingId_returnsRecord() {
            when(productService.getById(existingId)).thenReturn(sampleRecord);

            ProductRecord result = productService.getById(existingId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.name()).isEqualTo("Laptop");
            assertThat(result.productCategoryId()).isEqualTo(categoryId);
            assertThat(result.imageId()).isEqualTo(imageId);
            assertThat(result.createdAt()).isEqualTo(now);
            assertThat(result.updatedAt()).isEqualTo(now);
            verify(productService).getById(existingId);
        }

        @Test
        @DisplayName("getById – throws NotFoundException when product does not exist")
        void getById_nonExistingId_throwsNotFoundException() {
            when(productService.getById(nonExistingId))
                    .thenThrow(new NotFoundException("Product not found with id : " + nonExistingId));

            assertThatThrownBy(() -> productService.getById(nonExistingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }

        // --- save ---

        @Test
        @DisplayName("save – persists and returns record with generated id and timestamps")
        void save_validRecord_returnsSavedRecord() {
            ProductRecord input = new ProductRecord(null, "Laptop", categoryId, imageId, null, null);
            when(productService.save(input)).thenReturn(sampleRecord);

            ProductRecord result = productService.save(input);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.name()).isEqualTo("Laptop");
            assertThat(result.productCategoryId()).isEqualTo(categoryId);
            assertThat(result.imageId()).isEqualTo(imageId);
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
            verify(productService).save(input);
        }

        @Test
        @DisplayName("save – null imageId is accepted (image is optional)")
        void save_nullImageId_savedWithoutImage() {
            ProductRecord noImage = new ProductRecord(null, "Laptop", categoryId, null, null, null);
            ProductRecord saved = new ProductRecord(existingId, "Laptop", categoryId, null, now, now);
            when(productService.save(noImage)).thenReturn(saved);

            ProductRecord result = productService.save(noImage);

            assertThat(result.imageId()).isNull();
        }

        // --- update ---

        @Test
        @DisplayName("update – updates and returns record when product exists")
        void update_existingRecord_returnsUpdatedRecord() {
            ProductRecord updated = new ProductRecord(existingId, "Gaming Laptop", categoryId, imageId, now, now);
            when(productService.update(existingId, sampleRecord)).thenReturn(updated);

            ProductRecord result = productService.update(existingId, sampleRecord);

            assertThat(result.name()).isEqualTo("Gaming Laptop");
            verify(productService).update(existingId, sampleRecord);
        }

        @Test
        @DisplayName("update – throws NotFoundException when product does not exist")
        void update_nonExistingId_throwsNotFoundException() {
            ProductRecord missing = new ProductRecord(nonExistingId, "Laptop", categoryId, imageId, null, null);
            when(productService.update(nonExistingId, missing))
                    .thenThrow(new NotFoundException("Product not found with id : " + nonExistingId));

            assertThatThrownBy(() -> productService.update(nonExistingId, missing))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }

        // --- delete ---

        @Test
        @DisplayName("delete – completes without error when product exists")
        void delete_existingId_deletesSuccessfully() {
            doNothing().when(productService).delete(existingId);

            productService.delete(existingId);

            verify(productService).delete(existingId);
        }

        @Test
        @DisplayName("delete – throws NotFoundException when product does not exist")
        void delete_nonExistingId_throwsNotFoundException() {
            doThrow(new NotFoundException("Product not found with id : " + nonExistingId))
                    .when(productService)
                    .delete(nonExistingId);

            assertThatThrownBy(() -> productService.delete(nonExistingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }
    }

    // =========================================================================
    // ProductVariationService
    // =========================================================================

    @Nested
    @DisplayName("ProductVariationService")
    class ProductVariationServiceTests {

        @Mock
        private ProductVariationService productVariationService;

        private UUID existingId;
        private UUID nonExistingId;
        private UUID productId;
        private UUID imageId;
        private LocalDateTime now;

        private ProductVariationRecord sampleRecord;

        @BeforeEach
        void setUp() {
            existingId = UUID.randomUUID();
            nonExistingId = UUID.randomUUID();
            productId = UUID.randomUUID();
            imageId = UUID.randomUUID();
            now = LocalDateTime.now();
            sampleRecord = new ProductVariationRecord(existingId, "SKU-001", 299.99, productId, 50, imageId, now, now);
        }

        // --- getById ---

        @Test
        @DisplayName("getById – returns record with correct fields when variation exists")
        void getById_existingId_returnsRecord() {
            when(productVariationService.getById(existingId)).thenReturn(sampleRecord);

            ProductVariationRecord result = productVariationService.getById(existingId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.sku()).isEqualTo("SKU-001");
            assertThat(result.price()).isEqualTo(299.99);
            assertThat(result.productId()).isEqualTo(productId);
            assertThat(result.quantity()).isEqualTo(50);
            assertThat(result.imageId()).isEqualTo(imageId);
            assertThat(result.createdAt()).isEqualTo(now);
            assertThat(result.updatedAt()).isEqualTo(now);
            verify(productVariationService).getById(existingId);
        }

        @Test
        @DisplayName("getById – throws NotFoundException when variation does not exist")
        void getById_nonExistingId_throwsNotFoundException() {
            when(productVariationService.getById(nonExistingId))
                    .thenThrow(new NotFoundException("Product variation not found with id : " + nonExistingId));

            assertThatThrownBy(() -> productVariationService.getById(nonExistingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }

        // --- save ---

        @Test
        @DisplayName("save – persists and returns record with generated id and timestamps")
        void save_validRecord_returnsSavedRecord() {
            ProductVariationRecord input =
                    new ProductVariationRecord(null, "SKU-001", 299.99, productId, 50, imageId, null, null);
            when(productVariationService.save(input)).thenReturn(sampleRecord);

            ProductVariationRecord result = productVariationService.save(input);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.sku()).isEqualTo("SKU-001");
            assertThat(result.price()).isEqualTo(299.99);
            assertThat(result.quantity()).isEqualTo(50);
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
            verify(productVariationService).save(input);
        }

        @Test
        @DisplayName("save – null productId and null imageId are accepted (both optional)")
        void save_nullProductIdAndImageId_savedSuccessfully() {
            ProductVariationRecord noRefs =
                    new ProductVariationRecord(null, "SKU-002", 99.99, null, 10, null, null, null);
            ProductVariationRecord saved =
                    new ProductVariationRecord(existingId, "SKU-002", 99.99, null, 10, null, now, now);
            when(productVariationService.save(noRefs)).thenReturn(saved);

            ProductVariationRecord result = productVariationService.save(noRefs);

            assertThat(result.productId()).isNull();
            assertThat(result.imageId()).isNull();
        }

        // --- update ---

        @Test
        @DisplayName("update – updates and returns record when variation exists")
        void update_existingRecord_returnsUpdatedRecord() {
            ProductVariationRecord updated =
                    new ProductVariationRecord(existingId, "SKU-001-V2", 349.99, productId, 30, imageId, now, now);
            when(productVariationService.update(existingId, sampleRecord)).thenReturn(updated);

            ProductVariationRecord result = productVariationService.update(existingId, sampleRecord);

            assertThat(result.sku()).isEqualTo("SKU-001-V2");
            assertThat(result.price()).isEqualTo(349.99);
            assertThat(result.quantity()).isEqualTo(30);
            verify(productVariationService).update(existingId, sampleRecord);
        }

        @Test
        @DisplayName("update – throws NotFoundException when variation does not exist")
        void update_nonExistingId_throwsNotFoundException() {
            ProductVariationRecord missing =
                    new ProductVariationRecord(nonExistingId, "SKU-001", 299.99, productId, 50, imageId, null, null);
            when(productVariationService.update(nonExistingId, missing))
                    .thenThrow(new NotFoundException("Product variation not found with id : " + nonExistingId));

            assertThatThrownBy(() -> productVariationService.update(nonExistingId, missing))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }

        // --- delete ---

        @Test
        @DisplayName("delete – completes without error when variation exists")
        void delete_existingId_deletesSuccessfully() {
            doNothing().when(productVariationService).delete(existingId);

            productVariationService.delete(existingId);

            verify(productVariationService).delete(existingId);
        }

        @Test
        @DisplayName("delete – throws NotFoundException when variation does not exist")
        void delete_nonExistingId_throwsNotFoundException() {
            doThrow(new NotFoundException("Product variation not found with id : " + nonExistingId))
                    .when(productVariationService)
                    .delete(nonExistingId);

            assertThatThrownBy(() -> productVariationService.delete(nonExistingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }
    }

    // =========================================================================
    // VendorService
    // =========================================================================

    @Nested
    @DisplayName("VendorService")
    class VendorServiceTests {

        @Mock
        private VendorService vendorService;

        private UUID existingId;
        private UUID nonExistingId;
        private UUID productId;
        private LocalDateTime now;

        private VendorRecord sampleRecord;

        @BeforeEach
        void setUp() {
            existingId = UUID.randomUUID();
            nonExistingId = UUID.randomUUID();
            productId = UUID.randomUUID();
            now = LocalDateTime.now();
            sampleRecord = new VendorRecord(
                    existingId,
                    "Acme Corp",
                    "contact@acme.com",
                    "John Doe",
                    "+1-555-0100",
                    "NET30",
                    true,
                    productId,
                    now,
                    now);
        }

        // --- findAllPaginated ---

        @Test
        @DisplayName("findAllPaginated – returns page of vendor records with correct content")
        void findAllPaginated_returnsPageOfVendors() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<VendorRecord> page = new PageImpl<>(List.of(sampleRecord), pageable, 1);
            when(vendorService.findAllPaginated(pageable)).thenReturn(page);

            Page<VendorRecord> result = vendorService.findAllPaginated(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            VendorRecord first = result.getContent().get(0);
            assertThat(first.id()).isEqualTo(existingId);
            assertThat(first.companyName()).isEqualTo("Acme Corp");
            assertThat(first.email()).isEqualTo("contact@acme.com");
            assertThat(first.contactName()).isEqualTo("John Doe");
            assertThat(first.phoneNumber()).isEqualTo("+1-555-0100");
            assertThat(first.paymentTerms()).isEqualTo("NET30");
            assertThat(first.active()).isTrue();
            assertThat(first.productId()).isEqualTo(productId);
            verify(vendorService).findAllPaginated(pageable);
        }

        @Test
        @DisplayName("findAllPaginated – returns empty page when no vendors exist")
        void findAllPaginated_noVendors_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(vendorService.findAllPaginated(pageable)).thenReturn(Page.empty());

            Page<VendorRecord> result = vendorService.findAllPaginated(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("findAllPaginated – respects page and size parameters")
        void findAllPaginated_respectsPageSize() {
            Pageable firstPage = PageRequest.of(0, 5);
            Page<VendorRecord> page = new PageImpl<>(List.of(sampleRecord, sampleRecord, sampleRecord), firstPage, 8);
            when(vendorService.findAllPaginated(firstPage)).thenReturn(page);

            Page<VendorRecord> result = vendorService.findAllPaginated(firstPage);

            assertThat(result.getTotalElements()).isEqualTo(8);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isZero();
        }

        // --- getById ---

        @Test
        @DisplayName("getById – returns record with all fields when vendor exists")
        void getById_existingId_returnsRecord() {
            when(vendorService.getById(existingId)).thenReturn(sampleRecord);

            VendorRecord result = vendorService.getById(existingId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.companyName()).isEqualTo("Acme Corp");
            assertThat(result.email()).isEqualTo("contact@acme.com");
            assertThat(result.contactName()).isEqualTo("John Doe");
            assertThat(result.phoneNumber()).isEqualTo("+1-555-0100");
            assertThat(result.paymentTerms()).isEqualTo("NET30");
            assertThat(result.active()).isTrue();
            assertThat(result.productId()).isEqualTo(productId);
            assertThat(result.createdAt()).isEqualTo(now);
            assertThat(result.updatedAt()).isEqualTo(now);
            verify(vendorService).getById(existingId);
        }

        @Test
        @DisplayName("getById – throws NotFoundException when vendor does not exist")
        void getById_nonExistingId_throwsNotFoundException() {
            when(vendorService.getById(nonExistingId))
                    .thenThrow(new NotFoundException("Vendor not found with id : " + nonExistingId));

            assertThatThrownBy(() -> vendorService.getById(nonExistingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }

        // --- save ---

        @Test
        @DisplayName("save – persists and returns record with generated id and timestamps")
        void save_validRecord_returnsSavedRecord() {
            VendorRecord input = new VendorRecord(
                    null,
                    "Acme Corp",
                    "contact@acme.com",
                    "John Doe",
                    "+1-555-0100",
                    "NET30",
                    true,
                    productId,
                    null,
                    null);
            when(vendorService.save(input)).thenReturn(sampleRecord);

            VendorRecord result = vendorService.save(input);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.companyName()).isEqualTo("Acme Corp");
            assertThat(result.email()).isEqualTo("contact@acme.com");
            assertThat(result.contactName()).isEqualTo("John Doe");
            assertThat(result.phoneNumber()).isEqualTo("+1-555-0100");
            assertThat(result.paymentTerms()).isEqualTo("NET30");
            assertThat(result.active()).isTrue();
            assertThat(result.productId()).isEqualTo(productId);
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
            verify(vendorService).save(input);
        }

        @Test
        @DisplayName("save – null productId is accepted (product is optional)")
        void save_nullProductId_savedWithoutProduct() {
            VendorRecord noProduct = new VendorRecord(
                    null, "Acme Corp", "contact@acme.com", "John Doe", "+1-555-0100", "NET30", true, null, null, null);
            VendorRecord saved = new VendorRecord(
                    existingId,
                    "Acme Corp",
                    "contact@acme.com",
                    "John Doe",
                    "+1-555-0100",
                    "NET30",
                    true,
                    null,
                    now,
                    now);
            when(vendorService.save(noProduct)).thenReturn(saved);

            VendorRecord result = vendorService.save(noProduct);

            assertThat(result.productId()).isNull();
        }

        @Test
        @DisplayName("save – inactive vendor is persisted with active=false")
        void save_inactiveVendor_returnsSavedRecord() {
            VendorRecord inactive = new VendorRecord(
                    null, "Old Corp", "old@corp.com", "Jane Smith", "+1-555-0200", "NET60", false, null, null, null);
            VendorRecord saved = new VendorRecord(
                    existingId,
                    "Old Corp",
                    "old@corp.com",
                    "Jane Smith",
                    "+1-555-0200",
                    "NET60",
                    false,
                    null,
                    now,
                    now);
            when(vendorService.save(inactive)).thenReturn(saved);

            VendorRecord result = vendorService.save(inactive);

            assertThat(result.active()).isFalse();
            assertThat(result.paymentTerms()).isEqualTo("NET60");
        }

        // --- update ---

        @Test
        @DisplayName("update – updates and returns record when vendor exists")
        void update_existingRecord_returnsUpdatedRecord() {
            VendorRecord updated = new VendorRecord(
                    existingId,
                    "Acme Corp v2",
                    "new@acme.com",
                    "Jane Doe",
                    "+1-555-0199",
                    "NET60",
                    false,
                    productId,
                    now,
                    now);
            when(vendorService.update(existingId, sampleRecord)).thenReturn(updated);

            VendorRecord result = vendorService.update(existingId, sampleRecord);

            assertThat(result.companyName()).isEqualTo("Acme Corp v2");
            assertThat(result.email()).isEqualTo("new@acme.com");
            assertThat(result.active()).isFalse();
            assertThat(result.paymentTerms()).isEqualTo("NET60");
            verify(vendorService).update(existingId, sampleRecord);
        }

        @Test
        @DisplayName("update – throws NotFoundException when vendor does not exist")
        void update_nonExistingId_throwsNotFoundException() {
            VendorRecord missing = new VendorRecord(
                    nonExistingId,
                    "Acme Corp",
                    "contact@acme.com",
                    "John Doe",
                    "+1-555-0100",
                    "NET30",
                    true,
                    productId,
                    null,
                    null);
            when(vendorService.update(nonExistingId, missing))
                    .thenThrow(new NotFoundException("Vendor not found with id : " + nonExistingId));

            assertThatThrownBy(() -> vendorService.update(nonExistingId, missing))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }

        // --- delete ---

        @Test
        @DisplayName("delete – completes without error when vendor exists")
        void delete_existingId_deletesSuccessfully() {
            doNothing().when(vendorService).delete(existingId);

            vendorService.delete(existingId);

            verify(vendorService).delete(existingId);
        }

        @Test
        @DisplayName("delete – throws NotFoundException when vendor does not exist")
        void delete_nonExistingId_throwsNotFoundException() {
            doThrow(new NotFoundException("Vendor not found with id : " + nonExistingId))
                    .when(vendorService)
                    .delete(nonExistingId);

            assertThatThrownBy(() -> vendorService.delete(nonExistingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }
    }
}
