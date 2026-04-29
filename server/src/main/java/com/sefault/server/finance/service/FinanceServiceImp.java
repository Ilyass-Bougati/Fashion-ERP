package com.sefault.server.finance.service;

import com.sefault.server.finance.dto.record.FixChargeRecord;
import com.sefault.server.finance.dto.record.PayrollRecord;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.entity.FixCharge;
import com.sefault.server.finance.entity.Payroll;
import com.sefault.server.finance.entity.Transaction;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.mapper.FixChargeMapper;
import com.sefault.server.finance.mapper.PayrollMapper;
import com.sefault.server.finance.mapper.TransactionMapper;
import com.sefault.server.finance.repository.FixChargeRepository;
import com.sefault.server.finance.repository.PayrollRepository;
import com.sefault.server.finance.repository.TransactionRepository;
import com.sefault.server.hr.dto.projection.EmployeeProjection;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.sales.repository.SaleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class FinanceServiceImp implements FinanceService{
    private final TransactionRepository transactionRepository;
    private final SaleRepository saleRepository;
    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final FixChargeRepository fixChargeRepository;

    private final TransactionMapper transactionMapper;
    private final PayrollMapper payrollMapper;
    private final FixChargeMapper fixChargeMapper;

    public TransactionRecord createTransaction(TransactionRecord transactionRecord){
        Transaction transaction = Transaction.builder()
                .type(transactionRecord.type())
                .sale(transactionRecord.saleId() != null
                        ? saleRepository.getReferenceById(transactionRecord.saleId())
                        : null)
                .amount(transactionRecord.amount())
                .build();
        return transactionMapper.entityToRecord(transactionRepository.save(transaction));
    }

    public PayrollRecord processPayroll(UUID employeeId, LocalDateTime startDate, LocalDateTime endDate){
        EmployeeProjection employeeProjection = employeeRepository.getEmployeeProjectionById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        Double employeeSalesVolume = saleRepository.sumSaleAmountByEmployeeId(employeeId, startDate, endDate);

        if (employeeSalesVolume == null) {
            employeeSalesVolume = 0.0;
        }

        Double calculatedCommission = employeeSalesVolume * employeeProjection.getCommission();
        Double officialSalary = employeeProjection.getSalary();

        Transaction transaction = Transaction.builder()
                .type(TransactionType.PAID)
                .amount(officialSalary + calculatedCommission)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        Payroll payroll = Payroll.builder()
                .salary(officialSalary)
                .commission(calculatedCommission)
                .transaction(savedTransaction)
                .employee(employeeRepository.getReferenceById(employeeId))
                .build();

        return payrollMapper.entityToRecord(payrollRepository.save(payroll));
    }

    @Transactional(readOnly = true)
    public TransactionRecord getTransaction(UUID transactionId){
        return transactionRepository.getTransactionProjectionById(transactionId)
                .map(transactionMapper::projectionToRecord)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
    }

    @Transactional(readOnly = true)
    public PayrollRecord getPayroll(UUID payrollId){
        return payrollRepository.getPayrollProjectionById(payrollId)
                .map(payrollMapper::projectionToRecord)
                .orElseThrow(() -> new EntityNotFoundException("Payroll not found"));
    }

    public FixChargeRecord createFixCharge(FixChargeRecord fixChargeRecord){
        FixCharge fixCharge = fixChargeMapper.toEntity(fixChargeRecord);
        fixCharge.setActive(fixChargeRecord.active() != null ?  fixChargeRecord.active() : true);
        return  fixChargeMapper.entityToRecord(fixChargeRepository.save(fixCharge));
    }
}
