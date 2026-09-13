# The Crowns 1.4.2 — 모드팩 개발자용 설명서

> 보관 문서: 현재 1.5.3 기준은 `MODPACK_DEVELOPER_GUIDE_1.5.3_KO.md`를 사용하세요.

대상: **Minecraft 1.20.1 / Forge 47.4.x / Java 17**  
모드 표시명: **The Crowns**  
내부 mod id: **`glitchedcrown`**  
필수 의존성: **Curios 5.14.1+**  
네트워크 프로토콜: **8**  
현재 라이선스 메타데이터: **All Rights Reserved**

> 이 문서는 1.4.2 현행 구현을 기준으로 한다. 1.4.1에서 문서화했던 Builder/JEI/발전과제/자동화/수리/외형 회귀 항목은 1.4.2에서 보강되었다.

---

# 1. 설계 역할

The Crowns는 다음 두 층으로 나뉜다.

1. **Tier I~III 일반 Crown** — 고유한 빌드/전투 역할을 제공하는 장비
2. **Tier IV Glitched / Unleashed + U²** — 서버 진행도, config, 보호 API, 강제 피해 우회까지 포함하는 최종급 시스템

1.4.2에서는 여기에 **Crown Builder I~IV**가 추가되어 Crown 제작 진행을 단계화한다.

---

# 2. 주요 Registry ID

## Crown 아이템

| 한국어 | ID | 제작 Tier |
|---|---|---:|
| 불타는 왕관 | `glitchedcrown:burning_crown` | I |
| 강철화 왕관 | `glitchedcrown:ironforged_crown` | I |
| 핏빛 왕관 | `glitchedcrown:bloody_crown` | II |
| 암흑의 왕관 | `glitchedcrown:darkened_crown` | II |
| 전사의 왕관 | `glitchedcrown:warrior_crown` | II |
| 빛의 왕관 | `glitchedcrown:crown_of_light` | III |
| 차원의 왕관 | `glitchedcrown:dimensional_crown` | III |
| 천사의 왕관 | `glitchedcrown:angelic_crown` | III |
| 글리치 왕관 | `glitchedcrown:glitched_crown` | IV |
| 해방된 왕관 | `glitchedcrown:unleashed_crown` | IV |
| 해방된 해방된 왕관 | `glitchedcrown:unleashed_unleashed_crown` | 없음/admin |

## Builder/재료

- `glitchedcrown:crown_builder_t1`
- `glitchedcrown:crown_builder_t2`
- `glitchedcrown:crown_builder_t3`
- `glitchedcrown:crown_builder_t4`
- `glitchedcrown:compressed_emerald_block`
- `glitchedcrown:ironforged_core`
- `glitchedcrown:rotten_flesh_block`
- `glitchedcrown:big_rotten_flesh_block`

## Block ID

- `glitchedcrown:crown_builder_t1`
- `glitchedcrown:crown_builder_t2`
- `glitchedcrown:crown_builder_t3`
- `glitchedcrown:crown_builder_t4`
- `glitchedcrown:compressed_emerald_block`
- `glitchedcrown:rotten_flesh_block`
- `glitchedcrown:big_rotten_flesh_block`

---

# 3. Crown 장착 감지

Curios 데이터 태그:

- `data/curios/tags/items/head.json`
- `data/curios/tags/items/hat.json`

모든 11개 Crown이 두 태그에 들어 있다.

일반 Head 슬롯과 Curios 슬롯을 함께 검사한다. 서로 다른 Crown이 동시에 존재하면 일반 Tier Crown 효과는 독립적으로 활성화될 수 있다.

1.4.2에서는 Standard Crown과 Glitched/Unleashed/U² 모두 vanilla `IForgeItem#canEquip` 경로와 Curios `ICurioItem#canEquip` 경로에서 **동일 Item 종류의 중복 착용을 거부**한다. 서로 다른 Crown 동시 착용은 허용된다.

---

# 4. Crown Builder 구현

## 4.1 블록/BlockEntity 구조

공통 BlockEntity:

`CrownBuilderBlockEntity`

- 내부 `NonNullList<ItemStack>` 9칸
- **외부에는 `Container`를 구현하지 않음**
- GUI에만 private `Container` view 제공
- `MenuProvider` 구현
- Forge ItemHandler capability 미노출
- 블록 상태에서 Builder Tier 판정
- 결과 슬롯은 BlockEntity에 저장하지 않는 **가상 ResultContainer**

블록 파괴 시 내부 inventory가 비어 있지 않으면 `BlockEntityTag`로 Builder ItemStack에 저장된다. 재설치 시 BlockItem의 일반 BlockEntityTag 적용 경로를 통해 복원되는 구조다.

업그레이드 recipe는 중앙 Builder의 `BlockEntityTag`와 custom name을 결과 Builder에 복사한다.

## 4.2 GUI

- 3×3 input slots: 9칸
- virtual result: 1칸
- player inventory + hotbar
- 우측 상단에 `Tier I/II/III/IV` 표시

InputSlot과 BlockEntity의 `canPlaceItem()`에서 Crown Builder 아이템 입력을 거부한다.

## 4.3 결과 처리

결과를 직접 가져갈 때:

1. 현재 입력으로 recipe 재검색
2. Builder tier 검증
3. 필요 advancement 검증
4. remainder 수용 가능성 검증
5. 9개 입력 1개씩 소비
6. 각 crafting remainder를 원래 슬롯에 복귀
7. 결과 아이템 획득

remainder와 기존 잔여 스택이 충돌하면 결과 pickup을 막는다.

## 4.4 Custom recipe type

Serializer ID:

`glitchedcrown:crown_builder`

1.4.2 JSON 확장 필드:

```json
{
  "type": "glitchedcrown:crown_builder",
  "category": "equipment",
  "required_tier": 3,
  "pattern": ["ABC", "DEF", "GHI"],
  "key": {
    "A": { "item": "minecraft:diamond" }
  },
  "potion_slots": {
    "0": "minecraft:strong_strength"
  },
  "require_enchanted_book_slot": 5,
  "require_advancement": "example:advancement",
  "preserve_dragon_egg": false,
  "result": {
    "item": "example:result"
  }
}
```

### 필드 의미

- `required_tier`: 1~4, **최소 요구 Builder Tier**
- `pattern`: 정확히 3행, 각 행 3문자
- `key`: vanilla shaped recipe와 유사한 Ingredient JSON
- `potion_slots`: 특정 slot index가 특정 Potion registry ID여야 함
- `require_enchanted_book_slot`: 지정 슬롯이 실제 enchantment tag를 가진 Enchanted Book인지 검증
- `require_advancement`: 결과 제공 전 해당 player advancement 완료 요구
- `preserve_dragon_egg`: 중앙 slot 4가 Dragon Egg이면 remainder로 반환

상위 Builder는 하위 `required_tier` recipe를 사용할 수 있다.

## 4.5 1.4.2 built-in tier map

- Tier I: Burning, Ironforged
- Tier II: Bloody, Darkened, Warrior
- Tier III: Crown of Light, Dimensional, Angelic
- Tier IV: Glitched, Unleashed
- U²: recipe 없음

---

# 5. Builder 제작/업그레이드 Recipe ID

- `glitchedcrown:crown_builder_t1`
- `glitchedcrown:crown_builder_t2`
- `glitchedcrown:crown_builder_t3`
- `glitchedcrown:crown_builder_t4`
- `glitchedcrown:compressed_emerald_block`
- `glitchedcrown:compressed_emerald_block_unpacking`

T2~T4는 중앙의 이전 Builder inventory 보존을 위해 custom serializer를 사용한다.

### T2 serializer

`glitchedcrown:tier2_builder_upgrade`

### T3 serializer

`glitchedcrown:tier3_builder_upgrade`

### T4 serializer

`glitchedcrown:tier4_builder_upgrade`

### 1.4.2 T4 레시피

```text
Netherite Block     / Compressed Emerald / Netherite Block
Compressed Emerald / Builder III        / Compressed Emerald
Netherite Block     / Compressed Emerald / Netherite Block
```

1.4.2에서 설계 원안인 **Netherite corner / Compressed Emerald edge** 배치로 복원되었다.

---

# 6. Built-in Crown recipe ID와 특수 검증

Crown recipe ID는 기존과 같은 이름을 유지하지만 1.4.2에서는 대부분 `glitchedcrown:crown_builder` serializer를 사용한다.

| ID | Tier | 추가 검증 |
|---|---:|---|
| `glitchedcrown:burning_crown` | I | Lava Bucket remainder |
| `glitchedcrown:ironforged_crown` | I | 없음 |
| `glitchedcrown:bloody_crown` | II | 없음 |
| `glitchedcrown:darkened_crown` | II | 없음 |
| `glitchedcrown:warrior_crown` | II | slot 0 = `minecraft:strong_strength` |
| `glitchedcrown:crown_of_light` | III | `#glitchedcrown:amethyst_buds` |
| `glitchedcrown:dimensional_crown` | III | slot 5 실제 enchanted book |
| `glitchedcrown:angelic_crown` | III | slot 6 Slow Falling, slot 8 Leaping |
| `glitchedcrown:glitched_crown` | IV | Dragon Egg remainder 항상 보존 |
| `glitchedcrown:unleashed_crown` | IV | slot 6 `minecraft:empty` potion + `glitchedcrown:glitched_crown` advancement |

U²는 recipe가 없다.

---

# 7. Tier I~III 능력 구현 요약

## Burning

- +10 Max Health / +10 Armor / +5 Toughness
- Fire/Lava damage cancel
- Poison/Wither/Slowness/Nausea remove + application immunity
- retaliation = `4 + current Armor × 0.5`
- burn = `4s + current Toughness × 0.5s`
- wearer-target pair cooldown 20 ticks

## Ironforged

- +30 Armor / +30 Toughness / -10% Movement Speed
- K, 120s
- owned Iron Golem UUID 1개 추적
- spawn snapshot으로 owner current Max Health / Armor / Toughness를 golem base에 addition
- owner가 공격한/owner를 공격한 target 전달
- FTB Teams / scoreboard ally player 차단
- Creeper proactive target 보강

## Bloody

- +50 Max Health
- +15% Max Health (`MULTIPLY_TOTAL`)
- final resolved damage 기준 15% heal
- no damage 60 ticks 이후 초당 1 HP regen
- direct melee + Enemy 또는 angry NeutralMob kill만 Blessing proc
- proc: heal 20% max health, permanent +1 max health, max 50
- proc CD 600 ticks
- 피해 이벤트마다 CD 20 ticks 감소
- permanent HP modifier는 Crown을 벗어도 유지

## Darkened

- +20 Armor / +10% Movement Speed
- outgoing final ×2
- incoming final ×1.5
- Darkness/Blindness immunity
- 160 ticks마다 다음 attack cancel
- Warden target/anger memory 제거; wearer가 해당 Warden을 공격했으면 예외

## Warrior

- +20 Max Health / +20 Attack Damage
- Armor / Toughness `MULTIPLY_TOTAL -0.20`
- Bow/Crossbow use 차단
- K Duel: 300 ticks, CD 2400 ticks
- Duel 중 direct LivingEntity melee source 외 피해 차단
- 공격속도 +25%
- 32 block ray target
- Player / Enemy / NeutralMob만 표식
- Slowness II / Weakness II / Blindness II 300 ticks
- 표식 대상 kill 시 remaining CD -600 ticks
- setHealth 감소 경로도 Mixin에서 차단하되 normal max-health clamp 예외

## Crown of Light

- +20 Max Health / +15 Armor / +7 Toughness
- HP >= 50%: Movement Speed +20%, outgoing final ×1.5, incoming final ×0.65

## Dimensional

- Movement +30%, Attack Damage +10, Attack Speed +20%
- non-environment source 35% complete cancel
- every 200 ticks next outgoing final ×3
- evade during recharge → readyAt -20 ticks
- void cancel
- Y < -200 rescue to Y 300 or dimension ceiling-2
- rescue until onGround fall protection
- new mob target acquisition range half of FOLLOW_RANGE

## Angelic

- +10 Max Health / +35% Movement Speed
- flying speed = original ×1.35 while worn
- fall/in_wall/cramming attack cancel
- lethal guard: 600s CD / 4s invulnerability
- K flight 30s / CD 120s
- flight grant ownership bookkeeping 포함

---

# 8. Standard Crown durability / enchantment

Tier I~III Standard Crown:

- max durability = **4096**
- `ArmorItem implements ICurioItem`
- helmet-compatible enchantment proxy 검사
- enchantment table + enchanted book 지원
- Nether Star 1개 전용 모루 수리: 최대 내구도 25%
- `AnvilUpdateEvent`: material cost 1 / level cost 10

Glitched/Unleashed/U²:

- item NBT에 `Unbreakable=true` 강제
- explosion item damage 방지 경로 포함
- helmet-compatible enchantments 지원

1.4.2에서 “Nether Star로 25% 수리 / 모루 비용 10”이 구현되어 있다.

---

# 9. Glitched Crown 진행 게이트

일반 Glitched Crown의 **사용 가능 여부**는 `CrownAdvancementGate`가 서버에서 검사한다.

기본값:

`requiredAdvancementPoints = 4`

Core advancement CSV:

```text
minecraft:end/kill_dragon
minecraft:nether/summon_wither
minecraft:adventure/kill_mob_near_sculk_catalyst
minecraft:nether/netherite_armor
```

Substitute CSV:

```text
minecraft:nether/fast_travel
minecraft:nether/uneasy_alliance
minecraft:nether/all_potions
minecraft:nether/all_effects
minecraft:nether/explore_nether
minecraft:end/levitate
minecraft:adventure/kill_all_mobs
minecraft:adventure/sniper_duel
minecraft:adventure/arbalistic
minecraft:adventure/bullseye
minecraft:nether/create_full_beacon
minecraft:adventure/hero_of_the_village
```

각 완료 항목은 동일하게 1점이며 core 최소 개수 요구는 없다. 총합이 required points 이상이면 활성화된다.

---

# 10. Glitched 기본 수치

`sharedStats` 기본값:

| Config | 기본값 |
|---|---:|
| `maxHealth` | 80 |
| `armor` | 40 |
| `armorToughness` | 40 |
| `luck` | 7 |
| `lootingLevel` | 7 |
| `attackDamage` | 40 |
| `knockbackResistance` | 1.0 |
| `interactionReach` | 3.5 |

Glitched default:

- Flight
- Night Vision
- status/gravity-changing effect protection
- forced movement protection
- beneficial-effect removal protection
- Shield max 30
- shield damage multiplier 0.20
- recharge 10s/stack
- revive 300s / post-revive invulnerability 5s
- G range 50 / radius 2
- G normal/player/boss max-health fraction 0.65 / 0.45 / 0.25
- execution threshold 0.10
- nullification radius 30

---

# 11. Unleashed / U² damage semantics

## 11.1 damageMode

일반 Unleashed는 config:

- `NORMAL`
- `BYPASS_TAGS`
- `RAW_HEALTH` — 기본

U²는 항상 full-power RAW_HEALTH 계열 동작을 사용한다.

## 11.2 Power of the End — H

H state:

`0 → 1(50%) → 2(100%) → 0`

일반 Unleashed 기본:

| 단계 | final multiplier | attack speed | attack damage |
|---|---:|---:|---:|
| 50% | 1024 | +5 | +50 |
| 100% | 32767 | +10 | +100 |

공통:

- Armor Pierce +100
- Protection Shred +1.0
- hit target absolute bind 5s

구형 half/full Crit Chance / Crit Damage config key는 **1.4.0 이후 runtime에서 사용하지 않는 legacy key**다.

## 11.3 G

일반 Unleashed:

- remaining raw body health → 0 collapse
- Ice and Fire dragon corpse preservation special case

U²:

```text
afterFixed = max(0, health - maxHealth * 16)
if afterFixed > 0: raw health -> 0
```

즉 역사적 1600% max-health raw hit + collapse를 보존한다.

## 11.4 K

- duration: 60 ticks
- default radius: 75
- visible/lethal shell이 같은 속도로 확장
- player는 절멸 대상에서 제외
- ArmorStand/Hanging/AbstractVillager/Golem/Allay/Item/XP/Boat/Minecart/Display 등 여러 utility entity 보호
- tamed/owned 대상 보호

---

# 12. 서버 Config

파일:

`<world>/serverconfig/glitchedcrown-server.toml`

## general

| 키 | 기본값 | 의미 |
|---|---:|---|
| `debugLogging` | false | 강제이동/타겟 차단 디버그 로그 |

## targeting

| 키 | 기본값 |
|---|---:|
| `protectSameFTBTeam` | true |
| `protectSameScoreboardTeam` | true |
| `protectTamedEntities` | true |
| `protectOwnedEntities` | true |

U²는 이 토글을 무시하고 안전 기본값을 고정 적용한다.

## glitched

| 키 | 기본값 |
|---|---:|
| `enableRecipe` | true |
| `enableAdvancementGate` | true |
| `requiredAdvancementPoints` | 4 |
| `enableFlight` | true |
| `enableShield` | true |
| `enableRevive` | true |
| `enableRemovalRay` | true |
| `enableExecutionAura` | true |
| `enableInvulnerabilityNullificationAura` | true |
| `enableMovementImmunity` | true |
| `enableNegativeStatusImmunity` | true |
| `enableNightVision` | true |
| `protectBeneficialEffects` | true |
| `shieldStacks` | 30 |
| `shieldRechargeSecondsPerStack` | 10 |
| `shieldDamageTakenMultiplier` | 0.20 |
| `reviveCooldownSeconds` | 300 |
| `reviveInvulnerabilitySeconds` | 5 |
| `removalRayCooldownSeconds` | 150 |
| `removalRayRange` | 50 |
| `removalRayRadius` | 2 |
| `removalRayNormalMaxHealthFraction` | 0.65 |
| `removalRayPlayerMaxHealthFraction` | 0.45 |
| `removalRayBossMaxHealthFraction` | 0.25 |
| `executionThreshold` | 0.10 |
| `nullificationRadius` | 30 |

추가 문자열:

- `coreAdvancements`
- `substituteAdvancements`

## unleashed

| 키 | 기본값 |
|---|---:|
| `enableRecipe` | true |
| `requireGlitchedCrownAdvancementForCrafting` | true |
| `enableFlight` | true |
| `enableAbsoluteInvulnerability` | true |
| `enableInfiniteRevive` | true |
| `enableRemovalRay` | true |
| `enableEndPower` | true |
| `enableOblivionVeil` | true |
| `enableAnnihilation` | true |
| `enableAbsoluteDefenseBypass` | true |
| `damageMode` | RAW_HEALTH |
| `enableHiddenUtilityEffects` | true |
| `enableMovementImmunity` | true |
| `enableNegativeStatusImmunity` | true |
| `protectBeneficialEffects` | true |
| `enableInvulnerabilityNullificationAura` | true |
| `enableIntegrityProtection` | true |
| `nullificationRadius` | 200 |
| `removalRayRange` | 50 |
| `removalRayRadius` | 2 |
| `annihilationRadius` | 75 |

### endPower

| 키 | 기본값 | 상태 |
|---|---:|---|
| `halfCritChance` | 5 | legacy/unused |
| `halfCritDamage` | 5 | legacy/unused |
| `fullCritChance` | 10 | legacy/unused |
| `fullCritDamage` | 10 | legacy/unused |
| `halfFinalDamageMultiplier` | 1024 | 사용 |
| `fullFinalDamageMultiplier` | 32767 | 사용 |
| `armorPierce` | 100 | 사용 |
| `protectionShred` | 1.0 | 사용 |
| `halfAttackSpeed` | 5 | 사용 |
| `halfAttackDamage` | 50 | 사용 |
| `fullAttackSpeed` | 10 | 사용 |
| `fullAttackDamage` | 100 | 사용 |

`removalRayMaxHealthMultiplier=16` 및 `removalRayEraseRemainingHealth=true`는 config에 남아 있지만 1.4.2의 현재 일반 Unleashed G 코드에서는 실제로 참조되지 않는 legacy 성격의 값이다.

---

# 13. 관리자 명령

모두 permission level **2 이상** 요구.

## 진단

```text
/crown inspect <player>
```

출력:

- 착용 Crown type
- Glitched advancement progress
- shield stack/enabled
- revive/laser runtime switch
- laser cooldown switch
- End Power stage
- Oblivion Veil state
- damageMode
- config-immune(U²) 여부

## Glitched runtime controls

```text
/gt <player> canrevive true|false
/gt <player> shieldenable true|false
/gt <player> rivivetimeset <seconds>
/gt <player> removelaycdset <seconds>
/gt <player> canremovelay true|false
/gt <player> useremoveraycd true|false
/gt <player> shieldstack set <value>
/gt <player> shieldstack add <value>
/gt <player> shieldstack max
/gt <player> shieldstack rem
```

주의: `rivivetimeset`, `removelaycdset`는 기존 호환성을 위해 오탈자 형태의 명령명을 그대로 유지한다.

---

# 14. 데이터팩 보호 태그

Entity Type tags:

- `#glitchedcrown:protected_from_crown_offense`
- `#glitchedcrown:protected_from_removal_ray`
- `#glitchedcrown:protected_from_annihilation`
- `#glitchedcrown:protected_from_execution`

예:

```json
{
  "replace": false,
  "values": [
    "examplemod:quest_npc",
    "examplemod:scripted_boss"
  ]
}
```

첫 번째는 전역 Crown 공격 보호, 나머지는 능력별 보호다.

아이템 tag:

`#glitchedcrown:amethyst_buds`

기본 포함:

- small_amethyst_bud
- medium_amethyst_bud
- large_amethyst_bud

빛의 왕관 제작에 사용된다.

---

# 15. Public Forge API

패키지:

`com.glitchedcrown.api.event`

## CrownAbilityTargetEvent

Cancelable.

Ability enum:

- `REMOVAL_RAY`
- `EXECUTION`
- `ANNIHILATION`
- `FATE_BIND`
- `NULLIFICATION`
- `ABSOLUTE_DAMAGE`
- `INTEGRITY_RETALIATION`

예:

```java
@SubscribeEvent
public static void protectNpc(CrownAbilityTargetEvent event) {
    if (event.getTarget().getType().toString().equals("examplemod:quest_npc")) {
        event.setCanceled(true);
    }
}
```

## CrownReviveEvent

Cancelable.

Kinds:

- `GLITCHED`
- `UNLEASHED`

U²도 밸런스 config는 무시하지만 이 API cancellation과 데이터팩 보호 tag는 존중한다.

---

# 16. 동맹/소유 보호

`CrownTargeting` 기본 동작:

1. tamed entity 보호
2. owned entity 보호
3. vanilla allied/scoreboard team 보호
4. FTB Teams same team 보호

FTB Teams는 reflection-only optional compat라 모드가 없을 때 직접 클래스 링크를 요구하지 않는다.

U²는 targeting config를 무시하지만 위 safe-default protection을 고정 사용한다.

---

# 17. Optional compatibility

현행 compat 패키지:

- `FTBTeamsCompat`
- `VestigesCompat`
- `IceAndFireCompat`
- `ForcedMovementCompat`

Forced movement caller 인식에는 다음 계열이 포함되어 있다.

- Cataclysm
- Mutant Monsters
- L2 Hostility
- Travel Optics / T.O Magic 계열
- Iron's Spells root 계열
- Nightwarden
- Supernova/Black Hole 이름 계열
- Telekinesis
- Arcane Shackle
- explosion/explode caller 패턴

`debugLogging=true`이면 차단된 외부 caller를 로그에 남긴다.

---

# 18. 네트워크 권한

C2S 능력 요청:

- G Deletion Beam
- H Power of the End
- J Oblivion Veil
- K Annihilation
- K Iron Guardian
- K Warrior Duel
- K Angelic Flight

클라이언트 패킷은 “행동 요청”만 전송한다. 서버가 다시 다음을 검증한다.

- 장착 여부
- config feature switch
- cooldown
- Glitched advancement gate
- target safety
- API cancellation

피해량과 최종 대상은 서버 권한이다.

---

# 19. Persistent player data

1.4.2에서 영속적으로 유지되는 대표 상태:

- Bloody permanent health stacks
- Bloody proc readyAt
- Darkened dodge readyAt
- Dimensional empowered-hit readyAt
- Ironforged golem UUID / summon readyAt
- Warrior duel readyAt
- Angelic lethal guard readyAt
- Angelic flight readyAt
- Glitched shield/revive/ray runtime state
- Unleashed H/J state

시간 기준은 대부분 **server gameTime 기반 readyAt**이다.

따라서:

- 플레이어 로그아웃 중 서버가 계속 돌아가면 시간은 진행
- 서버 자체가 종료되어 있으면 gameTime도 멈춤

---

# 20. 1.4.2 보강 내역 / 현재 검증 경계

## 20.1 JEI

선택적 JEI 15.20+ 연동을 제공한다.

- Crown Builder 전용 category
- `Required Builder Tier: I/II/III/IV` / `필요한 Builder 티어: ...`
- Builder I~IV catalyst
- `+` recipe transfer
- 낮은 Builder Tier에서 transfer 거부
- server config상 비활성 recipe transfer 거부

JEI는 `mandatory=false`, `side=CLIENT` optional dependency다. 빌드 환경에는 실제 JEI runtime JAR이 없었으므로 최종 실게임 JEI UI 회귀 테스트는 팩 환경에서 수행해야 한다.

## 20.2 Builder advancement tree

리소스/서버 지급 로직이 구현되어 있다.

`The Crowns → Builder I → Tier I Crowns → Builder II → Tier II Crowns → Builder III → Tier III Crowns → Builder IV → Glitched → hidden Forbidden Domain`

개별 Crown advancement는 `minecraft:impossible` criterion을 사용하고 Builder ResultSlot take에서만 서버가 직접 지급한다. `/give`나 단순 pickup은 Crown 제작 advancement를 충족시키지 않는다.

## 20.3 Recipe config 연동

`CrownBuilderRecipe#isRecipeEnabled()`가 다음 기존 config를 다시 적용한다.

- `glitched.enableRecipe`
- `unleashed.enableRecipe`
- `unleashed.requireGlitchedCrownAdvancementForCrafting`

Glitched Crown Dragon Egg는 1.4.2에서 **항상 remainder로 반환**한다.

## 20.4 자동화 차단

`CrownBuilderBlockEntity` 외부 타입은 `MenuProvider`이며 `Container`를 구현하지 않는다. Forge ItemHandler capability도 노출하지 않는다. GUI는 private `Container` view만 사용하므로 vanilla Hopper/일반 Forge item pipe의 표준 접근 경로에서 Builder 내부 9칸을 발견하지 못하도록 구성했다.

## 20.5 T3/T4 Builder ItemEntity

- T3/T4: age reset으로 자연 despawn 방지, 일반 `hurt` 취소, fire clear
- T3: void 소멸 허용
- T4: dimension `minBuildHeight` 근처에서 위치/하강 속도를 보정해 공허 구조

외부 모드가 `discard()`/직접 entity removal을 강제하는 비표준 경로까지 무조건 방어한다고 주장하지 않는다.

## 20.6 Creative / T4 / Repair / 외형

- Builder I~IV 및 Compressed Emerald를 vanilla creative tabs에 추가
- T4 upgrade 레시피를 원안 배치로 수정
- Standard Crown Nether Star 25% repair / anvil cost 10 구현
- 동일 Crown 중복 vanilla Head/Curios 착용 차단
- Tier I~III 전용 equipped armor texture 사용
- Crown of Light item/equipped texture 모두 luminous cyan/blue 계열

## 20.7 런타임 검증 경계

실제 ForgeGradle `clean build`와 reobf는 성공했다. 다만 오프라인 빌드 번들의 `runServer`는 `downloadMCMeta` 단계에서 `piston-meta.mojang.com` 접근이 필요해 시작하지 못하므로 dedicated-server 장기 smoke test는 별도 환경에서 수행해야 한다.

---

# 21. 팩 배포 권장 체크리스트

1. Forge 47.4.x + Curios 버전 확인
2. Head/Hat slot 구성 확인
3. Builder T1~T4 실제 recipe craft 테스트
4. Builder break/re-place inventory 보존 테스트
5. Builder upgrade inventory 보존 테스트
6. Lava Bucket remainder 테스트
7. Glitched Dragon Egg 반환 테스트
8. Hopper input/output 여부 확인
9. JEI에서 custom recipe 노출 방식 확인
10. Glitched advancement gate 조건을 팩 진행도에 맞게 교체
11. `protected_from_*`에 퀘스트 NPC/스크립트 보스 등록
12. FTB Teams 동맹 판정 테스트
13. Unleashed RAW_HEALTH가 팩의 shield/cap/phase 시스템을 의도대로 우회하는지 확인
14. H50/H100에서 고체력 보스 및 Apothic Attributes와 성능 회귀 테스트
15. K Annihilation에서 보호해야 할 utility entity tag/API 추가
16. U²가 일반 플레이 경로로 유출되지 않는지 확인
17. 라이선스/재배포 정책 확정

---

# 22. 버전 1.4.2 요약

1.4.2는 1.4.1의 Crown Builder를 모드팩 배포에 사용할 수 있도록 다음을 보강한 버전이다.

- Tier I~IV persistent Crown Builder + tier-gated custom recipe
- 외부 표준 자동화 접근 차단
- JEI category / required-tier 표기 / `+` transfer
- Builder/Crown advancement tree
- Glitched/Unleashed recipe config 재연동
- Dragon Egg 상시 반환
- T3/T4 dropped Builder 보호
- Creative Tab 등록
- Standard Crown Nether Star 수리
- 동일 Crown 중복 장착 차단
- Tier I~III 전용 equipped texture와 luminous blue Crown of Light
- 기존 Glitched/Unleashed/U² 능력/관리자/API/보호 tag 유지

배포 전에는 실제 모드팩에서 JEI UI, Hopper/파이프, Builder break/re-place, T3/T4 ItemEntity, 모루 수리, Curios 중복 거부를 한 번씩 회귀 테스트하는 것을 권장한다.
