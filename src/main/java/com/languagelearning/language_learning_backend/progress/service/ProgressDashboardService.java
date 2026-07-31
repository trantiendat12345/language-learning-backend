package com.languagelearning.language_learning_backend.progress.service;

import com.languagelearning.language_learning_backend.progress.dto.response.ProgressDashboardResponse;

public interface ProgressDashboardService {

    ProgressDashboardResponse getDashboard(Long userId);
}
