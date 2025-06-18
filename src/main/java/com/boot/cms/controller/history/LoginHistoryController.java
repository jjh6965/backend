package com.boot.cms.controller.history;

import com.boot.cms.dto.common.ApiResponseDto;
import com.boot.cms.service.mapview.MapViewProcessor;
import com.boot.cms.util.CommonApiResponses;
import com.boot.cms.util.EscapeUtil;
import com.boot.cms.util.MapViewParamsUtil;
import com.boot.cms.util.ResponseEntityUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/history/login")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "9.이력관리 > 로그인이력", description = "로그인이력을 관리하는 API")
public class LoginHistoryController {
    private static final Logger logger = LoggerFactory.getLogger(LoginHistoryController.class);

    private final MapViewProcessor mapViewProcessor;
    private final ResponseEntityUtil responseEntityUtil;
    private final EscapeUtil escapeUtil;
    private final MapViewParamsUtil mapViewParamsUtil;

    @Setter
    @Getter
    String errorMessage;

    @CommonApiResponses
    @PostMapping("/list")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> loginHistoryList(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest
    ) {
        String rptCd = "LOGINHISTUSERINFO";
        String jobGb = "GET";

        Claims claims = (Claims) httpRequest.getAttribute("user");
        logger.debug("Received HTTP Request: {}", httpRequest);
        logger.debug("Extracted Claims from request: {}", claims);
        String empNo = claims != null && claims.getSubject() != null ? claims.getSubject() : null;

        if (empNo == null) {
            logger.warn("No authentication claims found. Authorization Header: {}", httpRequest.getHeader("Authorization"));
            return responseEntityUtil.okBodyEntity(null, "01", "인증이 필요합니다.");
        }

        logger.debug("Processing request with empNo: {}, raw pMDATE: {}", empNo, request.get("pMDATE"));
        String pMDATE = (String) request.get("pMDATE");
        pMDATE = pMDATE != null ? pMDATE.replace("-", "") : "202506"; // YYYYMM 형식으로 변환
        String pDEBUG = (String) request.get("pDEBUG") != null ? (String) request.get("pDEBUG") : "N";

        // 명시적으로 파라미터 설정
        List<String> params = new ArrayList<>();
        params.add(pMDATE);
        params.add(pDEBUG);
        logger.debug("Final params list: {}", params);

        List<Map<String, Object>> unescapedResultList;
        try {
            unescapedResultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);
            logger.debug("Fetched data from MapViewProcessor: {}", unescapedResultList);
            if (unescapedResultList.isEmpty()) {
                logger.debug("No data found for the given parameters in tb_map_view.");
                return responseEntityUtil.okBodyEntity(null, "01", "조회 결과가 없습니다.");
            }
        } catch (Exception e) {
            errorMessage = "/list unescapedResultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);";
            logger.error(this.getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "99", "서버 내부 오류가 발생했습니다: " + e.getMessage());
        }

        return responseEntityUtil.okBodyEntity(unescapedResultList);
    }

}