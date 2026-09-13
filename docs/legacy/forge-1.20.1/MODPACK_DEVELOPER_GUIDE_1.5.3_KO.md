# The Crowns 1.5.3 — 모드팩 개발자 설명서

대상: Minecraft 1.20.1 / Forge 47.4.x / Java 17 / Curios 5.14.1+  
mod id: `glitchedcrown`  
네트워크 프로토콜: `12`

## SERVER config

설정 파일은 월드별 `<월드>/serverconfig/glitchedcrown-server.toml`에 생성됩니다. `.minecraft/config/`의 전역 설정이 아닙니다. 서버가 최종 권한을 가지며 접속한 클라이언트에 동기화됩니다.

1.5.3은 기존 Glitched/Unleashed 설정 외에 신규 Crown 4종의 실제 동작 수치 38개를 제공합니다.

| 그룹 | 설정 범위 |
|---|---|
| `temporalCrown` | 이동·공격·비행 속도, 음식/물약 사용 속도, 비전투 판정, 시간 역행/시간 왜곡 쿨타임, 지속시간·범위·감속률 |
| `frostCrown` | 공격력·방어력·강인함, 추운 생명체 우호 범위와 바이옴 온도, 서리 스택·감속·감쇠·빙결, 추가 냉기 피해 |
| `divineCrown` | 고정/비율 최대 체력, 방어력·강인함, 회복 배율, 해로운 효과 정화 회복, 이로운 효과 지속시간, 성역 쿨타임·범위 |
| `cursedCrown` | 능력치 페널티, 약탈·행운, 경험치 배율, 해방 지속시간·쿨타임·범위·회복량 |

설정 숫자는 아이템 상세 설명과 `제왕의 길` 로어북에 반영됩니다. 저주받은 왕관의 귀속, 합법적 해제 예외, 사망 유지, 저주 변환 안전성은 무결성 규칙이라 설정으로 끌 수 없습니다.

## 저주 변환 경계

저주 인챈트가 붙어 저주받은 왕관으로 변할 수 있는 원본은 다음 명시적 목록뿐입니다.

- Tier I: 불타는, 강철화, 서리
- Tier II: 핏빛, 암흑, 전사, 신성
- Tier III: 빛, 차원, 천사, 시간

Glitched, Unleashed, U², 저주받은 왕관과 미래에 추가될 Crown은 자동 포함되지 않습니다. 변환 시 모든 curse 인챈트를 제거하고 비저주 인챈트와 허용된 스택 데이터를 보존합니다.

## 데이터팩·API 보호

퀘스트 NPC와 스크립트 보스를 다음 태그로 보호할 수 있습니다.

- `#glitchedcrown:protected_from_crown_offense`
- `#glitchedcrown:protected_from_removal_ray`
- `#glitchedcrown:protected_from_annihilation`
- `#glitchedcrown:protected_from_execution`

`com.glitchedcrown.api.event`의 `CrownAbilityTargetEvent`, `CrownReviveEvent`는 취소 가능합니다. 네트워크 패킷은 능력을 요청할 뿐이며 장착, 설정, 진행도, 쿨타임, 보호 대상과 이벤트를 서버가 다시 검사합니다.

## 진단 명령

- `/crown inspect <player>`: Crown과 주요 런타임 상태 확인
- `/crown diagnostics`: 핵심 보호 Mixin의 실제 적용 여부 확인

첫 플레이어 접속 뒤에도 Mixin 감사를 한 번 로그에 남깁니다. `FAIL`이면 사망 유지나 강제 해제 방어를 신뢰할 수 없으므로 배포를 중단해야 합니다.

## 호환 목록

| 대상 | 선언 범위 | 2026-08-19 시험 | 연동 내용 |
|---|---:|---:|---|
| Curios | 5.14.1+ | 5.14.1 PASS | 필수, 바닐라 머리 및 `head`/`hat` |
| JEI | 15.20+ | 15.20.0.110 PASS | Builder 카테고리·촉매·공개 레시피 12개·전송 |
| EMI | 1.1.x | 1.1.24 PASS | 네이티브 카테고리·작업대·공개 레시피 12개·전송 |
| REI | 12.x | 12.1.785 PASS | 네이티브 카테고리·작업대·디스플레이·전송 |
| FTB Teams | 선택 | 미실행 | 리플렉션 기반 같은 팀 보호 |
| Vestiges of the Present | 선택 | 미실행 | 격리된 soft compat |
| Ice and Fire | 선택 | 미실행 | registry ID 기반 시체 보존 대응 |
| T.O Magic / Iron's Spells / L2 Hostility / Cataclysm | 선택 | 미실행 | 강제 이동 호출 인식 |
| Corail Tombstone | 선택 | 미실행 | 조기 사망 격리 구조는 존재하나 동시 설치 런타임 검증 전에는 완전 호환으로 표기 금지 |

실제로 시험한 정확한 버전과 결과는 `PROJECT_STATE_KO.md`에 기록합니다. JEI/EMI/REI API stub은 컴파일에만 사용하며 배포 JAR에는 포함하지 않습니다.

## 모드팩 이용과 수익화

개인 사용과 수정하지 않은 JAR의 무료·비수익 모드팩 포함은 허가합니다. 현재 라이선스 메타데이터는 `All Rights Reserved`이므로 수익화 모드팩, JAR 단독 재배포, 수정 바이너리, 포크, 기타 상업 이용은 저작자의 별도 허가가 필요합니다. CurseForge·Modrinth 보상, 광고 분배, 제휴 수익처럼 모드팩으로 인해 제작자에게 금전적 이익이 생기면 수익화 모드팩으로 취급합니다.

## 차기 버전 배포 규칙

1.5.3의 현재 산출물 이름은 재현성을 위해 유지합니다. 다음 버전에서는 `build.gradle`의 archive base name에서 버전을 제거해 Gradle이 버전을 한 번만 붙이게 하고, manifest·`mods.toml`·프로토콜·이 호환 목록을 함께 검증한 뒤 배포용 이름을 확정합니다.

이전 `MODPACK_DEVELOPER_GUIDE_1.4.2_KO.md`는 과거 구현 기록이며 현재 기준 문서가 아닙니다.
