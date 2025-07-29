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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController("historyLoginHistoryController")
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

    // 수정: 등록 엔드포인트, mapViewProcessor로 통합
    @CommonApiResponses
    @PostMapping("/insert")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> insertLoginHistory(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        String rptCd = "LOGINHISTUSERINFO_INSERT";
        String jobGb = "SET";

        // JWT에서 empNo 추출
        Claims claims = (Claims) httpRequest.getAttribute("user");
        String empNo = claims != null && claims.getSubject() != null ? claims.getSubject() : null;
        if (empNo == null) {
            logger.warn("No authentication claims found for insert.");
            return responseEntityUtil.okBodyEntity(null, "01", "인증이 필요합니다.");
        }

        // 요청 파라미터 추출
        List<String> params = mapViewParamsUtil.getParams(request, escapeUtil);
        if (params.size() < 4) { // empNo, userIp, userCongb, dbCreatedDt 최소 4개 필요
            return responseEntityUtil.okBodyEntity(null, "01", "필수 파라미터가 부족합니다 (empNo, userIp, userCongb, dbCreatedDt).");
        }
        String userIp = params.get(0);      // userIp
        String userCongb = params.get(1);   // userCongb
        String dbCreatedDt = params.get(2); // dbCreatedDt
        String debug = params.size() > 3 ? params.get(3) : "F"; // debug (기본값: "F")

        List<String> finalParams = new ArrayList<>();
        finalParams.add(empNo);             // empNo를 params 앞에 추가
        finalParams.add(userIp);
        finalParams.add(userCongb);
        finalParams.add(dbCreatedDt);
        finalParams.add(debug);

        logger.debug("Insert params: {}", finalParams);

        List<Map<String, Object>> unescapedResultList;
        try {
            // mapViewProcessor로 프로시저 호출
            unescapedResultList = mapViewProcessor.processDynamicView(rptCd, finalParams, empNo, jobGb);
            if (unescapedResultList.isEmpty()) {
                return responseEntityUtil.okBodyEntity(null, "01", "등록 결과가 없습니다.");
            }
        } catch (Exception e) {
            errorMessage = "/insert unescapedResultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);";
            logger.error(this.getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "99", "서버 내부 오류가 발생했습니다: " + e.getMessage());
        }

        return responseEntityUtil.okBodyEntity(unescapedResultList, "00", "등록 성공");
    }

    // 수정: 삭제 엔드포인트, mapViewProcessor로 통합
    @CommonApiResponses
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> deleteLoginHistory(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        String rptCd = "LOGINHISTUSERINFO_DELETE";
        String jobGb = "DELETE";

        // JWT에서 empNo 추출
        Claims claims = (Claims) httpRequest.getAttribute("user");
        String empNo = claims != null && claims.getSubject() != null ? claims.getSubject() : null;
        if (empNo == null) {
            logger.warn("No authentication claims found for delete.");
            return responseEntityUtil.okBodyEntity(null, "01", "인증이 필요합니다.");
        }

        // 요청 파라미터 추출
        List<String> params = mapViewParamsUtil.getParams(request, escapeUtil);
        if (params.size() < 2) { // empNo, dbCreatedDt 최소 2개 필요
            return responseEntityUtil.okBodyEntity(null, "01", "필수 파라미터가 부족합니다 (empNo, dbCreatedDt).");
        }
        String dbCreatedDt = params.get(1); // dbCreatedDt
        String debug = params.size() > 2 ? params.get(2) : "F"; // debug (기본값: "F")

        List<String> finalParams = new ArrayList<>();
        finalParams.add(empNo);             // empNo를 params 앞에 추가
        finalParams.add(dbCreatedDt);
        finalParams.add(debug);

        logger.debug("Delete params: {}", finalParams);

        List<Map<String, Object>> unescapedResultList;
        try {
            // mapViewProcessor로 프로시저 호출
            unescapedResultList = mapViewProcessor.processDynamicView(rptCd, finalParams, empNo, jobGb);
            if (unescapedResultList.isEmpty()) {
                return responseEntityUtil.okBodyEntity(null, "01", "삭제 결과가 없습니다.");
            }
        } catch (Exception e) {
            errorMessage = "/delete unescapedResultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);";
            logger.error(this.getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "99", "서버 내부 오류가 발생했습니다: " + e.getMessage());
        }

        return responseEntityUtil.okBodyEntity(unescapedResultList, "00", "삭제 성공");
    }

}