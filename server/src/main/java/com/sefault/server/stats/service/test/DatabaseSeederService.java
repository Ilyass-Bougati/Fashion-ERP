package com.sefault.server.stats.service.test;

import com.sefault.server.finance.entity.*;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.repository.*;
import com.sefault.server.hr.entity.Employee;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.image.entity.Image;
import com.sefault.server.image.repository.ImageRepository;
import com.sefault.server.sales.entity.*;
import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.storage.entity.*;
import com.sefault.server.storage.repository.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DatabaseSeederService {

    private final ImageRepository imageRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariationRepository variationRepository;
    private final SaleRepository saleRepository;
    private final TransactionRepository transactionRepository;
    private final PayrollRepository payrollRepository;
    private final FixChargeRepository fixChargeRepository;

    private final Faker faker = new Faker();
    private final Random random = new Random();

    @Transactional
    public void seedDatabase() {
        System.out.println("🚀 Starting Database Seeding...");

        Image dummyImage = Image.builder()
                .objectKey("seed/dummy-product-image.jpg")
                .bucketName("erp-assets")
                .contentType("image/jpeg")
                .build();
        dummyImage = imageRepository.save(dummyImage);
        imageRepository.flush(); // 💥 Catch any Image constraint violation here
        System.out.println("✅ [1/9] Image saved & flushed. id=" + dummyImage.getId());

        ProductCategory category = ProductCategory.builder()
                .name("Apparel")
                .description("Clothing and accessories")
                .build();
        category = categoryRepository.save(category);
        categoryRepository.flush();
        System.out.println("✅ [2a/9] ProductCategory saved & flushed.");

        FixCharge rent = new FixCharge();
        rent.setName("Monthly Rent");
        rent.setDescription("Storefront rent for main location");
        rent.setAmount(1500.00);
        rent.setActive(true);
        fixChargeRepository.save(rent);
        fixChargeRepository.flush();
        System.out.println("✅ [2b/9] FixCharge saved & flushed.");

        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Employee emp = new Employee();
            emp.setFirstName(faker.name().firstName());
            emp.setLastName(faker.name().lastName());
            emp.setEmail("seed.employee." + i + "@sefault.com");
            emp.setPhoneNumber("060000000" + i);
            emp.setCIN("CIN-SEED-000" + i);
            emp.setActive(true);
            emp.setSalary(faker.number().randomDouble(2, 3000, 8000)); // @Positive ✓
            emp.setCommission(0.05);
            emp.setHiredAt(LocalDateTime.now().minusMonths(random.nextInt(24) + 1));
            emp.setTerminatedAt(null);

            emp = employeeRepository.save(emp);
            employeeRepository.flush();
            employees.add(emp);
            System.out.println("  ➕ Employee[" + i + "] saved. id=" + emp.getId());
        }
        System.out.println("✅ [3/9] All 5 Employees saved & flushed.");

        List<ProductVariation> variations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Product product = Product.builder()
                    .name(faker.commerce().productName())
                    .productCategory(category)
                    .image(dummyImage)
                    .build();
            product = productRepository.save(product);
            productRepository.flush();
            System.out.println("  ➕ Product[" + i + "] saved. id=" + product.getId());

            ProductVariation pv = ProductVariation.builder()
                    .product(product)
                    .sku("SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .price(faker.number().randomDouble(2, 20, 200))
                    .quantity(faker.number().numberBetween(10, 100))
                    .build();
            pv = variationRepository.save(pv);
            variationRepository.flush();
            variations.add(pv);
            System.out.println("  ➕ ProductVariation[" + i + "] saved. sku=" + pv.getSku());
        }
        System.out.println("✅ [4+5/9] Products & ProductVariations saved & flushed.");

        for (int i = 0; i < 30; i++) {
            Employee randomEmp = employees.get(random.nextInt(employees.size()));
            ProductVariation randomPv = variations.get(random.nextInt(variations.size()));

            Sale sale = new Sale();
            sale.setEmployee(randomEmp);
            sale.setDiscount(random.nextDouble() > 0.8 ? 0.10 : 0.0);
            sale.setRefunded(false);
            sale = saleRepository.save(sale);
            saleRepository.flush();

            SaleLineId saleLineId = new SaleLineId(sale.getId(), randomPv.getId());
            SaleLine line = new SaleLine();
            line.setId(saleLineId);
            line.setSale(sale);
            line.setProductVariation(randomPv);
            line.setQuantity(faker.number().numberBetween(1, 4));
            line.setSaleAtPrice(randomPv.getPrice());

            sale.getSaleLines().add(line);
            saleRepository.save(sale);
            saleRepository.flush();

            double lineTotal = line.getQuantity() * line.getSaleAtPrice();
            double netTotal = lineTotal * (1.0 - sale.getDiscount());

            Transaction tx = Transaction.builder()
                    .type(TransactionType.PAID)
                    .sale(sale)
                    .amount(netTotal)
                    .build();
            tx = transactionRepository.save(tx);
            transactionRepository.flush();

            Payroll payroll = Payroll.builder()
                    .employee(randomEmp)
                    .transaction(tx)
                    .salary(randomEmp.getSalary())
                    .commission(netTotal * randomEmp.getCommission())
                    .build();
            payrollRepository.save(payroll);
            payrollRepository.flush();

            System.out.println("  ✔ Sale[" + i + "] complete. net=€" + String.format("%.2f", netTotal));
        }

        System.out.println("✅ [6–9/9] Sales, SaleLines, Transactions & Payrolls saved & flushed.");
        System.out.println("🎉 Database Seeding Complete — 5 employees, 10 variations, 30 sales.");
    }
}
