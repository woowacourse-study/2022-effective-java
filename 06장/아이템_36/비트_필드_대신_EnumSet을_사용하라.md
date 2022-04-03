# 아이템 36 : 비트 필드 대신 EnumSet을 사용하라

## 비트 필드 열거 상수 - 구닥다리 기법!

```JAVA
public class Text {
    public static final int STYLE_BOLD = 1 << 0; // 1
    public static final int STYLE_ITALIC = 1 << 1; // 2
    public static final int STYLE_UNDERLINE = 1 << 2; // 4
    public static final int STYLE_STRIKETHROUGH = 1 << 3; // 8

    // 매개변수 styles는 0개 이상의 STYLE_ 상수를 비트별 OR한 값이다.
    public void applyStyles(int styles) { ... }
}
```

＞ 아래와 같은 식으로 비트별 OR을 사용해 여러 상수를 하나의 집합으로 모을 수 있음

＞ 이렇게 만들어진 집합을 비트 필드(bit field)라 함

```JAVA
public static void main(String[] args) {
    Text text = new Text();
    text.applyStyles(STYLE_BOLD | STYLE_ITALIC);
}
```

### 👍 비트 필드의 장점

＞ 비트별 연산을 사용해 합집합과 교집합 같은 집합 연산을 효율적으로 수행할 수 있음

### 👎 비트 필드의 단점

＞ 정수 열거 상수의 단점을 그대로 지님

＞ 추가로, 비트 필드 값이 그대로 출력되면 단순한 정수 열거 상수를 출력할 때보다 해석하기가 훨씬 어려움

＞ 비트 필드 하나에 녹아 있는 모든 원소를 순회하기도 까다로움

＞ 최대 몇 비트가 필요한지를 API 작성 시 미리 예측하여 적절한 타입(보통은 int나 long)을 선택해야 함  
(API를 수정하지 않고는 비트 수를 더 늘릴 수 없기 때문)

## EnumSet - 비트 필드를 대처하는 현대적 기법

```JAVA
public class Text {
    public enum Style { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH}

    // 어떤 Set을 넘겨도 되나, EnumSet 이 가장 좋다.
    public void applyStyles(Set<Style> styles) { }
}
```

```JAVA
public static void main(String[] args) {
    Text text = new Text();
    text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));
}
```

### EnumSet 이 아닌 Set 을 받는 이유

＞ applyStyles 메서드가 EnumSet\<Style>이 아닌 Set\<Style>을 받은 것은 다형성 때문

    모든 클라이언트가 EnumSet을 건네리라 짐작되는 상황이라도 이왕이면 인터페이스로 받는 게 일반적으로 좋은 습관임
    이렇게 하면 특이한 클라이언트가 다른 Set 구현체를 넘기더라도 처리할 수 있음

### EnumSet 의 장점

＞ 열거 타입 상수의 값으로 구성된 집합을 효과적으로 표현해줌

＞ Set 인터페이스를 완벽히 구현하며, 타입 안전하고, 다른 어떤 Set 구현체와도 함께 사용할 수 있음

＞ removeAll과 retainAll 같은 대량 작업은 (비트 필드를 사용할 때 쓰는 것과 같은) 비트를 효율적으로 처리할 수 있는 산술 연산을 써서 구현함

＞ 난해한 작업들은 EnumSet이 다 처리해주기 때문에 비트를 직접 다룰 때 겪는 흔한 오류들로부터 해방됨

### EnumSet 의 단점

＞ 자바 11까지는 아직 불변 EnumSet을 만들 수 없음

＞ EnumSet 대신 Collections.unmodifiableSet으로 EnumSet을 감싸 사용할 수 있음  
(명확성과 성능이 조금 희생되긴 함)
