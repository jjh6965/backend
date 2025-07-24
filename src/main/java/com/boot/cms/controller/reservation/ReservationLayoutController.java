package com.boot.cms.controller.reservation;

import com.boot.cms.dto.common.ApiResponseDto;
import com.boot.cms.service.mapview.MapViewProcessor;
import com.boot.cms.util.CommonApiResponses;
import com.boot.cms.util.EscapeUtil;
import com.boot.cms.util.MapViewParamsUtil;
import com.boot.cms.util.ResponseEntityUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;
import java.util.Map;

/**
 * 레이아웃 정보 컨트롤러
 * 설명: 관리자 페이지에서 층과 섹션별 호실 정보를 조회하고 등록/수정/삭제
 * 한글 주석: tb_map_view의 RPTCD를 기반으로 동적 프로시저 호출, 하드코딩 최소화
 */
@RestController
@RequestMapping("api/reservation/layout")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "4. 운영관리 > 레이아웃 관리", description = "층과 섹션별 호실 정보를 조회하고 등록/수정/삭제하는 API")
public class ReservationLayoutController {
    private static final Logger logger = LoggerFactory.getLogger(ReservationLayoutController.class);

    private final MapViewProcessor mapViewProcessor;
    private final ResponseEntityUtil responseEntityUtil;
    private final EscapeUtil escapeUtil;
    private final MapViewParamsUtil mapViewParamsUtil;

    @Setter
    @Getter
    private String errorMessage;

    /**
     * 호실 레이아웃 정보 조회 API
     * 설명: ReservationAdminPage.jsx에서 드롭다운 및 레이아웃 조회
     * 프로시저: UP_RESERVATIONLAYOUT_SELECT
     * 입력: 층 식별자(p_FLOOR_ID), 섹션(p_SECTION), 디버그 모드(p_DEBUG)
     * 출력: 호실 정보 리스트
     */
    @CommonApiResponses
    @PostMapping("/list")
    @Operation(summary = "호실 레이아웃 조회", description = "층과 섹션별 호실 정보를 조회합니다. 예: 1F의 A섹션 1인실 정보")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> reservationLayoutList(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest
    ) {
        String rptCd = "RESERVATIONLAYOUTSELECT"; // 하드코딩된 RPTCD
        String jobGb = "GET";

        Claims claims = (Claims) httpRequest.getAttribute("user");
        String empNo = claims != null && claims.getSubject() != null ? claims.getSubject() : null;

        List<String> params = mapViewParamsUtil.getParams(request, escapeUtil);
        logger.debug("호실 레이아웃 조회 요청: rptCd={}, params={}, empNo={}", rptCd, params, empNo);

        List<Map<String, Object>> resultList;
        try {
            resultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);
            if (resultList.isEmpty()) {
                logger.warn("호실 레이아웃 조회 결과 없음: rptCd={}, params={}", rptCd, params);
                return responseEntityUtil.okBodyEntity(null, "01", "조회된 호실 정보가 없습니다.");
            }
            logger.debug("호실 레이아웃 조회 성공: 결과={}", resultList);
            return responseEntityUtil.okBodyEntity(resultList, "00", "조회 성공");
        } catch (IllegalArgumentException e) {
            setErrorMessage("호실 레이아웃 조회 중 오류 발생: " + e.getMessage());
            logger.error(getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "01", "조회 중 오류: " + e.getMessage());
        } catch (Exception e) {
            setErrorMessage("호실 레이아웃 조회 중 시스템 오류 발생: " + e.getMessage());
            logger.error(getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "99", "시스템 오류: " + e.getMessage());
        }
    }

    /**
     * 호실 레이아웃 정보 등록/수정/삭제 API
     * 설명: ReservationAdminPage.jsx에서 호실 레이아웃 설정
     * 프로시저: UP_RESERVATIONLAYOUT_TRANSACTION
     * 입력: 작업 구분(p_GUBUN), 호실 ID(p_ROOM_ID), 층(p_FLOOR_ID), 섹션(p_SECTION), 호실 유형(p_ROOM_TYPE), 가격(p_PRICE), 직원 번호(p_EMP_NO), 디버그 모드(p_DEBUG)
     * 출력: 처리 결과
     */
    @CommonApiResponses
    @PostMapping("/save")
    @Operation(summary = "호실 레이아웃 등록/수정/삭제", description = "층과 섹션별 호실 정보를 등록, 수정 또는 삭제합니다. 예: 1FA1 호실을 1인실로 등록")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> reservationLayoutSave(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest
    ) {
        String rptCd = "RESERVATIONLAYOUTTRANSACTION"; // 하드코딩된 RPTCD
        String jobGb = "SET";

        Claims claims = (Claims) httpRequest.getAttribute("user");
        String empNo = claims != null && claims.getSubject() != null ? claims.getSubject() : null;

        List<String> params = mapViewParamsUtil.getParams(request, escapeUtil);
        logger.debug("호실 레이아웃 처리 요청: rptCd={}, params={}, empNo={}", rptCd, params, empNo);

        List<Map<String, Object>> resultList;
        try {
            resultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);
            if (resultList.isEmpty()) {
                logger.warn("호실 레이아웃 처리 결과 없음: rptCd={}, params={}", rptCd, params);
                return responseEntityUtil.okBodyEntity(null, "01", "처리 결과가 없습니다.");
            }
            logger.debug("호실 레이아웃 처리 성공: 결과={}", resultList);
            return responseEntityUtil.okBodyEntity(resultList, "00", resultList.get(0).get("ERRMSG").toString());
        } catch (IllegalArgumentException e) {
            setErrorMessage("호실 레이아웃 처리 중 오류 발생: " + e.getMessage());
            logger.error(getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "01", "처리 중 오류: " + e.getMessage());
        } catch (Exception e) {
            setErrorMessage("호실 레이아웃 처리 중 시스템 오류 발생: " + e.getMessage());
            logger.error(getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "99", "시스템 오류: " + e.getMessage());
        }
    }
}