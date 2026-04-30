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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FinanceServiceImp implements FinanceService {
    private final TransactionRepository transactionRepository;
    private final SaleRepository saleRepository;
    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final FixChargeRepository fixChargeRepository;

    private final TransactionMapper transactionMapper;
    private final PayrollMapper payrollMapper;
    private final FixChargeMapper fixChargeMapper;

    @Override
    public TransactionRecord createTransaction(TransactionRecord transactionRecord) {
        Transaction transaction = Transaction.builder()
                .type(transactionRecord.type())
                .sale(
                        transactionRecord.saleId() != null
                                ? saleRepository.getReferenceById(transactionRecord.saleId())
                                : null)
                .amount(transactionRecord.amount())
                .build();
        return transactionMapper.entityToRecord(transactionRepository.saveAndFlush(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionRecord getTransaction(UUID transactionId) {
        return transactionRepository
                .getTransactionProjectionById(transactionId)
                .map(transactionMapper::projectionToRecord)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
    }

    @Override
    public TransactionRecord reverseTransaction(UUID originalTransactionId) {
        Transaction originalTransaction = transactionRepository
                .findById(originalTransactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        TransactionType reverseType =
                originalTransaction.getType() == TransactionType.PAID ? TransactionType.RECEIVED : TransactionType.PAID;

        Transaction reversal = Transaction.builder()
                .type(reverseType)
                .amount(originalTransaction.getAmount())
                .sale(originalTransaction.getSale())
                .build();
        return transactionMapper.entityToRecord(transactionRepository.saveAndFlush(reversal));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionRecord> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAllBy(pageable).map(transactionMapper::projectionToRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionRecord> getTransactionsByType(TransactionType type, Pageable pageable) {
        return transactionRepository.findByType(type, pageable).map(transactionMapper::projectionToRecord);
    }

    @Override
    public PayrollRecord processPayroll(UUID employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        EmployeeProjection employeeProjection = employeeRepository
                .getEmployeeProjectionById(employeeId)
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

    @Override
    public FixChargeRecord createFixCharge(FixChargeRecord fixChargeRecord) {
        FixCharge fixCharge = fixChargeMapper.toEntity(fixChargeRecord);
        fixCharge.setActive(fixChargeRecord.active() != null ? fixChargeRecord.active() : true);
        return fixChargeMapper.entityToRecord(fixChargeRepository.saveAndFlush(fixCharge));
    }

    @Override
    @Transactional(readOnly = true)
    public FixChargeRecord getFixCharge(UUID chargeId) {
        return fixChargeRepository
                .getFixChargeProjectionById(chargeId)
                .map(fixChargeMapper::projectionToRecord)
                .orElseThrow(() -> new EntityNotFoundException("FixCharge not found"));
    }

    @Override
    public FixChargeRecord updateFixCharge(FixChargeRecord fixChargeRecord) {
        FixCharge existingCharge = fixChargeRepository
                .findById(fixChargeRecord.id())
                .orElseThrow(() -> new EntityNotFoundException("FixCharge not found"));

        fixChargeMapper.updateEntityFromRecord(fixChargeRecord, existingCharge);

        return fixChargeMapper.entityToRecord(fixChargeRepository.save(existingCharge));
    }

    @Override
    public void toggleFixChargeStatus(UUID chargeId) {
        FixCharge existingCharge = fixChargeRepository
                .findById(chargeId)
                .orElseThrow(() -> new EntityNotFoundException("FixCharge not found"));

        existingCharge.setActive(!existingCharge.getActive());
        fixChargeRepository.save(existingCharge);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FixChargeRecord> getAllFixCharges(Pageable pageable) {
        return fixChargeRepository.findAllBy(pageable).map(fixChargeMapper::projectionToRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FixChargeRecord> getActiveFixCharges(Pageable pageable) {
        return fixChargeRepository.findByActiveTrue(pageable).map(fixChargeMapper::projectionToRecord);
    }
}
