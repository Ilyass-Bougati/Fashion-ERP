-- Reset all stats and predictions tables.
-- Safe to run multiple times; TRUNCATE ... RESTART IDENTITY resets auto-increment sequences.

TRUNCATE TABLE
    financial_stat,
    sales_stat,
    employee_performance_stat,
    stock_stat,
    financial_prediction,
    sales_prediction,
    employee_performance_prediction,
    stock_prediction
RESTART IDENTITY CASCADE;
