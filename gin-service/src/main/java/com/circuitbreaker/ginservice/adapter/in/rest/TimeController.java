package com.circuitbreaker.ginservice.adapter.in.rest;

import com.circuitbreaker.ginservice.adapter.in.rest.dto.AckResponse;
import com.circuitbreaker.ginservice.adapter.in.rest.dto.TimeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Tag(name = "Time", description = "時間接收 API")
@RestController
@RequestMapping("/api")
public class TimeController {

    @Operation(
        summary = "接收時間資料",
        description = "接收並儲存來自 mp-service 的時間資料"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "成功接收時間資料",
            content = @Content(schema = @Schema(implementation = AckResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "請求格式錯誤"
        )
    })
    @PostMapping("/time")
    public ResponseEntity<AckResponse> receiveTime(@RequestBody TimeRequest request) {
        // TODO: 實作時間接收邏輯
        AckResponse response = new AckResponse(
            true,
            Instant.now().toString(),
            "Time data received successfully"
        );
        return ResponseEntity.ok(response);
    }
}
