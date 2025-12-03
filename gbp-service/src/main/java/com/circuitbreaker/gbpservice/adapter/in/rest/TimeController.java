package com.circuitbreaker.gbpservice.adapter.in.rest;

import com.circuitbreaker.gbpservice.adapter.in.rest.dto.TimeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Tag(name = "Time", description = "時間提供 API")
@RestController
@RequestMapping("/api")
public class TimeController {

    private static final Logger log = LoggerFactory.getLogger(TimeController.class);

    @Operation(
        summary = "取得當前時間",
        description = "回傳伺服器當前的 UTC 時間"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功取得當前時間",
            content = @Content(schema = @Schema(implementation = TimeResponse.class))
        )
    })
    @GetMapping("/time")
    public ResponseEntity<TimeResponse> getTime() {
        String currentTime = Instant.now().toString();

        // FR-1: 每次被呼叫時在 console 輸出時間日誌
        log.info("Time requested - returning: {}", currentTime);

        TimeResponse response = new TimeResponse(currentTime, "UTC");
        return ResponseEntity.ok(response);
    }
}
