## 인스턴스화를 막으려거든 private 생성자를 사용하라

> Effective Java / Chapter2 / item4  
> 작성자: 연로그

기본적으로 정적 메소드, 정적 필드만 담은 클래스는 때때로 유용하게 쓰일 수 있다.  
사용하는 경우가 크게 세 가지가 있는데 예제를 통해 살펴보자.

### 1. 기본 타입 값이나 배열 관련 메소드를 모은 클래스

ex: `java.lang.Math`, `java.util.Arrays`

`java.util.Arrays`의 코드를 살펴보면 아래와 같다.

```java
public class Arrays {
    public static boolean isArray(Object o) { ...}

    public static Object[] asObjectArray(Object array) { ...}

    public static List<Object> asList(Object array) { ...}

    public static <T> boolean isNullOrEmpty(T[] array) { ...}

    // ...

    private Arrays() {
    }
}
```

우리가 Arrays를 사용할 때를 생각해보면 절대 `Arrays arrays = new Arrays()`로 생성하지 않는다.  
`Arrays.asList(배열)` 식으로 바로 사용한다.  
내부 메소드를 보니 전부 static으로 선언되어 있고 생성자가 private로 선언된 것을 확인할 수 있다.

### 2. 특정 인터페이스를 구현하는 객체를 생성해주는 정적 메소드(or 팩토리)를 모은 클래스

ex: `java.util.Collections`

`java.util.Arrays`와 마찬가지로 static으로만 구성, private 생성자 존재하는 것을 확인할 수 있다.

### 3. final 클래스와 관련된 메소드들을 모아놓을 때

final 클래스는 보안 상의 이유로 상속을 금지 시킨다.  
(final 클래스를 상속해 하위 클래스를 만드는 것이 시스템의 파괴를 야기할 수 있음)

대표적인 예로는 String 클래스가 있다.

```java
public final class String implements java.io.Serializable, Comparable<String>, CharSequence { ... }
```

이러한 final 클래스와 관련된 메소드들을 모아둔 클래스가 존재한다면?    
예제용으로 junit에 존재하는 StringUtils를 살펴보자.

```java
@API(status = INTERNAL, since = "1.0")
public final class StringUtils {
    private static final Pattern ISO_CONTROL_PATTERN = compileIsoControlPattern();
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    static Pattern compileIsoControlPattern() { ... }

    private StringUtils() {
        /* no-op */
    }

    public static boolean isBlank(String str) { ... }

    public static boolean isNotBlank(String str) { ... }

    public static boolean containsWhitespace(String str) { ... }

    // ...
}
```

마찬가지로 static으로 선언되어 바로 호출할 수 있다.

## 👉
정적 메소드를 담은 클래스들은 보통 **유틸리티 클래스**로 사용하기 위해 생성된다.  
유틸리티 클래스는 기본적으로 **인스턴스로 만들어 쓰려고 설계한 것이 아니다**.

크게 세 가지로 구분해두었지만 모든 메소드가 static으로 이루어진 유틸리티 클래스에서 사용하면 된다.

### ❓ 생성자를 명시하지 않으면 되지 않을까?

👉 생성자를 명시하지 않으면 **컴파일러가 자동으로 public으로 기본 생성자가 생성**해준다.  
사용자가 코드만 보고 생성자가 없다고 생각하더라도 컴파일 시 자동으로 생성된다.

### ❓ 추상 클래스로 만들어서 인스턴스화를 막기?
abstract 클래스는 인스턴스로 생성하는 것이 불가능하다.  
private 생성자 대신에 쓸 수 있지 않을까? 라는 생각이 들 수 있다.

👉 하위 클래스를 생성하면 인스턴스화가 가능해진다.  
👉 abstract 클래스는 보통 클래스들의 공통 필드와 메소드를 정의하는 목적으로 만들기 때문에 상속해서 사용하라는 의미로 오해할 수 있다.

***
참고

1. Effective Java / 조슈아 블로크 / 26p ~ 27p
2. https://www.tutorialspoint.com/why-string-class-is-immutable-or-final-in-java
