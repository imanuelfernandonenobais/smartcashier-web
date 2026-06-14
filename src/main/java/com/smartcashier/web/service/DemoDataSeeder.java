package com.smartcashier.web.service;

import com.smartcashier.web.model.Customer;
import com.smartcashier.web.model.Product;
import com.smartcashier.web.model.ProductCategory;
import com.smartcashier.web.model.ProductUnitValue;
import com.smartcashier.web.model.PurchaseTransaction;
import com.smartcashier.web.model.PurchaseTransactionItem;
import com.smartcashier.web.model.SaleTransaction;
import com.smartcashier.web.model.SaleTransactionItem;
import com.smartcashier.web.model.Supplier;
import com.smartcashier.web.model.UnitType;
import com.smartcashier.web.model.User;
import com.smartcashier.web.model.UserRole;
import com.smartcashier.web.repository.CustomerRepository;
import com.smartcashier.web.repository.ProductCategoryRepository;
import com.smartcashier.web.repository.ProductRepository;
import com.smartcashier.web.repository.PurchaseTransactionRepository;
import com.smartcashier.web.repository.SaleTransactionRepository;
import com.smartcashier.web.repository.SupplierRepository;
import com.smartcashier.web.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final UserRepository userRepository;
    private final ProductCategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PurchaseTransactionRepository purchaseTransactionRepository;
    private final SaleTransactionRepository saleTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
            UserRepository userRepository,
            ProductCategoryRepository categoryRepository,
            SupplierRepository supplierRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            PurchaseTransactionRepository purchaseTransactionRepository,
            SaleTransactionRepository saleTransactionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.purchaseTransactionRepository = purchaseTransactionRepository;
        this.saleTransactionRepository = saleTransactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User admin = ensureUser("Administrator", "admin", "admin123", UserRole.ADMIN);
        User kasir = ensureUser("Kasir Demo", "kasir.demo", "kasir123", UserRole.KASIR);

        Map<String, ProductCategory> categories = seedCategories();
        Map<String, Supplier> suppliers = seedSuppliers();
        Map<String, Customer> customers = seedCustomers();
        Map<String, Product> products = seedProducts(categories);

        if (purchaseTransactionRepository.count() == 0 && !products.isEmpty() && !suppliers.isEmpty()) {
            seedPurchases(products, suppliers, admin, kasir);
        }

        if (saleTransactionRepository.count() == 0 && !products.isEmpty() && !customers.isEmpty()) {
            seedSales(products, customers, admin, kasir);
        }
    }

    private User ensureUser(String fullName, String username, String rawPassword, UserRole role) {
        return userRepository.findByUsernameIgnoreCase(username)
                .map(existing -> updateUser(existing, fullName, rawPassword, role))
                .orElseGet(() -> createUser(fullName, username, rawPassword, role));
    }

    private User updateUser(User user, String fullName, String rawPassword, UserRole role) {
        boolean dirty = false;

        if (user.getFullName() == null || user.getFullName().isBlank()) {
            user.setFullName(fullName);
            dirty = true;
        }

        if (user.getRole() != role) {
            user.setRole(role);
            dirty = true;
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            dirty = true;
        }

        if (!user.isEnabled()) {
            user.setEnabled(true);
            dirty = true;
        }

        return dirty ? userRepository.save(user) : user;
    }

    private User createUser(String fullName, String username, String rawPassword, UserRole role) {
        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Map<String, ProductCategory> seedCategories() {
        Map<String, ProductCategory> categories = indexCategories();

        ProductCategory katun = ensureCategory(categories, "Kain Katun");
        ProductCategory premium = ensureCategory(categories, "Kain Premium");
        ProductCategory pelapis = ensureCategory(categories, "Bahan Pelapis");
        ProductCategory kanvas = ensureCategory(categories, "Kanvas & Seragam");

        categories.put(katun.getName(), katun);
        categories.put(premium.getName(), premium);
        categories.put(pelapis.getName(), pelapis);
        categories.put(kanvas.getName(), kanvas);
        return categories;
    }

    private Map<String, Supplier> seedSuppliers() {
        Map<String, Supplier> suppliers = indexSuppliers();

        Supplier supplier1 = ensureSupplier(suppliers, "PT Tekstil Nusantara", "Jl. Industri Tekstil No. 18, Bandung", "022-86543210");
        Supplier supplier2 = ensureSupplier(suppliers, "CV Sumber Kain Bandung", "Jl. Cigondewah Kaler No. 25, Bandung", "022-6012345");
        Supplier supplier3 = ensureSupplier(suppliers, "UD Pelangi Fabric", "Jl. Otista Pasar Baru Blok C7, Jakarta", "021-55667788");
        Supplier supplier4 = ensureSupplier(suppliers, "Toko Grosir Bahan Jahit Murni", "Jl. Pahlawan No. 9, Surabaya", "031-77889900");

        suppliers.put(supplier1.getName(), supplier1);
        suppliers.put(supplier2.getName(), supplier2);
        suppliers.put(supplier3.getName(), supplier3);
        suppliers.put(supplier4.getName(), supplier4);
        return suppliers;
    }

    private Map<String, Customer> seedCustomers() {
        Map<String, Customer> customers = indexCustomers();

        Customer customer1 = ensureCustomer(customers, "Toko Busana Melati", "Jl. Sultan Agung No. 12, Yogyakarta", "0812-1100-2200");
        Customer customer2 = ensureCustomer(customers, "Konveksi Maju Jaya", "Jl. Raya Solo Km. 8, Klaten", "0813-2211-3344");
        Customer customer3 = ensureCustomer(customers, "Ibu Rina Tailor", "Perum Griya Asri Blok B2, Sleman", "0821-4567-8890");
        Customer customer4 = ensureCustomer(customers, "Sanggar Kebaya Laras", "Jl. Ahmad Yani No. 7, Semarang", "0851-9900-1122");
        Customer customer5 = ensureCustomer(customers, "UMKM Rumah Jahit Sari", "Jl. Cempaka No. 5, Magelang", "0819-7788-9911");

        customers.put(customer1.getName(), customer1);
        customers.put(customer2.getName(), customer2);
        customers.put(customer3.getName(), customer3);
        customers.put(customer4.getName(), customer4);
        customers.put(customer5.getName(), customer5);
        return customers;
    }

    private Map<String, Product> seedProducts(Map<String, ProductCategory> categories) {
        Map<String, Product> products = indexProducts();

        Product katunJepang = ensureProduct(
                products,
                "Kain Katun Jepang",
                categories.get("Kain Katun"),
                unit(UnitType.METER, "38000", "42"),
                unit(UnitType.ROLL, "650000", "12"),
                unit(UnitType.YARD, "36000", "28"),
                unit(UnitType.KG, "82000", "14")
        );

        Product drillSeragam = ensureProduct(
                products,
                "Kain Drill Seragam",
                categories.get("Kanvas & Seragam"),
                unit(UnitType.METER, "45000", "35"),
                unit(UnitType.ROLL, "780000", "11"),
                unit(UnitType.YARD, "42000", "24"),
                unit(UnitType.KG, "90000", "13")
        );

        Product linenPremium = ensureProduct(
                products,
                "Kain Linen Premium",
                categories.get("Kain Premium"),
                unit(UnitType.METER, "62000", "18"),
                unit(UnitType.ROLL, "1050000", "6"),
                unit(UnitType.YARD, "58000", "16"),
                unit(UnitType.KG, "130000", "10")
        );

        Product satinBridal = ensureProduct(
                products,
                "Kain Satin Bridal",
                categories.get("Kain Premium"),
                unit(UnitType.METER, "55000", "20"),
                unit(UnitType.ROLL, "930000", "10"),
                unit(UnitType.YARD, "51000", "7"),
                unit(UnitType.KG, "115000", "12")
        );

        Product furingAsahi = ensureProduct(
                products,
                "Furing Asahi",
                categories.get("Bahan Pelapis"),
                unit(UnitType.METER, "18000", "50"),
                unit(UnitType.ROLL, "300000", "5"),
                unit(UnitType.YARD, "17000", "30"),
                unit(UnitType.KG, "60000", "8")
        );

        Product kanvasTebal = ensureProduct(
                products,
                "Kain Kanvas Tebal",
                categories.get("Kanvas & Seragam"),
                unit(UnitType.METER, "68000", "9"),
                unit(UnitType.ROLL, "1180000", "10"),
                unit(UnitType.YARD, "64000", "15"),
                unit(UnitType.KG, "145000", "11")
        );

        products.put(katunJepang.getName(), katunJepang);
        products.put(drillSeragam.getName(), drillSeragam);
        products.put(linenPremium.getName(), linenPremium);
        products.put(satinBridal.getName(), satinBridal);
        products.put(furingAsahi.getName(), furingAsahi);
        products.put(kanvasTebal.getName(), kanvasTebal);
        return products;
    }

    private void seedPurchases(Map<String, Product> products, Map<String, Supplier> suppliers, User admin, User kasir) {
        LocalDate today = LocalDate.now();

        savePurchase(
                today.minusDays(3).atTime(8, 30),
                1,
                suppliers.get("PT Tekstil Nusantara"),
                admin,
                purchaseItem(products.get("Kain Katun Jepang"), UnitType.ROLL, "4", "580000"),
                purchaseItem(products.get("Furing Asahi"), UnitType.ROLL, "6", "250000")
        );

        savePurchase(
                today.minusDays(2).atTime(10, 10),
                1,
                suppliers.get("CV Sumber Kain Bandung"),
                kasir,
                purchaseItem(products.get("Kain Drill Seragam"), UnitType.ROLL, "3", "690000"),
                purchaseItem(products.get("Kain Linen Premium"), UnitType.ROLL, "2", "920000")
        );

        savePurchase(
                today.atTime(8, 15),
                1,
                suppliers.get("UD Pelangi Fabric"),
                admin,
                purchaseItem(products.get("Kain Satin Bridal"), UnitType.ROLL, "2", "820000"),
                purchaseItem(products.get("Kain Kanvas Tebal"), UnitType.ROLL, "1", "1020000")
        );

        savePurchase(
                today.atTime(13, 45),
                2,
                suppliers.get("Toko Grosir Bahan Jahit Murni"),
                kasir,
                purchaseItem(products.get("Furing Asahi"), UnitType.KG, "12", "48000"),
                purchaseItem(products.get("Kain Katun Jepang"), UnitType.KG, "10", "70000")
        );
    }

    private void seedSales(Map<String, Product> products, Map<String, Customer> customers, User admin, User kasir) {
        LocalDate today = LocalDate.now();

        saveSale(
                today.minusDays(3).atTime(10, 5),
                1,
                customers.get("Toko Busana Melati"),
                kasir,
                money("500000"),
                saleItem(products.get("Kain Katun Jepang"), UnitType.METER, "8", "38000"),
                saleItem(products.get("Furing Asahi"), UnitType.METER, "10", "18000")
        );

        saveSale(
                today.minusDays(1).atTime(16, 20),
                1,
                customers.get("Konveksi Maju Jaya"),
                admin,
                money("1000000"),
                saleItem(products.get("Kain Drill Seragam"), UnitType.METER, "12", "45000"),
                saleItem(products.get("Kain Linen Premium"), UnitType.METER, "6", "62000")
        );

        saveSale(
                today.atTime(9, 15),
                1,
                customers.get("Ibu Rina Tailor"),
                kasir,
                money("400000"),
                saleItem(products.get("Kain Satin Bridal"), UnitType.YARD, "5", "51000"),
                saleItem(products.get("Furing Asahi"), UnitType.YARD, "7", "17000")
        );

        saveSale(
                today.atTime(11, 40),
                2,
                customers.get("Sanggar Kebaya Laras"),
                admin,
                money("700000"),
                saleItem(products.get("Kain Kanvas Tebal"), UnitType.METER, "4", "68000"),
                saleItem(products.get("Kain Linen Premium"), UnitType.YARD, "6", "58000")
        );

        saveSale(
                today.atTime(15, 10),
                3,
                customers.get("UMKM Rumah Jahit Sari"),
                kasir,
                money("1000000"),
                saleItem(products.get("Kain Katun Jepang"), UnitType.ROLL, "1", "650000"),
                saleItem(products.get("Kain Drill Seragam"), UnitType.YARD, "8", "42000")
        );
    }

    private PurchaseTransaction savePurchase(LocalDateTime transactionTime, int sequence, Supplier supplier, User user, TransactionItemSeed... items) {
        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.setCode(buildCode("PB", transactionTime.toLocalDate(), sequence));
        transaction.setTransactionTime(transactionTime);
        transaction.setSupplier(supplier);
        transaction.setUser(user);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (TransactionItemSeed seed : items) {
            PurchaseTransactionItem item = new PurchaseTransactionItem();
            item.setProduct(seed.product());
            item.setUnitType(seed.unitType());
            item.setQuantity(seed.quantity());
            item.setUnitPrice(seed.unitPrice());
            item.setSubtotal(seed.subtotal());
            transaction.addItem(item);
            totalAmount = totalAmount.add(seed.subtotal());
        }

        transaction.setTotalAmount(totalAmount);
        return purchaseTransactionRepository.save(transaction);
    }

    private SaleTransaction saveSale(LocalDateTime transactionTime, int sequence, Customer customer, User user, BigDecimal amountPaid, TransactionItemSeed... items) {
        SaleTransaction transaction = new SaleTransaction();
        transaction.setCode(buildCode("PJ", transactionTime.toLocalDate(), sequence));
        transaction.setTransactionTime(transactionTime);
        transaction.setCustomer(customer);
        transaction.setUser(user);
        transaction.setAmountPaid(amountPaid);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (TransactionItemSeed seed : items) {
            SaleTransactionItem item = new SaleTransactionItem();
            item.setProduct(seed.product());
            item.setUnitType(seed.unitType());
            item.setQuantity(seed.quantity());
            item.setUnitPrice(seed.unitPrice());
            item.setSubtotal(seed.subtotal());
            transaction.addItem(item);
            totalAmount = totalAmount.add(seed.subtotal());
        }

        transaction.setTotalAmount(totalAmount);
        transaction.setChangeAmount(amountPaid.subtract(totalAmount));
        return saleTransactionRepository.save(transaction);
    }

    private ProductCategory saveCategory(String name) {
        ProductCategory category = new ProductCategory();
        category.setName(name);
        return categoryRepository.save(category);
    }

    private ProductCategory ensureCategory(Map<String, ProductCategory> categories, String name) {
        ProductCategory category = categories.get(name);
        return category != null ? category : saveCategory(name);
    }

    private Supplier saveSupplier(String name, String address, String phoneNumber) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setAddress(address);
        supplier.setPhoneNumber(phoneNumber);
        return supplierRepository.save(supplier);
    }

    private Supplier ensureSupplier(Map<String, Supplier> suppliers, String name, String address, String phoneNumber) {
        Supplier supplier = suppliers.get(name);
        return supplier != null ? supplier : saveSupplier(name, address, phoneNumber);
    }

    private Customer saveCustomer(String name, String address, String phoneNumber) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setAddress(address);
        customer.setPhoneNumber(phoneNumber);
        return customerRepository.save(customer);
    }

    private Customer ensureCustomer(Map<String, Customer> customers, String name, String address, String phoneNumber) {
        Customer customer = customers.get(name);
        return customer != null ? customer : saveCustomer(name, address, phoneNumber);
    }

    private Product saveProduct(String name, ProductCategory category, UnitSeed... units) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);

        for (UnitSeed seed : units) {
            ProductUnitValue unitValue = new ProductUnitValue();
            unitValue.setUnitType(seed.unitType());
            unitValue.setSalePrice(seed.salePrice());
            unitValue.setStockQuantity(seed.stockQuantity());
            product.addUnitValue(unitValue);
        }

        return productRepository.save(product);
    }

    private Product ensureProduct(Map<String, Product> products, String name, ProductCategory category, UnitSeed... units) {
        Product product = products.get(name);
        return product != null ? product : saveProduct(name, category, units);
    }

    private Map<String, ProductCategory> indexCategories() {
        Map<String, ProductCategory> categories = new LinkedHashMap<>();
        for (ProductCategory category : categoryRepository.findAll()) {
            categories.putIfAbsent(category.getName(), category);
        }
        return categories;
    }

    private Map<String, Supplier> indexSuppliers() {
        Map<String, Supplier> suppliers = new LinkedHashMap<>();
        for (Supplier supplier : supplierRepository.findAll()) {
            suppliers.putIfAbsent(supplier.getName(), supplier);
        }
        return suppliers;
    }

    private Map<String, Customer> indexCustomers() {
        Map<String, Customer> customers = new LinkedHashMap<>();
        for (Customer customer : customerRepository.findAll()) {
            customers.putIfAbsent(customer.getName(), customer);
        }
        return customers;
    }

    private Map<String, Product> indexProducts() {
        Map<String, Product> products = new LinkedHashMap<>();
        for (Product product : productRepository.findAll()) {
            products.putIfAbsent(product.getName(), product);
        }
        return products;
    }

    private String buildCode(String prefix, LocalDate date, int sequence) {
        return prefix + "-" + date.format(DATE_FORMATTER) + "-" + String.format("%03d", sequence);
    }

    private UnitSeed unit(UnitType unitType, String salePrice, String stockQuantity) {
        return new UnitSeed(unitType, money(salePrice), quantity(stockQuantity));
    }

    private TransactionItemSeed purchaseItem(Product product, UnitType unitType, String quantity, String unitPrice) {
        return item(product, unitType, quantity(quantity), money(unitPrice));
    }

    private TransactionItemSeed saleItem(Product product, UnitType unitType, String quantity, String unitPrice) {
        return item(product, unitType, quantity(quantity), money(unitPrice));
    }

    private TransactionItemSeed item(Product product, UnitType unitType, BigDecimal quantity, BigDecimal unitPrice) {
        return new TransactionItemSeed(product, unitType, quantity, unitPrice, quantity.multiply(unitPrice));
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private BigDecimal quantity(String value) {
        return new BigDecimal(value);
    }

    private record UnitSeed(UnitType unitType, BigDecimal salePrice, BigDecimal stockQuantity) {
    }

    private record TransactionItemSeed(
            Product product,
            UnitType unitType,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
