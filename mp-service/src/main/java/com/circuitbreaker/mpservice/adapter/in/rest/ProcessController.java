package com.circuitbreaker.mpservice.adapter.in.rest;

import com.circuitbreaker.mpservice.adapter.in.rest.dto.ErrorResponse;
import com.circuitbreaker.mpservice.adapter.in.rest.dto.ProcessResponse;
import com.circuitbreaker.mpservice.adapter.in.rest.dto.StatusResponse;
import com.circuitbreaker.mpservice.application.usecase.GetStatusUseCase;
import com.circuitbreaker.mpservice.application.usecase.ProcessTimeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Tag(name = "Process", description = "時間處理流程 API")
@RestController
@RequestMapping("/api")
public class ProcessController {

    private final ProcessTimeUseCase processTimeUseCase;
    private final GetStatusUseCase getStatusUseCase;

    public ProcessController(ProcessTimeUseCase processTimeUseCase, GetStatusUseCase getStatusUseCase) {
        this.processTimeUseCase = processTimeUseCase;
        this.getStatusUseCase = getStatusUseCase;
    }

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
    public ResponseEntity<?> process() {
        ProcessTimeUseCase.ProcessResult result = processTimeUseCase.process();

        if (!result.success()) {
            ErrorResponse error = new ErrorResponse(
                "SERVICE_UNAVAILABLE",
                result.message(),
                Instant.now().toString()
            );
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }

        ProcessResponse response = new ProcessResponse(
            result.success(),
            result.degraded(),
            result.timeData(),
            result.message(),
            result.pendingCount()
        );

        if (result.degraded()) {
            return ResponseEntity.accepted().body(response);
        }

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
        GetStatusUseCase.StatusResult result = getStatusUseCase.getStatus();

        StatusResponse response = new StatusResponse(
            result.circuitState().name(),
            result.pendingMessages(),
            result.gbpServiceAvailable(),
            result.ginServiceAvailable()
        );

        return ResponseEntity.ok(response);
    }
}
