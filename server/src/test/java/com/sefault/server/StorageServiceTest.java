package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.storage.dto.projection.ProductCategoryProjection;
import com.sefault.server.storage.dto.projection.ProductProjection;
import com.sefault.server.storage.dto.projection.ProductVariationProjection;
import com.sefault.server.storage.dto.projection.VendorProjection;
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

        // record: ProductCategoryRecord(UUID id, String name, String description)
        private ProductCategoryRecord sampleRecord;

        @BeforeEach
        void setUp() {
            existingId = UUID.randomUUID();
            nonExistingId = UUID.randomUUID();
            sampleRecord = new ProductCategoryRecord(existingId, "Electronics", "Electronic products");
        }

        // --- getById ---

        @Test
        @DisplayName("getById – returns projection with correct fields when category exists")
        void getById_existingId_returnsProjection() {
            ProductCategoryProjection projection = mock(ProductCategoryProjection.class);
            when(projection.getId()).thenReturn(existingId);
            when(projection.getName()).thenReturn("Electronics");
            when(projection.getDescription()).thenReturn("Electronic products");
            when(productCategoryService.getById(existingId)).thenReturn(projection);

            ProductCategoryProjection result = productCategoryService.getById(existingId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(existingId);
            assertThat(result.getName()).isEqualTo("Electronics");
            assertThat(result.getDescription()).isEqualTo("Electronic products");
            verify(productCategoryService).getById(existingId);
        }

        @Test
        @DisplayName("getById – throws NotFoundException when category does not exist")
        void getById_nonExistingId_throwsNotFoundException() {
            when(productCategoryService.getById(nonExistingId))
                    .thenThrow(new NotFoundException("Product category not found by id : " + nonExistingId));

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
            when(productCategoryService.update(sampleRecord)).thenReturn(updated);

            ProductCategoryRecord result = productCategoryService.update(sampleRecord);

            assertThat(result.name()).isEqualTo("Updated Name");
            assertThat(result.description()).isEqualTo("Updated Desc");
            verify(productCategoryService).update(sampleRecord);
        }

        @Test
        @DisplayName("update – throws NotFoundException when id is null")
        void update_nullId_throwsNotFoundException() {
            ProductCategoryRecord noId = new ProductCategoryRecord(null, "Name", "Desc");
            when(productCategoryService.update(noId))
                    .thenThrow(new NotFoundException("Product category not found by id : null"));

            assertThatThrownBy(() -> productCategoryService.update(noId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("update – throws NotFoundException when category does not exist")
        void update_nonExistingId_throwsNotFoundException() {
            ProductCategoryRecord missing = new ProductCategoryRecord(nonExistingId, "Name", "Desc");
            when(productCategoryService.update(missing))
                    .thenThrow(new NotFoundException("Product category not found by id : " + nonExistingId));

            assertThatThrownBy(() -> productCategoryService.update(missing))
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
            doThrow(new NotFoundException("Product category not found by id : " + nonExistingId))
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

        // record: ProductRecord(UUID id, String name, UUID productCategoryId, UUID imageId,
        //                       LocalDateTime createdAt, LocalDateTime updatedAt)
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
        @DisplayName("getById – returns projection with correct fields when product exists")
        void getById_existingId_returnsProjection() {
            ProductProjection projection = mock(ProductProjection.class);
            when(projection.getId()).thenReturn(existingId);
            when(projection.getName()).thenReturn("Laptop");
            when(projection.getProductCategoryId()).thenReturn(categoryId);
            when(projection.getImageId()).thenReturn(imageId);
            when(projection.getCreatedAt()).thenReturn(now);
            when(projection.getUpdatedAt()).thenReturn(now);
            when(productService.getById(existingId)).thenReturn(projection);

            ProductProjection result = productService.getById(existingId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(existingId);
            assertThat(result.getName()).isEqualTo("Laptop");
            assertThat(result.getProductCategoryId()).isEqualTo(categoryId);
            assertThat(result.getImageId()).isEqualTo(imageId);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
            verify(productService).getById(existingId);
        }

        @Test
        @DisplayName("getById – throws NotFoundException when product does not exist")
        void getById_nonExistingId_throwsNotFoundException() {
            when(productService.getById(nonExistingId))
                    .thenThrow(new NotFoundException("Product not found by id : " + nonExistingId));

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
            when(productService.update(sampleRecord)).thenReturn(updated);

            ProductRecord result = productService.update(sampleRecord);

            assertThat(result.name()).isEqualTo("Gaming Laptop");
            verify(productService).update(sampleRecord);
        }

        @Test
        @DisplayName("update – throws NotFoundException when id is null")
        void update_nullId_throwsNotFoundException() {
            ProductRecord noId = new ProductRecord(null, "Laptop", categoryId, imageId, null, null);
            when(productService.update(noId)).thenThrow(new NotFoundException("Product not found by id : null"));

            assertThatThrownBy(() -> productService.update(noId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("update – throws NotFoundException when product does not exist")
        void update_nonExistingId_throwsNotFoundException() {
            ProductRecord missing = new ProductRecord(nonExistingId, "Laptop", categoryId, imageId, null, null);
            when(productService.update(missing))
                    .thenThrow(new NotFoundException("Product not found by id : " + nonExistingId));

            assertThatThrownBy(() -> productService.update(missing))
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
            doThrow(new NotFoundException("Product not found by id : " + nonExistingId))
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

        // record: ProductVariationRecord(UUID id, String sku, Double price,
        //                                UUID productId, Integer quantity,
        //                                UUID imageId, LocalDateTime createdAt, LocalDateTime updatedAt)
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
        @DisplayName("getById – returns projection with correct fields when variation exists")
        void getById_existingId_returnsProjection() {
            ProductVariationProjection projection = mock(ProductVariationProjection.class);
            when(projection.getId()).thenReturn(existingId);
            when(projection.getSku()).thenReturn("SKU-001");
            when(projection.getPrice()).thenReturn(299.99);
            when(projection.getProductId()).thenReturn(productId);
            when(projection.getQuantity()).thenReturn(50);
            when(projection.getImageId()).thenReturn(imageId);
            when(projection.getCreatedAt()).thenReturn(now);
            when(projection.getUpdatedAt()).thenReturn(now);
            when(productVariationService.getById(existingId)).thenReturn(projection);

            ProductVariationProjection result = productVariationService.getById(existingId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(existingId);
            assertThat(result.getSku()).isEqualTo("SKU-001");
            assertThat(result.getPrice()).isEqualTo(299.99);
            assertThat(result.getProductId()).isEqualTo(productId);
            assertThat(result.getQuantity()).isEqualTo(50);
            assertThat(result.getImageId()).isEqualTo(imageId);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
            verify(productVariationService).getById(existingId);
        }

        @Test
        @DisplayName("getById – throws NotFoundException when variation does not exist")
        void getById_nonExistingId_throwsNotFoundException() {
            when(productVariationService.getById(nonExistingId))
                    .thenThrow(new NotFoundException("Product variation not found by id : " + nonExistingId));

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
            when(productVariationService.update(sampleRecord)).thenReturn(updated);

            ProductVariationRecord result = productVariationService.update(sampleRecord);

            assertThat(result.sku()).isEqualTo("SKU-001-V2");
            assertThat(result.price()).isEqualTo(349.99);
            assertThat(result.quantity()).isEqualTo(30);
            verify(productVariationService).update(sampleRecord);
        }

        @Test
        @DisplayName("update – throws NotFoundException when id is null")
        void update_nullId_throwsNotFoundException() {
            ProductVariationRecord noId =
                    new ProductVariationRecord(null, "SKU-001", 299.99, productId, 50, imageId, null, null);
            when(productVariationService.update(noId))
                    .thenThrow(new NotFoundException("Product variation not found by id : null"));

            assertThatThrownBy(() -> productVariationService.update(noId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("update – throws NotFoundException when variation does not exist")
        void update_nonExistingId_throwsNotFoundException() {
            ProductVariationRecord missing =
                    new ProductVariationRecord(nonExistingId, "SKU-001", 299.99, productId, 50, imageId, null, null);
            when(productVariationService.update(missing))
                    .thenThrow(new NotFoundException("Product variation not found by id : " + nonExistingId));

            assertThatThrownBy(() -> productVariationService.update(missing))
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
            doThrow(new NotFoundException("Product variation not found by id : " + nonExistingId))
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

        // record: VendorRecord(UUID id, String companyName, String email, String contactName,
        //                      String phoneNumber, String paymentTerms, Boolean active,
        //                      UUID productId, LocalDateTime createdAt, LocalDateTime updatedAt)
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
        @DisplayName("findAllPaginated – returns page of vendors with correct content")
        void findAllPaginated_returnsPageOfVendors() {
            VendorProjection projection = mock(VendorProjection.class);
            when(projection.getId()).thenReturn(existingId);
            when(projection.getCompanyName()).thenReturn("Acme Corp");
            when(projection.getEmail()).thenReturn("contact@acme.com");
            when(projection.getContactName()).thenReturn("John Doe");
            when(projection.getPhoneNumber()).thenReturn("+1-555-0100");
            when(projection.getPaymentTerms()).thenReturn("NET30");
            when(projection.getActive()).thenReturn(true);
            when(projection.getProductId()).thenReturn(productId);
            Pageable pageable = PageRequest.of(0, 10);
            Page<VendorProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
            when(vendorService.findAllPaginated(pageable)).thenReturn(page);

            Page<VendorProjection> result = vendorService.findAllPaginated(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            VendorProjection first = result.getContent().get(0);
            assertThat(first.getId()).isEqualTo(existingId);
            assertThat(first.getCompanyName()).isEqualTo("Acme Corp");
            assertThat(first.getEmail()).isEqualTo("contact@acme.com");
            assertThat(first.getContactName()).isEqualTo("John Doe");
            assertThat(first.getPhoneNumber()).isEqualTo("+1-555-0100");
            assertThat(first.getPaymentTerms()).isEqualTo("NET30");
            assertThat(first.getActive()).isTrue();
            assertThat(first.getProductId()).isEqualTo(productId);
            verify(vendorService).findAllPaginated(pageable);
        }

        @Test
        @DisplayName("findAllPaginated – returns empty page when no vendors exist")
        void findAllPaginated_noVendors_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(vendorService.findAllPaginated(pageable)).thenReturn(Page.empty());

            Page<VendorProjection> result = vendorService.findAllPaginated(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("findAllPaginated – respects page and size parameters")
        void findAllPaginated_respectsPageSize() {
            VendorProjection projection = mock(VendorProjection.class);
            Pageable firstPage = PageRequest.of(0, 5);
            Page<VendorProjection> page = new PageImpl<>(List.of(projection, projection, projection), firstPage, 8);
            when(vendorService.findAllPaginated(firstPage)).thenReturn(page);

            Page<VendorProjection> result = vendorService.findAllPaginated(firstPage);

            assertThat(result.getTotalElements()).isEqualTo(8);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isZero();
        }

        // --- getById ---

        @Test
        @DisplayName("getById – returns projection with all fields when vendor exists")
        void getById_existingId_returnsProjection() {
            VendorProjection projection = mock(VendorProjection.class);
            when(projection.getId()).thenReturn(existingId);
            when(projection.getCompanyName()).thenReturn("Acme Corp");
            when(projection.getEmail()).thenReturn("contact@acme.com");
            when(projection.getContactName()).thenReturn("John Doe");
            when(projection.getPhoneNumber()).thenReturn("+1-555-0100");
            when(projection.getPaymentTerms()).thenReturn("NET30");
            when(projection.getActive()).thenReturn(true);
            when(projection.getProductId()).thenReturn(productId);
            when(projection.getCreatedAt()).thenReturn(now);
            when(projection.getUpdatedAt()).thenReturn(now);
            when(vendorService.getById(existingId)).thenReturn(projection);

            VendorProjection result = vendorService.getById(existingId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(existingId);
            assertThat(result.getCompanyName()).isEqualTo("Acme Corp");
            assertThat(result.getEmail()).isEqualTo("contact@acme.com");
            assertThat(result.getContactName()).isEqualTo("John Doe");
            assertThat(result.getPhoneNumber()).isEqualTo("+1-555-0100");
            assertThat(result.getPaymentTerms()).isEqualTo("NET30");
            assertThat(result.getActive()).isTrue();
            assertThat(result.getProductId()).isEqualTo(productId);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
            verify(vendorService).getById(existingId);
        }

        @Test
        @DisplayName("getById – throws NotFoundException when vendor does not exist")
        void getById_nonExistingId_throwsNotFoundException() {
            when(vendorService.getById(nonExistingId))
                    .thenThrow(new NotFoundException("Vendor not found by id : " + nonExistingId));

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
            when(vendorService.update(sampleRecord)).thenReturn(updated);

            VendorRecord result = vendorService.update(sampleRecord);

            assertThat(result.companyName()).isEqualTo("Acme Corp v2");
            assertThat(result.email()).isEqualTo("new@acme.com");
            assertThat(result.active()).isFalse();
            assertThat(result.paymentTerms()).isEqualTo("NET60");
            verify(vendorService).update(sampleRecord);
        }

        @Test
        @DisplayName("update – throws NotFoundException when id is null")
        void update_nullId_throwsNotFoundException() {
            VendorRecord noId = new VendorRecord(
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
            when(vendorService.update(noId)).thenThrow(new NotFoundException("Vendor not found by id : null"));

            assertThatThrownBy(() -> vendorService.update(noId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("null");
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
            when(vendorService.update(missing))
                    .thenThrow(new NotFoundException("Vendor not found by id : " + nonExistingId));

            assertThatThrownBy(() -> vendorService.update(missing))
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
            doThrow(new NotFoundException("Vendor not found by id : " + nonExistingId))
                    .when(vendorService)
                    .delete(nonExistingId);

            assertThatThrownBy(() -> vendorService.delete(nonExistingId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(nonExistingId.toString());
        }
    }
}
