## modules
- kotlin 1.4

- spring boot 2.4.2
    - webflux
    - spring data redis reactive
    - spring cloud gateway
  
- data
    - redis

- etc
    - resilience4j
    - feign reactor
  
## package architecture
- `gateway`: 게이트웨이 모듈

- `data`
  - `common`: 공통 데이터 및 설정 모듈
  - `data-redis`: 레디스 데이터 모듈
  
- `core`
  - `common`: 공통 설정 모듈
  - `common-api`: 공통 api 인터페이스 모듈
