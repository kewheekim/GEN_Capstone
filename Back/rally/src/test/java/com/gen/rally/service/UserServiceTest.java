package com.gen.rally.service;

import com.gen.rally.dto.TierAssessRequest;
import com.gen.rally.dto.TierAssessResponse;
import com.gen.rally.enums.Tier;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Test
    void getFirstTier_점수계산_정상() {
        TierAssessRequest req = new TierAssessRequest();
        req.setQ1(5); req.setQ2(4); req.setQ3(5); // selfQ=14
        req.setQ4(5);                             // expQ=5
        req.setQ5(7);                             // careerQ=7

        TierAssessResponse res = userService.getFirstTier(req);

        assertThat(res.getScore()).isGreaterThan(80.0);
        assertThat(res.getTier()).isEqualTo(Tier.valueOf("상급자1"));
    }
}