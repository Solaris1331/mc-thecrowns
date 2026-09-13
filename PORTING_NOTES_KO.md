# 포팅 검증 메모

- 레시피 디렉터리 없음: `src/main/resources/data/glitchedcrown/recipes` 및 구식 `recipe` 모두 없음
- 퀘스트 디렉터리 없음: `QUEST_PACK` 없음
- 1.0.7에서 왕관 획득 도전과제 `glitchedcrown:forbidden_domain` 복원
- NeoForge 메타데이터 없음
- Forge 메타데이터: `src/main/resources/META-INF/mods.toml`
- Curios 태그 경로는 1.20.1 형식인 `data/curios/tags/items/` 사용
- Draconic Evolution이 없는 Dragonfyre에서 클래스 탐색 오류를 내지 않도록 Chaos Guardian 믹스인 제거


## 1.0.5 - 절대 아이템 무결성

- 보호 대상은 착용 중인 **Glitched Crown ItemStack 자체**이다. 다른 인벤토리 아이템까지 동결하지 않는다.
- 최초 착용 시 전체 직렬화 상태를 스냅샷으로 저장하고, 서버 틱마다 동일성을 검증한다.
- `ItemStack`, 바닐라 `Inventory`, Forge `ItemStackHandler`(Curios 포함), `LivingEntity#setItemSlot`, `EnchantmentHelper#setEnchantments`의 대표 변경/삭제 경로를 Mixin으로 즉시 차단한다.
- 위 훅을 우회해 NBT/Capability를 직접 바꾼 모드에 대해서도 다음 서버 틱에 전체 ItemStack을 스냅샷으로 복구한다.
- 착용자가 보낸 바닐라 컨테이너 클릭/크리에이티브 슬롯 패킷 처리 구간에서만 해제를 허용한다. 착용 상태를 유지한 채 상태값만 변조된 경우에는 플레이어 클릭 중이더라도 원본 상태로 되돌린다.
- 피해원/효과원 및 현재 엔티티 틱 문맥을 통해 간섭 주체를 판별할 수 있으면 `FORCED_DEATHS` 우회를 사용해 즉사시킨다. 원인 엔티티가 존재하지 않거나 판별할 수 없으면 복구만 수행한다.

### L2 Hostility `SealedItem` 확인

L2Hostility 2.5.x 소스의 `SealedItem.sealItem`은 원본 스택을 `sealedItem` NBT에 저장한 뒤
`LHItems.SEAL` 스택으로 **교체**한다. 1.0.5의 `LivingEntity#setItemSlot`, Forge
`ItemStackHandler#setStackInSlot/extractItem`, Curios unequip 차단과 틱 스냅샷 복구는 이 방식을
직접 방어한다. L2Hostility를 컴파일 의존성으로 추가하지 않아도 동작하도록 구현했다.


## 1.0.6 - 삭제 광선 비삭제화

- 광선은 더 이상 엔티티를 `discard()`하거나 직접 제거하지 않는다.
- 비생명체는 광선 피해 대상에서 제외한다.
- 모든 LivingEntity는 보스/플레이어/일반몹 구분 없이 동일한 경로를 사용한다.
- 정확한 처리 순서: `target.hurt(source, target.getMaxHealth())` → `target.kill()` → `target.die(source)`.
- `die(source)`는 앞 단계의 성공/생존 여부와 관계없이 비조건부로 호출한다.
- `FORCED_DEATHS`는 Glitched Crown의 자체 부활 훅이 이 명시적 사망 순서를 취소하지 못하게 하는 가드 플래그로만 유지한다.
- 삭제 광선 코드 경로에서는 `discard()`를 사용하지 않는다.

## 1.0.6 build-fix 1
- `CuriosDynamicStackHandlerMixin`은 Curios API가 아니라 내부 구현 클래스
  `top.theillusivec4.curios.common.inventory.DynamicStackHandler`를 대상으로 한다.
- 기존 `compileOnly ...:api`만으로는 Mixin Annotation Processor가 해당 클래스를 볼 수 없어
  `Mixin target ... DynamicStackHandler could not be found`로 컴파일이 실패했다.
- `compileOnly`를 Curios 전체 구현 JAR로 변경했다. `runtimeOnly`는 기존 그대로 유지한다.
- 삭제광선/절대 아이템 무결성의 런타임 동작 코드는 변경하지 않았다.

## BuildFix3 - 런타임 LivingEntityMixin 수정
- `LivingEntity#setItemSlot` 추상 메서드에 HEAD 주입하던 훅을 제거했습니다.
- 같은 보호를 `Player#setItemSlot`에 적용하는 `PlayerMixin`으로 이동했습니다.
- 증상: PREINJECT 단계 `InsnList.indexOf(null)` NPE / `glitchedcrown.mixins.json:LivingEntityMixin` 적용 실패.

## 1.0.7 - Dragonfyre 개선

- Tombstone 단일 ID 제한 제거. `minecraft:mending`, `minecraft:unbreaking`, Soulbound/Soul Binding 계열 registry/description ID/인챈트 태그 허용. 허용된 마법부여 책과의 모루 조합도 지원.
- 최대 내구도 16,777,216. `ItemStack#isDamageableItem`에서 Glitched Crown은 false를 반환하여 일반 내구도 소모/파괴 불가.
- 삭제광선에서 `die(source)` 제거. `hurt(maxHealth) -> setHealth(0) -> kill()`만 수행하며 `discard()`는 계속 사용하지 않음.
- 삭제광선이 `ShulkerBullet`을 직접 판정해 `kill()` 처리.
- `ShulkerBulletMixin`에서 망각의 오라 사용자 반경 256블록 안의 ShulkerBullet을 틱 시작 즉시 제거하여 타깃 추적 자체가 성립하지 않게 함.
- 망각의 오라에 서버/클라이언트 `noPhysics` 기반 위상 이동과 클라이언트 블록 모델 비렌더링 + `canOcclude=false` 기반 장막 투시 추가.
- `glitchedcrown:forbidden_domain` 도전과제 복원. 서버 공개 메시지는 수신자별 왕관 소유 여부를 확인해 소유자는 `[금단의 영역]`, 미소유자는 난독화 문자열로 전송. 도전과제 GUI/토스트도 클라이언트 Mixin으로 같은 규칙을 적용.
- K키 `절멸` 추가. 75블록 구형 범위의 비플레이어 처리 대상에게 삭제광선 사망 시퀀스를 실행하고, 생존 시에만 `discard()` fallback. 금빛 Dust 구형 파동을 20틱 동안 75블록까지 확장.


## 1.0.7.1 런타임 등록 크래시 수정
- `Item.Properties.durability()` 뒤의 중복 `.stacksTo(1)` 제거. 1.20.1에서는 durability()가 이미 스택 크기를 1로 설정하며, 이후 stacksTo 호출은 `Unable to have damage AND stack.` 예외를 발생시킴.

## 1.0.8 - 이동 방어 / 절멸 파동 / 오라 롤백

- 절멸 시각 효과를 Dust 선형 샘플에서 실제 삼각형으로 채운 반투명 금빛 구면 렌더러로 교체. 지속시간은 60틱(3초), 서버 즉사 판정도 구면 전선 도달 시점과 동기화.
- 1.0.7에서 추가했던 Aura `noPhysics`/블록 통과를 제거. 블록 모델 비렌더링과 `canOcclude=false`만 유지.
- 왕관 착용자 및 떨어진 왕관에 폭발 전용 방어 추가.
- ENTITY_GRAVITY attribute를 변경하는 효과를 `addEffect`/`forceAddEffect`에서 거부하고 기존 효과도 정리.
- Mutant Monsters / L2 Hostility / Cataclysm 직접 속도 변경 호출 차단. 공격 반사/카운터가 시전자 속도를 변경하는 경우 삭제 광선·절멸 처리 전후의 속도를 복구.
- 운명적 파멸에 Protection Shred +100% (`prot_shred` +1.0) 추가.
- [금단의 영역] 설명도 왕관 보유자에게만 해독. 한국어 설명: `파멸의 힘을 얻고 이 세상을 망가뜨리세요`.


## 1.1.4 hotfix
- 투구 인챈트 허용 판정의 무한 재귀(StackOverflowError) 수정.
- Crown 자신을 다시 검사하지 않고 바닐라 투구 프록시로 호환성을 판정.
- 모루/인챈트 책 경로도 동일한 비재귀 판정으로 변경.
