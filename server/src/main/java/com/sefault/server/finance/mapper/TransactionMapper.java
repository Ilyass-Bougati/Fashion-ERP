package com.sefault.server.finance.mapper;

import com.sefault.server.finance.dto.projection.TransactionProjection;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionRecord entityToRecord(Transaction Transaction);

    TransactionRecord projectionToRecord(TransactionProjection projection);

    Transaction toEntity(TransactionRecord record);

    void updateEntityFromRecord(TransactionRecord record, @MappingTarget Transaction entityToUpdate);
}
