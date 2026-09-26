# The Crowns 1.0.4 현재 상태 문서

이 문서는 일반 채팅, 모드팩 검토, 버그 보고에서 **The Crowns 1.0.4의 현재 구현 기준**으로 사용할 수 있는 요약이다. 기능 판단이 필요하면 이 문서와 저장소의 해당 플랫폼 소스를 함께 기준으로 삼는다.

## 모드 개요

The Crowns(`thecrowns`)는 왕관 장비를 제작·장착하여 전투, 생존, 이동, 보조 능력을 얻는 Minecraft 진행 모드다. 왕관은 바닐라 머리 방어구 슬롯과 Curios 슬롯에 장착할 수 있으며, Curios 전용 `crown` 슬롯도 제공한다. 동일 왕관의 중복 장착은 허용하지 않는다.

현재 왕관 계열은 불타는, 강철화, 서리, 핏빛, 암흑, 전사, 신성, 빛, 차원, 천사, 시간, 저주, 글리치, 해방, U², 그림자, 심해로 구성된다. 제작은 Crown Builder 티어 시스템을 사용한다.

## 보관된 배포물

| 대상 | 파일 | SHA-256 |
| --- | --- | --- |
| Minecraft 1.20.1 Forge | `thecrowns-forge-1.20.1-1.0.4.jar` | `C5172DC59BA7C64545F08C019ED0BAB62495F545F7B392574B8649A3C78E84C2` |
| Minecraft 1.21.1 NeoForge | `thecrowns-neoforge-1.21.1-1.0.4.jar` | `E095B5792CC6C5413B703F2FBF19469498D6915F1162CC33161391767FB56BD5` |

두 JAR은 2026-09-26 워크스페이스 스냅샷에서 생성했다. NeoForge 1.21은 이식 작업용 경로만 있으며, 이 스냅샷에는 검증된 배포 JAR이 없다.

이 배포물에는 기능 수치·범위·지속시간을 바꾸지 않는 서버 tick 최적화가 포함된다. 왕관 상태가 없는 LivingEntity는 고급 왕관 상태 처리를 건너뛰며, Time Warp·저주 왕관·암흑 왕관·Glitched 주변 효과의 반복 검색 비용을 줄였다.

## 1.0.4의 신규 왕관

### 그림자 왕관

- 능력치: 공격력 +7, 공격 속도 +7%.
- 그림자 갈고리: 자신이 원인이 아닌 강제 이동 및 넉백을 무시한다.
- 어둠과 하나 된 자: 8초 동안 공격하거나 공격받지 않으면 절대적 투명화가 된다. 착용자와 갑옷을 숨기고, 50블록 안의 적대 몹이 목표로 삼지 않게 한다. 공격 또는 적에게 받은 피해로 해제되며, 낙하 피해 같은 환경 피해로는 해제되지 않는다.
- 기습: 투명화 상태에서 가한 근접·투사체 공격의 최종 피해가 4배가 된다.
- Enhanced AI의 `TeleportToTarget` 계열 엔더맨 강제 순간이동은 그림자 갈고리로 차단한다. 일반 환경 피해 중 정상 이동 갱신은 차단하지 않는다.

### 심해의 왕관

- 능력치: 최대 체력 +10, 수중 이동 속도 +25%.
- 모든 수중 생명체 및 물에서 활동하는 몬스터가 착용자를 공격 대상으로 삼지 않는다.
- 물속에서 숨을 쉬며, 물과 접촉한 뒤 3초간 비전투 상태이면 초당 체력 1을 회복한다. 10초 이상 연속 접촉하면 초당 회복량이 5가 된다.
- 물속에서는 공격력·방어력·이동 속도가 25% 증가한다. 20블록 안에 물이 있으면 15%, 가장 가까운 물이 10블록 이상 떨어져 있으면 10% 증가한다.
- 채굴 피로에 면역이다.

두 왕관에는 전용 인벤토리 아이콘, 착용 텍스처, Crown Lorebook 항목이 있다. 두 왕관 모두 바닐라 머리 슬롯 및 Curios 장착 판정을 사용한다.

## 글리치 왕관 융합

글리치 왕관은 하나의 왕관을 융합해 선택 능력을 계승한다.

- 그림자 융합: 절대적 투명화와 기습만 계승한다. 그림자 갈고리와 기본 능력치는 계승하지 않는다.
- 심해 융합: 수중 호흡, 수중 비전투 체력 재생, 수중 생물 비적대만 계승한다. 채굴 피로 면역 및 모든 능력치 보너스는 계승하지 않는다.

## 소스 안내

| 내용 | Forge 1.20.1 | NeoForge 1.21.1 |
| --- | --- | --- |
| 신규 왕관 능력 | `platforms/forge-1.20.1/src/main/java/com/thecrowns/logic/ShadowAbyssalLogic.java` | `platforms/neoforge-1.21.1/src/main/java/com/thecrowns/logic/ShadowAbyssalLogic.java` |
| 글리치 융합 | `platforms/forge-1.20.1/src/main/java/com/thecrowns/logic/GlitchedCrownFusion.java` | `platforms/neoforge-1.21.1/src/main/java/com/thecrowns/logic/GlitchedCrownFusion.java` |
| 강제 이동 호환 | `platforms/forge-1.20.1/src/main/java/com/thecrowns/compat/ForcedMovementCompat.java` | `platforms/neoforge-1.21.1/src/main/java/com/thecrowns/compat/ForcedMovementCompat.java` |
| 아이템·제작법·번역 | `platforms/forge-1.20.1/src/main/resources/` | `platforms/neoforge-1.21.1/src/main/resources/` |

## 검증 상태

- Forge 1.20.1 및 NeoForge 1.21.1 JAR 컴파일·패키징을 완료했고, JAR 안에 그림자/심해 능력 클래스, 제작법, 아이콘·외형 리소스가 포함된 것을 확인했다.
- JSON 리소스와 한·영 번역 키 검증은 릴리스 전 다시 실행해야 한다.
- 이 스냅샷의 최신 수정 사항에 대한 실제 Minecraft 플레이 통합 테스트는 아직 완료하지 않았다. 따라서 빌드 성공과 실게임 검증을 같은 의미로 취급하지 않는다.

## 일반 채팅에 사용하는 법

일반 채팅에서 이 버전을 논의할 때는 이 파일과 필요 시 해당 JAR 또는 저장소 링크를 첨부한다. 일반 채팅은 이 로컬 저장소를 자동으로 읽지 않으므로, 이 문서가 현재 상태를 전달하는 기준 자료다.
