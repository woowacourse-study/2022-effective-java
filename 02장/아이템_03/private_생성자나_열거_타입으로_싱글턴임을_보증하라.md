# 아이템 3 : private 생성자나 열거 타입으로 싱글턴임을 보증하라

## 싱글턴이란?

싱글턴 : 인스턴스를 오직 하나만 생성할 수 있는 클래스  
[참고하세요 :)](https://github.com/pup-paw/TIL/blob/main/woowacourse/LV1/singleton.md)

## 싱글턴을 만드는 방식

### 1️⃣ public static final 필드 방식의 싱글턴

```JAVA
public class Yaho {
	public static final Yaho INSTANCE = new Yaho();
	private Yaho() { ... }
}
```

**`private 생성자`는 public static final 필드인 Yaho.INSTANCE 를 초기화할 때 단 한번만 호출됨**

    public 이나 protected 생성자가 없음
    ＞ Yaho 클래스가 초기화될 때 만들어진 인스턴스가 전체 시스템에서 하나뿐임이 보장됨

    클라이언트는 손 쓸 방법이 없음
    ＞ 예외 : 권한이 있는 클라이언트는 리플렉션 API(아이템 65)인
            AccessibleObject.setAccessible 을 이용해 private 생성자 호출 가능
    ＞ 방어 : 생성자를 수정하여 두 번째 객체가 생성되려 할 때 예외를 던지게 함

**장점**

    1. 해당 클래스가 싱글턴임이 API 에 명백히 드러남
    ＞ public static 필드가 final 이므로 절대로 다른 객체를 참조할 수 없음

    2. 간결함

**단점**

    1. 클라이언트에서 사용하지 않더라도 인스턴스가 항상 생성됨
    > 메모리가 낭비됨

### 2️⃣ 정적 팩터리 방식의 싱글턴

```JAVA
public class Yaho {
	private static final Yaho INSTANCE = new Yaho();
	private Yaho() { ... }
	public static Yaho getInstance() { return INSTANCE; }
}
```

**Yaho 인스턴스는 단 한번만 만들어짐**

    Yaho.getInstance는 항상 같은 객체의 참조를 반환함

    1️⃣ 과 같이, 리플렉션을 통한 예외는 적용됨

**장점**

    1. (마음이 바뀌면) API를 바꾸지 않고도 싱글턴이 아니게 변경할 수 있음
        ex) 유일한 인스턴스를 반환하던 팩터리 메서드가
            호출하는 스레드별로 다른 인스턴스를 넘겨주게 할 수 있음

    2. 원한다면 정적 팩터리를 제네릭 싱글턴 팩터리로 만들 수 있음 (아이템 30)

    3. 정적 팩터리의 메서드 참조를 공급자(supplier)로 사용할 수 있음 (아이템 43, 44)
        ex) Yaho::getInstance를 Supplier<Yaho>로 사용하는 식

이러한 장점들이 굳이 필요하지 않다면, 1️⃣ 방식이 좋음

**단점**

    여전히 사용하지 않더라도 인스턴스가 생성됨

### 1️⃣ 과 2️⃣ 의 직렬화 (12장)

**둘 중 하나의 방식으로 만든 싱글턴 클래스를 직렬화하려면 단순히 Serializable을 구현한다고 선언하는 것으로는 부족**

    모든 인스턴스 필드를 일시적(transient)이라고 선언하고 readResolve 메서드를 제공해야 함 (아이템 89)
    ＞ 이렇게 하지 않으면 직렬화된 인스턴스를 역직렬화할 때마다 새로운 인스턴스가 만들어짐
    ex) 2️⃣ 의 경우, 가짜 Yaho가 탄생한다는 뜻

    해결 : readResolve 메서드를 추가

```JAVA
//싱글턴임을 보장해주는 readResolve 메서드
private Object readResolve() {
	// '진짜' Yaho 를 반환하고, 가짜 Yaho 는 가비지 컬렉터에 맡김
	return INSTANCE;
}
```

### 3️⃣ 열거 타입 방식의 싱글턴

```JAVA
public enum Yaho {
	INSTANCE;
}
```

**싱글턴을 만드는 세가지 방법 중 가장 바람직한 방법**

    1. public 필드 방식과 비슷하지만 더 간결함

    2. 추가 노력 없이 직렬화할 수 있음
    ＞ 심지어 아주 복잡한 직렬화 상황이나 리프렉션 공격에서도 제 2의 인스턴스가 생기는 일을 완벽하게 막아줌

    대부분 상황에서는 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법임
    ＞ 단, 만들려는 싱글턴이 Enum 외의 클래스를 상속해야 한다면 이 방법은 사용할 수 없음
    ＞ 열거 타입이 다른 인터페이스를 구현하도록 선언할 수는 있음
