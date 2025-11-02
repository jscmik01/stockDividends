
-- 쿼리
    WITH PIVOTED_DATA AS (
        -- 1. 조건부 집계를 통해 주당 배당금과 수익률 데이터를 같은 행으로 합치기 (Pivot)
        SELECT
            T.BSNS_YEAR,
            T.REPRT_CODE,
            T.CORP_CODE,
            T.CORP_NAME,
            T.STOCK_KND,
            
            -- 누적 주당 현금배당금 (THSTRM 값이 '주당 현금배당금(원)'인 경우)
            MAX(CASE WHEN T.SE = '주당 현금배당금(원)' THEN T.THSTRM END) AS CUMULATIVE_DIVIDEND,
            
            -- 현금배당수익률 (THSTRM 값이 '현금배당수익률(%)'인 경우)
            MAX(CASE WHEN T.SE = '현금배당수익률(%)' THEN T.THSTRM END) AS REPORTED_DIVIDEND_YIELD,
            
            -- 계산 및 정렬을 위한 분기 순서 정의
            CASE T.REPRT_CODE
                WHEN '11013' THEN 1  -- 1분기
                WHEN '11012' THEN 2  -- 반기 (2분기 누적)
                WHEN '11014' THEN 3  -- 3분기
                WHEN '11011' THEN 4  -- 사업 (4분기 누적)
                ELSE 99
            END AS QUARTER_ORDER,
            
            -- 출력용 분기 이름
            CASE T.REPRT_CODE
                WHEN '11013' THEN '1분기'
                WHEN '11012' THEN '2분기'
                WHEN '11014' THEN '3분기'
                WHEN '11011' THEN '4분기'
                ELSE '기타'
            END AS QUARTER_NAME
        FROM
            STOCK_DIVIDEND_DETAIL T
        
        WHERE se in ('주당 현금배당금(원)','현금배당수익률(%)') --AND CORP_CODE = '00126380'
        
        GROUP BY
            T.BSNS_YEAR, T.REPRT_CODE, T.CORP_CODE, T.CORP_NAME, T.STOCK_KND
    ),
    CALCULATED_DIVIDEND AS (
        -- 2. LAG 함수를 사용하여 이전 분기의 누적 배당금을 가져오기
        SELECT
            P.*,
            
            -- LAG 함수: 이전 분기의 누적 배당금 (이전 행이 없으면 0)
            LAG(P.CUMULATIVE_DIVIDEND, 1, 0) OVER (
                PARTITION BY P.CORP_CODE, P.STOCK_KND, P.BSNS_YEAR
                ORDER BY P.QUARTER_ORDER
            ) AS PREV_CUMULATIVE_DIVIDEND
        FROM
            PIVOTED_DATA P
    )
    -- 3. 최종 결과 출력
    SELECT
        BSNS_YEAR,
        CORP_CODE,
        CORP_NAME,
        STOCK_KND,
        QUARTER_NAME,
        
        -- 실제 분기별 배당금 = 현재 누적 배당금 - 이전 누적 배당금
        CUMULATIVE_DIVIDEND - NVL(PREV_CUMULATIVE_DIVIDEND, 0) AS ACTUAL_DIVIDEND_AMOUNT,
        
        -- 해당 보고서에 명시된 수익률 (배당금 옆에 그대로 표시)
        REPORTED_DIVIDEND_YIELD
    FROM
        CALCULATED_DIVIDEND
    WHERE 
        CUMULATIVE_DIVIDEND IS NOT NULL -- 배당금이 없는 행은 제외 (주로 1분기 보고서의 경우 수익률 데이터는 있으나 배당금 데이터가 없는 경우를 방지)
        AND (CUMULATIVE_DIVIDEND - NVL(PREV_CUMULATIVE_DIVIDEND, 0)) > 0
    ORDER BY
        BSNS_YEAR DESC, CORP_CODE, QUARTER_NAME DESC, STOCK_KND--, QUARTER_ORDER;
        
    
-- 디비 용량
SELECT * FROM DBA_TABLESPACES;
SELECT sum(bytes)/1024/1024/1024 || 'GB' FROM dba_data_files;

-- 잔여 확인 쿼리
SELECT count(*) 
FROM (
    SELECT CORP_CODE FROM CORP_CODE WHERE STOCK_CODE IS NOT NULL 
    AND CORP_CODE NOT IN 
        ( SELECT CORP_CODE FROM STOCK_DIVIDEND_DETAIL WHERE BSNS_YEAR = '2025' AND REPRT_CODE = '11014' GROUP BY CORP_CODE )
    )   
WHERE CORP_CODE NOT IN 
(SELECT CORP_CODE FROM STOCK_DIVIDEND_DETAIL WHERE SE = '013' AND BSNS_YEAR = '2025' AND REPRT_CODE = '11011')



-- JSON 출력
SELECT
    JSON_ARRAYAGG(
        JSON_OBJECT(
            'year' VALUE T.BSNS_YEAR,
            'code' VALUE T.CORP_CODE,
            'name' VALUE T.CORP_NAME,
            'kind' VALUE T.STOCK_KND,
            'quarter' VALUE T.QUARTER_NAME,
            'actual_dividend' VALUE (T.CUMULATIVE_DIVIDEND - NVL(T.PREV_CUMULATIVE_DIVIDEND, 0)),
            'reported_yield' VALUE T.REPORTED_DIVIDEND_YIELD
        )
        ORDER BY
            T.BSNS_YEAR DESC, T.QUARTER_ORDER DESC, T.STOCK_KND
        --ORDER BY T.BSNS_YEAR DESC, T.CORP_CODE, T.STOCK_KND, T.QUARTER_ORDER
        RETURNING CLOB
    ) AS JSON_RESULT
FROM (
    WITH PIVOTED_DATA AS (
        -- 1. 조건부 집계를 통해 주당 배당금과 수익률 데이터를 같은 행으로 합치기 (Pivot)
        SELECT
            T.BSNS_YEAR,
            T.REPRT_CODE,
            T.CORP_CODE,
            T.CORP_NAME,
            T.STOCK_KND,
            
            -- 누적 주당 현금배당금 (THSTRM 값이 '주당 현금배당금(원)'인 경우)
            MAX(CASE WHEN T.SE = '주당 현금배당금(원)' THEN T.THSTRM END) AS CUMULATIVE_DIVIDEND,
            
            -- 현금배당수익률 (THSTRM 값이 '현금배당수익률(%)'인 경우)
            MAX(CASE WHEN T.SE = '현금배당수익률(%)' THEN T.THSTRM END) AS REPORTED_DIVIDEND_YIELD,
            
            -- 계산 및 정렬을 위한 분기 순서 정의
            CASE T.REPRT_CODE
                WHEN '11013' THEN 1  -- 1분기
                WHEN '11012' THEN 2  -- 반기 (2분기 누적)
                WHEN '11014' THEN 3  -- 3분기
                WHEN '11011' THEN 4  -- 사업 (4분기 누적)
                ELSE 99
            END AS QUARTER_ORDER,
            
            -- 출력용 분기 이름
            CASE T.REPRT_CODE
                WHEN '11013' THEN '1분기'
                WHEN '11012' THEN '2분기'
                WHEN '11014' THEN '3분기'
                WHEN '11011' THEN '4분기'
                ELSE '기타'
            END AS QUARTER_NAME
        FROM
            STOCK_DIVIDEND_DETAIL T
        
        WHERE se in ('주당 현금배당금(원)','현금배당수익률(%)') -- and CORP_CODE = '00126380'
        
        GROUP BY
            T.BSNS_YEAR, T.REPRT_CODE, T.CORP_CODE, T.CORP_NAME, T.STOCK_KND
    ),
    CALCULATED_DIVIDEND AS (
        -- 2. LAG 함수를 사용하여 이전 분기의 누적 배당금을 가져오기
        SELECT
            P.*,
            
            -- LAG 함수: 이전 분기의 누적 배당금 (이전 행이 없으면 0)
            LAG(P.CUMULATIVE_DIVIDEND, 1, 0) OVER (
                PARTITION BY P.CORP_CODE, P.STOCK_KND, P.BSNS_YEAR
                ORDER BY P.QUARTER_ORDER
            ) AS PREV_CUMULATIVE_DIVIDEND
        FROM
            PIVOTED_DATA P
    )
-- 3. JSON 생성을 위한 최종 데이터셋 준비
SELECT 
    * FROM 
    CALCULATED_DIVIDEND
WHERE 
    NVL(CUMULATIVE_DIVIDEND,0) > 0
) T;
    
    
    
    