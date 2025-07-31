/*
 * Controller: LoginHistoryController
 * Purpose: Handles REST API requests for reservation history management
 * Endpoints:
 *   - POST /api/history/login/list: Calls UP_LOGINHISTORY_USERINFO_SELECT
 *   - POST /api/history/login/save: Calls UP_LOGINHISTORY_USERINFO_TRANSACTION (I/U/D)
 * Notes: Aligns with procedure param order, logs actions in tb_map_view_hist, uses dynamic params for /save
 */
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/history/login")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "9. 이력관리 > 로그인이력", description = "로그인이력을 관리하는 API")
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
            HttpServletRequest httpRequest) {
        String rptCd = "LOGINHISTORYUSERINFOSELECT";
        String jobGb = "GET";

        Claims claims = (Claims) httpRequest.getAttribute("user");
        String empNo = claims != null && claims.getSubject() != null ? claims.getSubject() : null;

        if (empNo == null) {
            logger.warn("No authentication claims found. Authorization Header: {}", httpRequest.getHeader("Authorization"));
            return responseEntityUtil.okBodyEntity(null, "01", "인증이 필요합니다.");
        }

        String pMDATE = (String) request.get("pMDATE");
        LocalDate today = LocalDate.now();
        if (pMDATE != null && !pMDATE.matches("\\d{6}")) {
            return responseEntityUtil.okBodyEntity(null, "01", "pMDATE 형식이 올바르지 않습니다 (YYYYMM 필요).");
        }
        pMDATE = pMDATE != null ? pMDATE.replace("-", "") : String.valueOf(today.getYear()) + String.format("%02d", today.getMonthValue());
        String pDEBUG = (String) request.get("pDEBUG") != null ? (String) request.get("pDEBUG") : "F";

        List<String> params = new ArrayList<>();
        params.add(pMDATE);              // 1. pMDATE
        params.add(request.get("pEMPNO") != null ? (String) request.get("pEMPNO") : ""); // 2. pEMPNO
        params.add(pDEBUG);              // 3. pDEBUG

        List<Map<String, Object>> unescapedResultList;
        try {
            unescapedResultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);
            if (unescapedResultList.isEmpty()) {
                return responseEntityUtil.okBodyEntity(null, "01", "조회 결과가 없습니다.");
            }
            unescapedResultList.forEach(row -> {
                if (row.get("DATE") != null) {
                    row.put("date", row.get("DATE").toString());
                }
            });
        } catch (Exception e) {
            errorMessage = "/list unescapedResultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);";
            logger.error(this.getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "99", "서버 내부 오류가 발생했습니다: " + e.getMessage());
        }

        return responseEntityUtil.okBodyEntity(unescapedResultList);
    }

    @CommonApiResponses
    @PostMapping("/save")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> insertLoginHistory(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        String rptCd = "LOGINHISTORYUSERINFOTRANSACTION";
        String jobGb = "SET";

        Claims claims = (Claims) httpRequest.getAttribute("user");
        String empNo = claims != null && claims.getSubject() != null ? claims.getSubject() : null;

        if (empNo == null) {
            logger.warn("No authentication claims found. Authorization Header: {}", httpRequest.getHeader("Authorization"));
            return responseEntityUtil.okBodyEntity(null, "01", "인증이 필요합니다.");
        }

        // Dynamically get params with basic validation
        List<String> params = mapViewParamsUtil.getParams(request, escapeUtil);
        if (params.size() != 6) { // Expect 6 params: pGUBUN, pEMPNO, pUSERIP, pSTATUS, pDBCREATEDT, pDEBUG
            return responseEntityUtil.okBodyEntity(null, "01", "필수 파라미터 6개가 필요합니다.");
        }
        String pGUBUN = params.get(0);
        if (!List.of("I", "U", "D").contains(pGUBUN)) {
            return responseEntityUtil.okBodyEntity(null, "01", "pGUBUN은 I, U, 또는 D이어야 합니다.");
        }

        // Ensure pDEBUG is set
        params.set(5, params.get(5) != null ? params.get(5) : "F");

        List<Map<String, Object>> unescapedResultList;
        try {
            unescapedResultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);
        } catch (IllegalArgumentException e) {
            errorMessage = "/save unescapedResultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);";
            logger.error(this.getErrorMessage(), e.getMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "01", e.getMessage());
        }

        if (unescapedResultList.isEmpty()) {
            return responseEntityUtil.okBodyEntity(null, "01", "결과없음");
        }

        return responseEntityUtil.okBodyEntity(unescapedResultList);
    }
}