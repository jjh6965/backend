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
 * 예약 관리 컨트롤러
 * 설명: 관리자 페이지에서 예약 정보를 조회하고 등록/수정/삭제
 * 한글 주석: tb_map_view의 RPTCD를 기반으로 동적 프로시저 호출, 중복 방지 및 연장/승인 관리
 */
@RestController
@RequestMapping("api/reservation/reservation")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "4. 운영관리 > 예약 관리", description = "예약 정보를 조회하고 등록/수정/삭제하는 API")
public class ReservationController {
    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final MapViewProcessor mapViewProcessor;
    private final ResponseEntityUtil responseEntityUtil;
    private final EscapeUtil escapeUtil;
    private final MapViewParamsUtil mapViewParamsUtil;

    @Setter
    @Getter
    private String errorMessage;

    /**
     * 예약 정보 조회 API
     * 설명: ReservationManage.jsx에서 예약 정보를 동적으로 조회
     * 프로시저: UP_RESERVATION_SELECT
     * 입력: 예약자 이름(p_NAME), 상태(p_STATUS), 층 식별자(p_FLOOR_ID), 섹션(p_SECTION), 연장 상태(p_EXTENSION_STATUS), 승인 상태(p_APPROVAL_STATUS), 디버그 모드(p_DEBUG)
     * 출력: 예약 정보 리스트
     */
    @CommonApiResponses
    @PostMapping("/list")
    @Operation(summary = "예약 정보 조회", description = "예약자 이름, 상태, 층, 섹션, 연장/승인 상태로 예약 정보를 조회합니다. 예: 김예약의 사용중 예약")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> reservationList(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest
    ) {
        String rptCd = "RESERVATIONSELECT";
        String jobGb = "GET";

        Claims claims = (Claims) httpRequest.getAttribute("user");
        String empNo = claims != null && claims.getSubject() != null ? claims.getSubject() : null;

        List<String> params = mapViewParamsUtil.getParams(request, escapeUtil);
        logger.debug("예약 정보 조회 요청: rptCd={}, params={}, empNo={}", rptCd, params, empNo);

        List<Map<String, Object>> resultList;
        try {
            resultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);
            if (resultList.isEmpty()) {
                logger.warn("예약 정보 조회 결과 없음: rptCd={}, params={}", rptCd, params);
                return responseEntityUtil.okBodyEntity(null, "01", "조회된 예약 정보가 없습니다.");
            }
            logger.debug("예약 정보 조회 성공: 결과={}", resultList);
            return responseEntityUtil.okBodyEntity(resultList, "00", "조회 성공");
        } catch (IllegalArgumentException e) {
            setErrorMessage("예약 정보 조회 중 오류 발생: " + e.getMessage());
            logger.error(getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "01", "조회 중 오류: " + e.getMessage());
        } catch (Exception e) {
            setErrorMessage("예약 정보 조회 중 시스템 오류 발생: " + e.getMessage());
            logger.error(getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "99", "시스템 오류: " + e.getMessage());
        }
    }

    /**
     * 예약 정보 등록/수정/삭제 API
     * 설명: ReservationManage.jsx에서 예약 정보를 설정
     * 프로시저: UP_RESERVATION_TRANSACTION
     * 입력: 작업 구분(p_GUBUN), 예약 ID(p_RESERVATION_ID), 호실 ID(p_ROOM_ID), 호실 유형(p_ROOM_TYPE),
     * 예약자 이름(p_NAME), 성별(p_GENDER), 전화번호(p_PHONE), 예약 시작일(p_START_DATE), 기간(p_DURATION),
     * 연장 상태(p_EXTENSION_STATUS), 승인 상태(p_APPROVAL_STATUS), 금액(p_PRICE), 직원 번호(p_EMP_ID), 특이사항(p_NOTE), 디버그 모드(p_DEBUG)
     * 출력: 처리 결과
     */
    @CommonApiResponses
    @PostMapping("/save")
    @Operation(summary = "예약 정보 등록/수정/삭제", description = "예약 정보를 등록, 수정 또는 삭제합니다. 예: 김예약의 1FA1 예약 등록")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    public ResponseEntity<ApiResponseDto<List<Map<String, Object>>>> reservationSave(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest
    ) {
        String rptCd = "RESERVATIONTRANSACTION";
        String jobGb = "SET";

        Claims claims = (Claims) httpRequest.getAttribute("user");
        String empNo = claims != null && claims.getSubject() != null ? claims.getSubject() : null;

        List<String> params = mapViewParamsUtil.getParams(request, escapeUtil);
        logger.debug("예약 정보 처리 요청: rptCd={}, params={}, empNo={}", rptCd, params, empNo);

        List<Map<String, Object>> resultList;
        try {
            resultList = mapViewProcessor.processDynamicView(rptCd, params, empNo, jobGb);
            if (resultList.isEmpty()) {
                logger.warn("예약 정보 처리 결과 없음: rptCd={}, params={}", rptCd, params);
                return responseEntityUtil.okBodyEntity(null, "01", "처리 결과가 없습니다.");
            }
            logger.debug("예약 정보 처리 성공: 결과={}", resultList);
            return responseEntityUtil.okBodyEntity(resultList, "00", resultList.get(0).get("ERRMSG").toString());
        } catch (IllegalArgumentException e) {
            setErrorMessage("예약 정보 처리 중 오류 발생: " + e.getMessage());
            logger.error(getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "01", "처리 중 오류: " + e.getMessage());
        } catch (Exception e) {
            setErrorMessage("예약 정보 처리 중 시스템 오류 발생: " + e.getMessage());
            logger.error(getErrorMessage(), e);
            return responseEntityUtil.okBodyEntity(null, "99", "시스템 오류: " + e.getMessage());
        }
    }
}