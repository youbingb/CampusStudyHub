package com.csh.modules.reservation.controller;

import com.csh.common.R;
import com.csh.modules.reservation.dto.RecommendQuery;
import com.csh.modules.reservation.dto.RecommendVo;
import com.csh.modules.reservation.service.RecommendationService;
import com.csh.security.LoginUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "智能推荐（学生端）")
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendationService recommendationService;

    @Operation(summary = "根据期望时段返回 Top-N 座位推荐")
    @GetMapping
    public R<List<RecommendVo>> recommend(@Valid @ModelAttribute RecommendQuery query) {
        Long uid = LoginUserHolder.requireCurrent().getId();
        return R.ok(recommendationService.recommend(uid, query));
    }
}
