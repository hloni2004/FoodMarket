package com.llburgers.service;

import com.llburgers.domain.AuditLog;

import java.util.List;

public interface IAuditLogService {

    AuditLog log(String action, String entity, String detail, String performedBy);

    List<AuditLog> getAll();
}
