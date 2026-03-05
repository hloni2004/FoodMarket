package com.llburgers.service;

import com.llburgers.domain.Admin;
import com.llburgers.domain.BusinessStatus;

import java.time.LocalDateTime;

public interface IBusinessStatusService {

    // ─── Read ─────────────────────────────────────────────────────────────────

    BusinessStatus getCurrentStatus();

    boolean isOpen();

    // ─── Business Logic ───────────────────────────────────────────────────────

    BusinessStatus openBusiness(Admin admin);

    BusinessStatus closeBusiness(Admin admin, String closedMessage, LocalDateTime expectedReopenAt);
}

