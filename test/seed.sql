-- Fashion ERP – Database Seed
-- Mirrors seed.py but inserts data directly via SQL, with sales and
-- transactions spread across a wide time range (2023-01-01 → 2025-12-31).
--
-- Volume summary:
--   50 images · 10 categories · 91 products · ~640 variations
--   30 employees · 2 000 sales · 60 manual transactions · monthly payroll (36 months)
--   15 additional user accounts
--
-- Prerequisites:
--   • PostgreSQL with pgcrypto extension available
--   • The Spring Boot application has been started at least once so that
--     Hibernate has created all tables.
--
-- Usage:
--   psql -U <user> -d fashion_erp -f seed.sql

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ── Guard: abort early if tables are missing ──────────────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'erp_users'
    ) THEN
        RAISE EXCEPTION
            'Tables not found – run the Spring Boot app once to initialise the schema, then re-run this file.';
    END IF;
END $$;

-- ── 1. Placeholder images (50) ────────────────────────────────────────────────
INSERT INTO image (id, object_key, bucket_name, content_type, created_at, updated_at)
SELECT
    gen_random_uuid(),
    'placeholders/img_' || LPAD(n::TEXT, 2, '0') || '.png',
    'fashion-erp',
    'image/png',
    NOW() - (random() * INTERVAL '730 days'),
    NOW()
FROM generate_series(1, 50) AS n;

-- ── 2-11. All reference + transactional data ──────────────────────────────────
DO $$
DECLARE
    -- collected ID arrays
    image_ids    UUID[];
    cat_ids      UUID[];
    prod_ids     UUID[];
    var_ids      UUID[];
    var_prices   FLOAT8[];
    emp_ids      UUID[];

    -- loop / scratch
    i            INT;
    j            INT;
    new_id       UUID;
    cat_idx      INT;
    prod_id      UUID;
    emp_id       UUID;
    img_id       UUID;
    var_id       UUID;
    var_idx      INT;
    sale_id      UUID;
    sale_date    TIMESTAMP;
    discount_val FLOAT8;
    status_val   TEXT;
    roll         FLOAT8;
    price_val    FLOAT8;
    sale_price   FLOAT8;
    qty_val      INT;
    total_amount FLOAT8;
    lines_added  INT;
    tx_date      TIMESTAMP;
    base_amt     FLOAT8;
    fname        TEXT;
    lname        TEXT;
    company      TEXT;
    slug         TEXT;
    sz           TEXT;
    col          TEXT;
    sku          TEXT;
    rec          RECORD;
    month_dt     DATE;
    sal_tx       UUID;
    pay_date     TIMESTAMP;

    -- counters
    sku_n        INT     := 1000;
    vendor_n     INT     := 1;
    isle_n       INT     := 1;
    phone_n      BIGINT  := 1100000000;

    -- ── static reference data ─────────────────────────────────────────────

    categories TEXT[][] := ARRAY[
        ARRAY['T-Shirts',    'Casual and formal t-shirts for every occasion and season'],
        ARRAY['Jeans',       'Denim jeans in multiple cuts, washes and fits'],
        ARRAY['Dresses',     'Elegant dresses ranging from casual to formal evening wear'],
        ARRAY['Jackets',     'Lightweight and heavy jackets for all weather conditions'],
        ARRAY['Shoes',       'Footwear collection spanning casual sneakers to formal heels'],
        ARRAY['Accessories', 'Belts, scarves, hats, bags and other fashion accessories'],
        ARRAY['Hoodies',     'Comfortable hoodies and sweatshirts for everyday wear'],
        ARRAY['Shorts',      'Summer shorts, bermudas and athletic bottoms'],
        ARRAY['Blazers',     'Tailored blazers and suits for a sharp professional look'],
        ARRAY['Activewear',  'Performance sportswear designed for training and lifestyle']
    ];

    -- products[i][1] = name, products[i][2] = category index (1-based)
    products TEXT[][] := ARRAY[
        -- T-Shirts (cat 1) – 10 items
        ARRAY['Classic White Tee',        '1'],
        ARRAY['Graphic Print Tee',        '1'],
        ARRAY['Polo Shirt',               '1'],
        ARRAY['V-Neck Tee',               '1'],
        ARRAY['Striped Tee',              '1'],
        ARRAY['Pocket Tee',               '1'],
        ARRAY['Long Sleeve Tee',          '1'],
        ARRAY['Henley Shirt',             '1'],
        ARRAY['Mock Neck Tee',            '1'],
        ARRAY['Tie-Dye Tee',             '1'],
        -- Jeans (cat 2) – 10 items
        ARRAY['Slim Fit Jeans',           '2'],
        ARRAY['Bootcut Jeans',            '2'],
        ARRAY['Skinny Jeans',             '2'],
        ARRAY['Wide Leg Jeans',           '2'],
        ARRAY['Cargo Jeans',              '2'],
        ARRAY['Straight Cut Jeans',       '2'],
        ARRAY['High Waist Jeans',         '2'],
        ARRAY['Tapered Jeans',            '2'],
        ARRAY['Ripped Jeans',             '2'],
        ARRAY['Relaxed Fit Jeans',        '2'],
        -- Dresses (cat 3) – 10 items
        ARRAY['Summer Floral Dress',      '3'],
        ARRAY['Evening Gown',             '3'],
        ARRAY['Casual Midi Dress',        '3'],
        ARRAY['Maxi Dress',               '3'],
        ARRAY['Mini Dress',               '3'],
        ARRAY['Wrap Dress',               '3'],
        ARRAY['Shirt Dress',              '3'],
        ARRAY['Bodycon Dress',            '3'],
        ARRAY['A-Line Dress',             '3'],
        ARRAY['Slip Dress',               '3'],
        -- Jackets (cat 4) – 9 items
        ARRAY['Leather Biker Jacket',     '4'],
        ARRAY['Denim Jacket',             '4'],
        ARRAY['Puffer Jacket',            '4'],
        ARRAY['Trench Coat',              '4'],
        ARRAY['Bomber Jacket',            '4'],
        ARRAY['Windbreaker',              '4'],
        ARRAY['Fleece Jacket',            '4'],
        ARRAY['Varsity Jacket',           '4'],
        ARRAY['Raincoat',                 '4'],
        -- Shoes (cat 5) – 11 items
        ARRAY['White Sneakers',           '5'],
        ARRAY['Ankle Boots',              '5'],
        ARRAY['Loafers',                  '5'],
        ARRAY['High Heels',               '5'],
        ARRAY['Running Shoes',            '5'],
        ARRAY['Oxford Shoes',             '5'],
        ARRAY['Slip-On Sneakers',         '5'],
        ARRAY['Chelsea Boots',            '5'],
        ARRAY['Mule Sandals',             '5'],
        ARRAY['Platform Shoes',           '5'],
        ARRAY['Derby Shoes',              '5'],
        -- Accessories (cat 6) – 11 items
        ARRAY['Leather Belt',             '6'],
        ARRAY['Silk Scarf',               '6'],
        ARRAY['Baseball Cap',             '6'],
        ARRAY['Sunglasses',               '6'],
        ARRAY['Tote Bag',                 '6'],
        ARRAY['Crossbody Bag',            '6'],
        ARRAY['Leather Wallet',           '6'],
        ARRAY['Classic Watch',            '6'],
        ARRAY['Hair Clip Set',            '6'],
        ARRAY['Bucket Hat',               '6'],
        ARRAY['Knit Gloves',              '6'],
        -- Hoodies (cat 7) – 8 items
        ARRAY['Zip-Up Hoodie',            '7'],
        ARRAY['Pullover Hoodie',          '7'],
        ARRAY['Cropped Hoodie',           '7'],
        ARRAY['Oversized Hoodie',         '7'],
        ARRAY['Fleece Hoodie',            '7'],
        ARRAY['Quarter-Zip Hoodie',       '7'],
        ARRAY['Tie-Dye Hoodie',           '7'],
        ARRAY['Ribbed Hoodie',            '7'],
        -- Shorts (cat 8) – 7 items
        ARRAY['Chino Shorts',             '8'],
        ARRAY['Denim Shorts',             '8'],
        ARRAY['Athletic Shorts',          '8'],
        ARRAY['Linen Shorts',             '8'],
        ARRAY['Cargo Shorts',             '8'],
        ARRAY['Swim Shorts',              '8'],
        ARRAY['Paperbag Shorts',          '8'],
        -- Blazers (cat 9) – 6 items
        ARRAY['Single Breasted Blazer',   '9'],
        ARRAY['Double Breasted Blazer',   '9'],
        ARRAY['Checked Blazer',           '9'],
        ARRAY['Velvet Blazer',            '9'],
        ARRAY['Linen Blazer',             '9'],
        ARRAY['Oversized Blazer',         '9'],
        -- Activewear (cat 10) – 9 items
        ARRAY['Sports Leggings',          '10'],
        ARRAY['Training Top',             '10'],
        ARRAY['Running Jacket',           '10'],
        ARRAY['Compression Shorts',       '10'],
        ARRAY['Sports Bra',               '10'],
        ARRAY['Yoga Pants',               '10'],
        ARRAY['Tank Top',                 '10'],
        ARRAY['Cycling Shorts',           '10'],
        ARRAY['Sports Hoodie',            '10']
    ];

    sizes  TEXT[] := ARRAY['XS','S','M','L','XL','XXL'];
    colors TEXT[] := ARRAY['BLACK','WHITE','NAVY','RED','GREEN','GREY','BEIGE','BLUE','KHAKI','BROWN','PINK','PURPLE','ORANGE','YELLOW','BURGUNDY'];

    companies TEXT[] := ARRAY[
        'FashionHub Ltd',    'StyleSource Inc',   'TrendFactory SA',    'GlobalTextiles Co',
        'ModaSupply AG',     'PrimeCloth GmbH',   'EliteWear Partners', 'UrbanFabric LLC',
        'ChicSuppliers',     'LuxeTextile Group', 'FabricNation Ltd',   'TrendSetters Co',
        'CoutureSupply',     'ModeExpress',        'StyleBridge LLC',    'NovaTex SA',
        'FastFabric Inc',    'EcoCloth Co',        'PremiumWear Ltd',    'FreshMode SAS',
        'DesignForce Ltd',   'SilkRoad Textiles',  'MetroFashion GmbH',  'AlphaCloth Inc',
        'VogueSupply Co',    'ArtisanFabrics AG',  'TrendWave Ltd',      'PureFiber SAS',
        'ClothingBridge',    'TextileVision GmbH'
    ];

    payment_terms TEXT[] := ARRAY['Net 30','Net 60','Net 15','2/10 Net 30','COD','Net 45','EOM'];

    first_names TEXT[] := ARRAY[
        'Youssef', 'Fatima',   'Mohammed', 'Aicha',    'Omar',    'Khadija',
        'Hassan',  'Zineb',    'Rachid',   'Samira',   'Khalid',  'Nadia',
        'Mehdi',   'Soukaina', 'Amine',    'Houda',    'Tarik',   'Laila',
        'Bilal',   'Meryem',   'Karim',    'Sara',     'Hamza',   'Leila',
        'Adil',    'Imane',    'Younes',   'Hajar',    'Saad',    'Chaimae',
        'Redouane','Widad',    'Hicham',   'Btissam',  'Walid',   'Loubna'
    ];

    last_names TEXT[] := ARRAY[
        'Benkhalil', 'Alami',   'Benali',   'Tazi',    'Chraibi', 'Fassi',
        'Ouali',     'Berrada', 'Rhazi',    'Lamrani', 'Kadiri',  'Mekki',
        'Bouazza',   'Hajji',   'El Amri',  'Slaoui',  'Idrissi', 'Lahlou',
        'Mansouri',  'Cherkaoui','Bougati', 'Tlemcani','Bensouda','Merini',
        'Ziani',     'Qasimi',  'Fennich',  'Oujda',   'Sefrioui','Benkirane'
    ];

    discounts FLOAT8[] := ARRAY[0.0, 0.0, 0.0, 0.05, 0.10, 0.15, 0.20, 0.25];

    -- Manual (non-sale) transaction templates: type, base amount (60 entries)
    tx_types TEXT[] := ARRAY[
        'PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED',
        'PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED',
        'PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED',
        'PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED',
        'PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED',
        'PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED','PAID','RECEIVED'
    ];
    tx_amounts FLOAT8[] := ARRAY[
         2500.0,  5000.0,   800.0,  1200.0,  3200.0,   750.0,  1500.0,  4300.0,   600.0,   900.0,
         2100.0,  3600.0,   450.0,  1800.0,  1100.0,  7500.0,  2200.0,  4800.0,   350.0,  6100.0,
         1750.0,  3300.0,   980.0,  2600.0,  5400.0,   420.0,  1950.0,  8200.0,   670.0,  3900.0,
         1300.0,  4100.0,   550.0,  2900.0,  6700.0,   830.0,  2400.0,  5600.0,   740.0,  1600.0,
         3800.0,  7100.0,   490.0,  2150.0,  4500.0,  1050.0,  3100.0,  6300.0,   880.0,  2700.0,
         5200.0,  1400.0,   960.0,  3450.0,  7800.0,   510.0,  2850.0,  4700.0,  1250.0,  5900.0
    ];

BEGIN

    -- ── Collect image IDs inserted above ─────────────────────────────────
    SELECT ARRAY(SELECT id FROM image ORDER BY created_at) INTO image_ids;

    -- ── 3. Product categories ─────────────────────────────────────────────
    cat_ids := ARRAY[]::UUID[];
    FOR i IN 1..array_length(categories, 1) LOOP
        new_id := gen_random_uuid();
        INSERT INTO product_category (id, name, description)
        VALUES (new_id, categories[i][1], categories[i][2]);
        cat_ids := array_append(cat_ids, new_id);
    END LOOP;

    -- ── 4. Products ───────────────────────────────────────────────────────
    prod_ids := ARRAY[]::UUID[];
    FOR i IN 1..array_length(products, 1) LOOP
        new_id  := gen_random_uuid();
        cat_idx := products[i][2]::INT;
        img_id  := image_ids[1 + (random() * (array_length(image_ids, 1) - 1))::INT];
        INSERT INTO product (id, name, product_category_id, image_id, created_at, updated_at)
        VALUES (new_id, products[i][1], cat_ids[cat_idx], img_id, NOW(), NOW());
        prod_ids := array_append(prod_ids, new_id);
    END LOOP;

    -- ── 5. Product variations (5-8 per product) ───────────────────────────
    var_ids    := ARRAY[]::UUID[];
    var_prices := ARRAY[]::FLOAT8[];
    FOR i IN 1..array_length(prod_ids, 1) LOOP
        prod_id := prod_ids[i];
        FOR j IN 1..(5 + (random() * 3)::INT) LOOP
            sz        := sizes [1 + (random() * (array_length(sizes,  1) - 1))::INT];
            col       := colors[1 + (random() * (array_length(colors, 1) - 1))::INT];
            sku       := 'SKU-' || LPAD(sku_n::TEXT, 5, '0') || '-' || sz || '-' || LEFT(col, 3);
            price_val := ROUND((12.0 + random() * 338.0)::NUMERIC, 2);
            new_id    := gen_random_uuid();
            img_id    := image_ids[1 + (random() * (array_length(image_ids, 1) - 1))::INT];
            INSERT INTO product_variation (id, sku, price, quantity, product_id, image_id, created_at, updated_at)
            VALUES (new_id, sku, price_val, 10 + (random() * 290)::INT, prod_id, img_id, NOW(), NOW());
            var_ids    := array_append(var_ids,    new_id);
            var_prices := array_append(var_prices, price_val);
            sku_n      := sku_n + 1;
        END LOOP;
    END LOOP;

    -- ── 6. Vendors (1-3 per product) ─────────────────────────────────────
    FOR i IN 1..array_length(prod_ids, 1) LOOP
        FOR j IN 1..(1 + (random() * 2)::INT) LOOP
            company := companies[1 + (random() * (array_length(companies, 1) - 1))::INT];
            slug    := LOWER(SPLIT_PART(company, ' ', 1));
            INSERT INTO vendor (id, company_name, email, contact_name, phone_number,
                                payment_terms, active, product_id, created_at, updated_at)
            VALUES (
                gen_random_uuid(),
                company,
                'orders.' || vendor_n || '@' || slug || '.com',
                'Purchasing Agent ' || vendor_n,
                (phone_n + vendor_n)::TEXT,
                payment_terms[1 + (random() * (array_length(payment_terms, 1) - 1))::INT],
                TRUE,
                prod_ids[i],
                NOW(), NOW()
            );
            vendor_n := vendor_n + 1;
        END LOOP;
    END LOOP;
    phone_n := phone_n + vendor_n + 1;

    -- ── 7. Employees (30) ─────────────────────────────────────────────────
    emp_ids := ARRAY[]::UUID[];
    FOR i IN 1..30 LOOP
        fname  := first_names[1 + (random() * (array_length(first_names, 1) - 1))::INT];
        lname  := last_names [1 + (random() * (array_length(last_names,  1) - 1))::INT];
        new_id := gen_random_uuid();
        img_id := image_ids[1 + (random() * (array_length(image_ids, 1) - 1))::INT];
        INSERT INTO employee (
            id, first_name, last_name, phone_number, cin, email,
            active, salary, commission, hired_at, image_id, created_at, updated_at
        ) VALUES (
            new_id,
            fname,
            lname,
            phone_n::TEXT,
            'AB' || (100000 + (random() * 899999)::INT)::TEXT,
            LOWER(fname) || '.' || LOWER(REPLACE(lname, ' ', '')) || i || '@fashion-erp.ma',
            TRUE,
            ROUND((2800.0 + random() * 6700.0)::NUMERIC, 2),
            ROUND((0.02  + random() * 0.16)::NUMERIC, 4),
            NOW() - ((90 + (random() * 1370)::INT) || ' days')::INTERVAL,
            img_id,
            NOW(), NOW()
        );
        emp_ids := array_append(emp_ids, new_id);
        phone_n := phone_n + 1;
    END LOOP;

    -- ── 8. Isles (1-3 per employee) ───────────────────────────────────────
    FOR i IN 1..array_length(emp_ids, 1) LOOP
        FOR j IN 1..(1 + (random() * 2)::INT) LOOP
            INSERT INTO isle (id, code, employee_id)
            VALUES (gen_random_uuid(), 'ISLE-' || LPAD(isle_n::TEXT, 3, '0'), emp_ids[i]);
            isle_n := isle_n + 1;
        END LOOP;
    END LOOP;

    -- ── 9. Fixed charges ─────────────────────────────────────────────────
    INSERT INTO fix_charge (id, name, description, amount, active, created_at) VALUES
        (gen_random_uuid(), 'Store Rent – Main Branch',   'Monthly rent for the flagship retail location downtown',      18500.0, TRUE,  NOW()),
        (gen_random_uuid(), 'Store Rent – Annexe',        'Monthly rent for the secondary outlet in the shopping mall',   9200.0, TRUE,  NOW()),
        (gen_random_uuid(), 'Electricity',                'Monthly electricity utility bill covering all store floors',    2800.0, TRUE,  NOW()),
        (gen_random_uuid(), 'Water & Sewage',             'Municipal water and sewage charges for all premises',            380.0, TRUE,  NOW()),
        (gen_random_uuid(), 'Internet & Telecoms',        'Fibre broadband plus business phone lines',                      950.0, TRUE,  NOW()),
        (gen_random_uuid(), 'Business Insurance',         'Annual liability and stock insurance policy (monthly slice)',   1400.0, TRUE,  NOW()),
        (gen_random_uuid(), 'CCTV & Security',            'Monthly subscription for monitored alarm and CCTV systems',      550.0, TRUE,  NOW()),
        (gen_random_uuid(), 'Professional Cleaning',      'Twice-weekly commercial cleaning contractor fees',                720.0, TRUE,  NOW()),
        (gen_random_uuid(), 'Accounting Software',        'Monthly SaaS licence for accounting and invoicing platform',     180.0, TRUE,  NOW()),
        (gen_random_uuid(), 'POS System Licence',         'Monthly point-of-sale software subscription per terminal',       240.0, TRUE,  NOW()),
        (gen_random_uuid(), 'Warehouse Rental',           'Off-site warehouse space for seasonal overflow inventory',      6200.0, TRUE,  NOW()),
        (gen_random_uuid(), 'Pest Control',               'Quarterly pest-control service contract amortised monthly',      120.0, FALSE, NOW()),
        (gen_random_uuid(), 'Window Display Maintenance', 'Monthly cost of external window-dressing service',               300.0, TRUE,  NOW());

    -- ── 10. Sales + sale lines + transactions ─────────────────────────────
    -- 2 000 sales distributed across 2023-01-01 … 2025-12-31.
    -- Status mix: 65 % COMPLETED, 12 % REFUNDED, 23 % PENDING (same as seed.py).
    -- Each completed/refunded sale generates a matching transaction entry.
    FOR i IN 1..2000 LOOP
        emp_id := emp_ids[1 + (random() * (array_length(emp_ids, 1) - 1))::INT];

        -- Random timestamp spanning the full 3-year window
        sale_date :=
            TIMESTAMP '2023-01-01 00:00:00'
            + (random()
               * EXTRACT(EPOCH FROM (TIMESTAMP '2026-01-01' - TIMESTAMP '2023-01-01'))
              ) * INTERVAL '1 second';

        discount_val := discounts[1 + (random() * (array_length(discounts, 1) - 1))::INT];

        roll := random();
        IF    roll < 0.65 THEN status_val := 'COMPLETED';
        ELSIF roll < 0.77 THEN status_val := 'REFUNDED';
        ELSE                   status_val := 'PENDING';
        END IF;

        sale_id := gen_random_uuid();
        INSERT INTO sale (id, discount, status, employee_id, created_at, updated_at)
        VALUES (sale_id, discount_val, status_val, emp_id, sale_date, sale_date + INTERVAL '1 minute');

        -- Insert 2-8 sale lines, skipping duplicate variations per sale
        lines_added := 0;
        FOR j IN 1..(2 + (random() * 6)::INT) LOOP
            var_idx   := 1 + (random() * (array_length(var_ids, 1) - 1))::INT;
            var_id    := var_ids   [var_idx];
            price_val := var_prices[var_idx];
            sale_price := GREATEST(ROUND((price_val * (0.85 + random() * 0.20))::NUMERIC, 2), 0.01);
            qty_val    := 1 + (random() * 4)::INT;
            BEGIN
                INSERT INTO sale_line (sale_id, product_variation_id, quantity, sale_at_price)
                VALUES (sale_id, var_id, qty_val, sale_price);
                lines_added := lines_added + 1;
            EXCEPTION WHEN unique_violation THEN
                NULL; -- same variation already in this sale – skip
            END;
        END LOOP;

        -- Create transactions only for sales that have lines
        IF lines_added > 0 AND status_val IN ('COMPLETED', 'REFUNDED') THEN
            SELECT ROUND((SUM(sl.quantity * sl.sale_at_price) * (1.0 - discount_val))::NUMERIC, 2)
            INTO   total_amount
            FROM   sale_line sl
            WHERE  sl.sale_id = sale_id;

            -- Revenue received at point of sale
            INSERT INTO transaction (id, type, sale_id, amount, created_at)
            VALUES (gen_random_uuid(), 'RECEIVED', sale_id, total_amount, sale_date);

            -- Refunds go out a few hours later
            IF status_val = 'REFUNDED' THEN
                INSERT INTO transaction (id, type, sale_id, amount, created_at)
                VALUES (gen_random_uuid(), 'PAID', sale_id, total_amount, sale_date + INTERVAL '3 hours');
            END IF;
        END IF;
    END LOOP;

    -- ── 11. Payroll (monthly, 2023-01 → 2025-12, all employees) ─────────
    FOR rec IN SELECT id, salary, commission FROM employee LOOP
        FOR month_dt IN
            SELECT d::DATE
            FROM generate_series(
                DATE '2023-01-01',
                DATE '2025-12-01',
                INTERVAL '1 month'
            ) d
        LOOP
            -- Pay on the 28th of each month
            pay_date := (month_dt + 27)::TIMESTAMP;
            sal_tx   := gen_random_uuid();

            INSERT INTO transaction (id, type, sale_id, amount, created_at)
            VALUES (
                sal_tx,
                'PAID',
                NULL,
                ROUND((rec.salary * (0.95 + random() * 0.10))::NUMERIC, 2),
                pay_date
            );

            INSERT INTO payroll (id, salary, commission, transaction_id, employee_id, created_at)
            VALUES (
                gen_random_uuid(),
                rec.salary,
                rec.commission,
                sal_tx,
                rec.id,
                pay_date
            );
        END LOOP;
    END LOOP;

    -- ── 12. Standalone manual transactions (wide time spread) ────────────
    FOR i IN 1..array_length(tx_types, 1) LOOP
        tx_date  := TIMESTAMP '2023-01-01 00:00:00'
                  + (random()
                     * EXTRACT(EPOCH FROM (TIMESTAMP '2026-01-01' - TIMESTAMP '2023-01-01'))
                    ) * INTERVAL '1 second';
        base_amt := tx_amounts[i];
        INSERT INTO transaction (id, type, sale_id, amount, created_at)
        VALUES (
            gen_random_uuid(),
            tx_types[i],
            NULL,
            ROUND((base_amt * (0.80 + random() * 0.50))::NUMERIC, 2),
            tx_date
        );
    END LOOP;

END $$;

-- ── 13. Additional user accounts ──────────────────────────────────────────────
-- Passwords are hashed with Blowfish (bf) via pgcrypto, compatible with
-- Spring Security's BCryptPasswordEncoder.  Plain-text password: "password123"
INSERT INTO erp_users (id, first_name, last_name, email, password, phone_number, active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Ilyass',   'Bougati',   'ilyass.bougati@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0612345678', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Sara',     'Alaoui',    'sara.alaoui@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0623456789', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Karim',    'Mansouri',  'karim.mansouri@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0634567890', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Leila',    'Cherkaoui', 'leila.cherkaoui@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0645678901', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Hamza',    'Bensouda',  'hamza.bensouda@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0656789012', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Adil',     'Ziani',     'adil.ziani@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0667890123', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Imane',    'Qasimi',    'imane.qasimi@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0678901234', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Younes',   'Fennich',   'younes.fennich@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0689012345', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Hajar',    'Merini',    'hajar.merini@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0690123456', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Redouane', 'Sefrioui',  'redouane.sefrioui@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0601234567', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Widad',    'Benkirane', 'widad.benkirane@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0611234567', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Hicham',   'Tlemcani',  'hicham.tlemcani@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0622234567', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Btissam',  'Oujda',     'btissam.oujda@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0633234567', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Walid',    'Idrissi',   'walid.idrissi@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0644234567', TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'Loubna',   'Lahlou',    'loubna.lahlou@fashion-erp.ma',
     crypt('password123', gen_salt('bf', 10)), '0655234567', TRUE, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

COMMIT;
