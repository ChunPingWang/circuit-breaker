#!/bin/bash

# 整合測試腳本
# 每 10 秒發送 request 給 gbp-service 和 gin-service
# 使用 Ctrl+C 停止腳本

GBP_SERVICE_URL="http://localhost:8081/api/time"
GIN_SERVICE_URL="http://localhost:8082/api/time"

# 顏色定義
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 計數器
request_count=0

# 捕捉 Ctrl+C 信號
trap cleanup SIGINT SIGTERM

cleanup() {
    echo ""
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}整合測試結束${NC}"
    echo -e "${YELLOW}總共發送了 ${request_count} 輪請求${NC}"
    echo -e "${YELLOW}========================================${NC}"
    exit 0
}

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}開始整合測試${NC}"
echo -e "${BLUE}GBP Service: ${GBP_SERVICE_URL}${NC}"
echo -e "${BLUE}GIN Service: ${GIN_SERVICE_URL}${NC}"
echo -e "${BLUE}間隔時間: 10 秒${NC}"
echo -e "${BLUE}按 Ctrl+C 停止測試${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

while true; do
    ((request_count++))
    current_time=$(date '+%Y-%m-%d %H:%M:%S')

    echo -e "${YELLOW}[第 ${request_count} 輪] ${current_time}${NC}"
    echo "----------------------------------------"

    # 發送請求給 GBP Service (GET)
    echo -e "${BLUE}=> 發送 GET 請求到 GBP Service...${NC}"
    gbp_response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X GET "${GBP_SERVICE_URL}" 2>&1)
    gbp_http_code=$(echo "$gbp_response" | grep "HTTP_CODE:" | cut -d':' -f2)
    gbp_body=$(echo "$gbp_response" | grep -v "HTTP_CODE:")

    if [ "$gbp_http_code" = "200" ]; then
        echo -e "${GREEN}   GBP Service [HTTP ${gbp_http_code}]: ${gbp_body}${NC}"
    else
        echo -e "${RED}   GBP Service [HTTP ${gbp_http_code}]: ${gbp_body}${NC}"
    fi

    # 發送請求給 GIN Service (POST)
    echo -e "${BLUE}=> 發送 POST 請求到 GIN Service...${NC}"
    gin_response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST "${GIN_SERVICE_URL}" \
        -H "Content-Type: application/json" \
        -d '{"timestamp": "'"$(date -Iseconds)"'"}' 2>&1)
    gin_http_code=$(echo "$gin_response" | grep "HTTP_CODE:" | cut -d':' -f2)
    gin_body=$(echo "$gin_response" | grep -v "HTTP_CODE:")

    if [ "$gin_http_code" = "200" ]; then
        echo -e "${GREEN}   GIN Service [HTTP ${gin_http_code}]: ${gin_body}${NC}"
    else
        echo -e "${RED}   GIN Service [HTTP ${gin_http_code}]: ${gin_body}${NC}"
    fi

    echo ""

    # 等待 10 秒
    sleep 10
done
