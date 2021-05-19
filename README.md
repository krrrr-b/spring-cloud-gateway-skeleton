## [for webflux_beginner](https://github.com/krrrr-b/spring-webflux-skeleton/raw/master/사내_엔지니어링데이_웹플럭스_공유_자료.pdf)


## modules
- kotlin 1.4

- spring boot 2.4.2
    - webFlux
    - spring data redis reactive
    - spring data mongo reactive
    - spring reactive kafka (*todo)
  
- data
    - redis
    - mongo

- etc
    - resilience4j
    - feign reactor
  
## package architecture
- `internal-api`: api 제공을 프레젠테이션, 비즈니스 레이어 모듈

- `data`
  - `common`: 공통 데이터 및 설정 모듈
  - `data-mongo`: 몽고 데이터 모듈
  - `data-redis`: 레디스 데이터 모듈
  
- `core`
  - `common`: 공통 설정 모듈
  - `common-api`: 공통 api 인터페이스 모듈
  
- `client`
  - `common`: 공통 외부 통신 모듈 (feign)
  - `unknown1-client`: 클라이언트 모듈1
  - `unknown2-client`: 클라이언트 모듈2

## feature
### circuit breaker
- resilience4j 설정으로 실패율, 지연율을 설정에 따라 계산하여 써킷을 관리
  - [*설정: ResilienceConfiguration](https://github.com/krrrr-b/spring-webflux-skeleton/blob/master/internal-api/src/main/kotlin/com/skeleton/webflux/internal/api/config/resilience/ResilienceConfiguration.kt)
  - [*알림 설정: CustomResilienceRegistryEventConsumer](https://github.com/krrrr-b/spring-webflux-skeleton/blob/master/internal-api/src/main/kotlin/com/skeleton/webflux/internal/api/config/resilience/CustomResilienceRegistryEventConsumer.kt)
- default resilience 설정이 있으며 msa 시스템, 인프라에 따라 구분해 놓음

