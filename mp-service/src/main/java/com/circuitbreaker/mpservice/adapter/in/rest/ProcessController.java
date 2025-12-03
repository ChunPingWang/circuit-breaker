package com.circuitbreaker.mpservice.adapter.in.rest;

import com.circuitbreaker.mpservice.adapter.in.rest.dto.ErrorResponse;
import com.circuitbreaker.mpservice.adapter.in.rest.dto.ProcessResponse;
import com.circuitbreaker.mpservice.adapter.in.rest.dto.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Process", description = "時間處理流程 API")
@RestController
@RequestMapping("/api")
public class ProcessController {

    @Operation(
        summary = "處理時間流程",
        description = "呼叫 gbp-service 取得當前時間，再傳遞至 gin-service。若 gin-service 無法連線，資料會暫存。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功完成時間處理",
            content = @Content(schema = @Schema(implementation = ProcessResponse.class))
        ),
        @ApiResponse(
            responseCode = "202",
            description = "服務降級 - 資料已暫存",
            content = @Content(schema = @Schema(implementation = ProcessResponse.class))
        ),
        @ApiResponse(
            responseCode = "503",
            description = "服務不可用",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/process")
    public ResponseEntity<ProcessResponse> process() {
        // TODO: 實作處理邏輯
        ProcessResponse response = new ProcessResponse(
            true,
            false,
            "2025-12-03T10:30:00Z",
            "Time processed successfully",
            null
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "查詢系統狀態",
        description = "取得斷路器狀態、待傳送訊息數量及各服務連線狀態"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功取得系統狀態",
            content = @Content(schema = @Schema(implementation = StatusResponse.class))
        )
    })
    @GetMapping("/status")
    public ResponseEntity<StatusResponse> status() {
        // TODO: 實作狀態查詢邏輯
        StatusResponse response = new StatusResponse(
            "CLOSED",
            0,
            true,
            true
        );
        return ResponseEntity.ok(response);
    }
}
