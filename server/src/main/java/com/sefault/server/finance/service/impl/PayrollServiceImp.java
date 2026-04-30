package com.sefault.server.finance.service.impl;

import com.sefault.server.finance.dto.record.PayrollRecord;
import com.sefault.server.finance.entity.Payroll;
import com.sefault.server.finance.entity.Transaction;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.mapper.PayrollMapper;
import com.sefault.server.finance.repository.PayrollRepository;
import com.sefault.server.finance.repository.TransactionRepository;
import com.sefault.server.finance.service.PayrollService;
import com.sefault.server.hr.dto.projection.EmployeeProjection;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.sales.repository.SaleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PayrollServiceImp implements PayrollService {
    private final TransactionRepository transactionRepository;
    private final SaleRepository saleRepository;
    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;

    private final PayrollMapper payrollMapper;

    @Override
    public PayrollRecord processPayroll(UUID employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        EmployeeProjection employeeProjection = employeeRepository
                .getEmployeeProjectionById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        Double employeeSalesVolume = saleRepository.sumSaleAmountByEmployeeId(employeeId, startDate, endDate);

        Double calculatedCommission = employeeSalesVolume * employeeProjection.getCommission();
        Double officialSalary = employeeProjection.getSalary();

        Transaction transaction = Transaction.builder()
                .type(TransactionType.PAID)
                .amount(officialSalary + calculatedCommission)
                .build();

        Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);

        Payroll payroll = Payroll.builder()
                .salary(officialSalary)
                .commission(calculatedCommission)
                .transaction(savedTransaction)
                .employee(employeeRepository.getReferenceById(employeeId))
                .build();

        return payrollMapper.entityToRecord(payrollRepository.saveAndFlush(payroll));
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollRecord getPayroll(UUID payrollId) {
        return payrollRepository
                .getPayrollProjectionById(payrollId)
                .map(payrollMapper::projectionToRecord)
                .orElseThrow(() -> new EntityNotFoundException("Payroll not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollRecord> getAllPayrolls(Pageable pageable) {
        return payrollRepository.findAllBy(pageable).map(payrollMapper::projectionToRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayrollRecord> getPayrollHistoryForEmployee(UUID employeeId, Pageable pageable) {
        return payrollRepository.findByEmployeeId(employeeId, pageable).map(payrollMapper::projectionToRecord);
    }
}
