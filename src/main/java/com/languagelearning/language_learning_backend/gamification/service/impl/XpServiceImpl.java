package com.languagelearning.language_learning_backend.gamification.service.impl;

import com.languagelearning.language_learning_backend.exception.ResourceNotFoundException;
import com.languagelearning.language_learning_backend.gamification.entity.XpLog;
import com.languagelearning.language_learning_backend.gamification.enums.XpReason;
import com.languagelearning.language_learning_backend.gamification.repository.XpLogRepository;
import com.languagelearning.language_learning_backend.gamification.service.XpService;
import com.languagelearning.language_learning_backend.user.entity.User;
import com.languagelearning.language_learning_backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class XpServiceImpl implements XpService {

    private final UserRepository userRepository;
    private final XpLogRepository xpLogRepository;

    @Override
    @Transactional
    public void awardXp(Long userId, XpReason reason, int amount, Long sourceId) {
        User user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
        user.setXp(user.getXp() + amount);
        userRepository.save(user);

        XpLog log = new XpLog();
        log.setUser(user);
        log.setReason(reason);
        log.setAmount(amount);
        log.setSourceId(sourceId);
        log.setEarnedAt(LocalDateTime.now());
        xpLogRepository.save(log);
    }
}
