package com.sefault.server.user.service.impl;

import com.sefault.server.user.dto.record.UserReportRecord;
import com.sefault.server.user.entity.Report;
import com.sefault.server.user.entity.User;
import com.sefault.server.user.entity.UserReport;
import com.sefault.server.user.entity.id.UserReportId;
import com.sefault.server.user.mapper.UserReportMapper;
import com.sefault.server.user.repository.ReportRepository;
import com.sefault.server.user.repository.UserReportRepository;
import com.sefault.server.user.repository.UserRepository;
import com.sefault.server.user.service.UserReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserReportServiceImpl implements UserReportService {
    private final UserReportRepository userReportRepository;
    private final UserReportMapper userReportMapper;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @Override
    public Page<UserReportRecord> getAll(Pageable pageable) {
        return userReportRepository.findAllBy(pageable).map(userReportMapper::projectionToRecord);
    }

    @Override
    public void logAccess(String email, UUID reportId) {
        User user = userRepository.getReferenceByEmail(email);
        Report report = reportRepository.getReferenceById(reportId);

        UserReport entity = new UserReport();
        entity.setReport(report);
        entity.setUser(user);

        userReportRepository.save(entity);
    }
}
