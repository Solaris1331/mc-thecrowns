# The Crowns

The Crowns는 1.0.0 베타로 새롭게 시작하는 Minecraft 장비·진행 모드입니다.

## 지원 대상

- Forge 1.20.1
- NeoForge 1.21
- NeoForge 1.21.1

각 대상은 `platforms/` 아래의 독립 프로젝트로 관리합니다. 공통 기능은 세 플랫폼에서 같은 동작을 유지하고, 로더·Minecraft 버전에 의존하는 코드는 각 플랫폼에 둡니다.

## 현재 상태

`platforms/forge-1.20.1`의 최신 버전은 **1.0.4**입니다. 이 기준선을 바탕으로 `platforms/neoforge-1.21`과 `platforms/neoforge-1.21.1`에 Java 21 NeoForge 이식을 진행합니다.

기존 `glitchedcrown` Forge 1.20.1 1.5.3 소스와 베타 이력은 `docs/legacy/forge-1.20.1/`, Git 태그 `forge-1.20.1-1.5.3`에 보존합니다.

일반 채팅·검토에 사용할 1.0.4 JAR과 현재 상태 요약은 [`releases/1.0.4/`](releases/1.0.4/)에 보관합니다. 구현 소스는 계속 `platforms/` 아래에서 관리합니다.

## 빌드

저장소 루트에서 Forge 1.20.1 기준선을 빌드합니다.

```powershell
.\gradlew.bat :platforms:forge-1.20.1:clean :platforms:forge-1.20.1:build
```

배포 JAR은 The Crowns 1.0.0부터 게시합니다. 레거시 1.5.3 바이너리는 이 저장소에 올리지 않습니다.
