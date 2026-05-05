package com.smartRestaurant.accounting.service;

import com.smartRestaurant.accounting.dto.AccountingSummaryDTO;

import java.time.LocalDateTime;

public interface AccountingService {
    AccountingSummaryDTO getSummary(LocalDateTime from, LocalDateTime to);
}
