# 아이템60 - 정확한 답이 필요하다면 float와 double은 피하라

## 시작은 리뷰어의 BigDecimal 언급

미션을 진행하던 중 돈과 관련된 로직에서 `BigDecimal`에 대한 키워드를 얻을 수 있었다.

![](https://user-images.githubusercontent.com/59357153/156870030-14429bfa-5484-41ad-98f7-80ba46fb4f47.png)

Java에서는 다양한 기본 자료형을 제공한다. 정수를 나타내는 `int`, `long`과 실수를 나타내는 `float`, `double`이 있는데 이것을 그대로 사용해도 문제가 없다고 생각한다.

하지만 이펙티브 자바의 아이템 60을 읽고 금융 관련 계산에서 float와 double의 사용은 얼마나 안일한 생각인지 알게 되었다.

## Java의 double과 float

Java에는 실수 표현을 위해 기본적으로 제공되는 자료형이 있다. 

### float

 * `4 Byte`
 * `저장 가능 범위`: <img src="http://latex.codecogs.com/svg.latex?-1.4&space;*&space;10^{-45}&space;~&space;3.4&space;*&space;10^{38}" title="http://latex.codecogs.com/svg.latex?-1.4 * 10^{-45} ~ 3.4 * 10^{38}" />

### double

 * `8 byte`
 * `저장 가능 범위`: <img src="http://latex.codecogs.com/svg.latex?-4.9&space;*&space;10^{-324}&space;~&space;1.7&space;*&space;10^{308}" title="http://latex.codecogs.com/svg.latex?-4.9 * 10^{-324} ~ 1.7 * 10^{308}" />

이러한 float와 double은 기본적으로 `부동 소수점 방식`을 활용한다.

## 부동 소수점 방식

Java의 실수 표현을 위한 `double`과 `float`의 경우 `IEEE 754 부동 소수점 방식`을 사용하고 있다. 이러한 부동 소수점 방식은 표현의 범위는 넓지만 `약간의 오차`를 가지고 있다.

#### float
<CenterImage image-src=https://user-images.githubusercontent.com/59357153/156870624-2c8b9fe8-f6d6-4d9f-baeb-675071db9a8f.png />

#### double
<CenterImage image-src=https://user-images.githubusercontent.com/59357153/156870629-a3573a9a-c32a-4aef-8f19-eb4f3b0f1bcc.png />

### 약간의 오차?

이러한 부동 소수점 방식을 활용한 실수 표현은 오차를 동반한다. 실수를 표현할 때 정확한 표현이 아닌 `근사치`를 표현하고 있다.

아래는 오차를 확인하기 위한 간단한 예시이다.

작성 날짜를 기준으로 비트코인 `1 BTC`는 한화 약 `47,000,000 KRW`로 거래되고 있다. 이러한 비트코인은 1개 보다 작은 단위로 나눠서 거래가 가능하다.

필자에게 1.015 BTC가 있다고 가정한다. 특정한 상품의 가격이 0.13 BTC라고 가정한 뒤 구매하기 위해 기존 값에서 계산을 진행한다.

```java
public class FloatingPointTest {

    @Test
    void floatingPointTest() {
        // given
        double coin = 1.015;

        // when
        double result = coin - 0.13;

        // then
        assertThat(result).isEqualTo(0.885);
    }
}
```

<CenterImage image-src=https://user-images.githubusercontent.com/59357153/156874885-afd15770-0b61-4bc2-b179-527d1623ee2f.png />

위 테스트는 실패한다. 이러한 방식은 정확한 계산을 요구하는 금융 관련 계산과는 맞지 않다.

## 올바른 방안

위 같은 문제를 올바르게 해결하기 위해서는 고정 소수점 방식을 활용한 `BigDecimal`이나 `int`, `long`을 사용해야한다.

## BigDecimal

Java에는 정수를 이용하여 실수를 표현하기 위한 `BigDecimal`이 제공된다. 

아래는 `BigDecimal`의 실제 내부 구현 중 일부분 이다.

```java
public class BigDecimal extends Number implements Comparable<BigDecimal> {
    ...
    private final BigInteger intVal;
    private final int scale; 
    private transient int precision;
    ...
}
```

 * `private final BigInteger intVal`: 정수를 저장하는데 사용한다.
 * `private final int scale`: 총 소수점 자리수를 가리킨다.
 * `private transient int precision`: 정밀도이다. 수가 시작하는 위치부터 끝나는 위치까지 총 자리수이다.

위 같은 내부 변수를 활용하여 실수를 정확하게 표현한다. 아래는 앞서 실패한 테스트를 `BigDecimal`로 변경하였다.

```java
public class BigDecimalTest {

    @Test
    void bigDecimalTest() {
        // given
        BigDecimal coin = new BigDecimal("1.015");

        // when
        BigDecimal result = coin.subtract(new BigDecimal("0.13"));

        // then
        assertThat(result).isEqualTo(new BigDecimal("0.885"));
    }
}
```

위 테스트는 정확하게 일치하여 성공한다.

### 생성 관련

BigDecimal을 생성하기 위해서는 다양한 방법이 존재한다. 

```java
BigDecimal bigDecimal1 = new BigDecimal("1.015");
BigDecimal bigDecimal2 = new BigDecimal(1.015);
BigDecimal bigDecimal3 = BigDecimal.valueOf(1.015);
```

한 가지 주의해야 할 점은 문자열이 아닌 double 타입을 그대로 전달할 경우 앞서 언급한 근사치가 전달되기 때문에 오차가 생길 가능성이 있다.

인텔리제이에서도 문자열로 변경하는 것을 권장하고 있다.

![](https://user-images.githubusercontent.com/59357153/156875606-57cfaf8e-236f-4ea7-acd3-5c529bf1dd84.png)

### 비교하기

BigDecimal은 `Comparable`의 구현체이기 때문에 객체간의 비교가 가능하다. 

Comparable과 관련된 아이템은 [아이템 14. Comparable을 구현할지 고려하라. (오찌)](https://github.com/woowacourse-study/2022-effective-java/blob/main/03%EC%9E%A5/%EC%95%84%EC%9D%B4%ED%85%9C_14/Comparable%EC%9D%84_%EA%B5%AC%ED%98%84%ED%95%A0%EC%A7%80_%EA%B3%A0%EB%A0%A4%ED%95%98%EB%9D%BC.pdf)에서 자세히 살펴볼 수 있다.

BigDecimal은 대부분의 규약을 지키고 있지만 권고 사항인 네 번째 규약을 지키지 않아 아래와 같은 일이 발생한다.

> ### 구현 시 필요한 규약 중 일부
> #### 네 번째 규약 (권고)
>
> * (x.compareTo(y) == 0) == (x.equals(y))여야 한다.
>    * 즉, compareTo로 수행한 동치성 테스트의 결과가 equals의 결과와 같아야 한다.
>    * 이 규약을 잘 지키면 compareTo로 줄지은 순서와 equals의 결과가 일관된다.
>    * 지키지 않는 클래스는 그 사실을 명시해야 한다.

### compareTo를 활용하는 TreeSet

```java
@Test
void duplicateBigDecimalTreeSetTest() {
    // given
    Set<BigDecimal> bigDecimals = new TreeSet<>(Set.of(new BigDecimal("2.00"), new BigDecimal("2.0")));

    // when
    int size = bigDecimals.size();

    // then
    assertThat(size).isEqualTo(1);
}
```

`new BigDecimal("2.00")`, `new BigDecimal("2.0")`를 동일한 객체로 판단하여 집합의 크기는 `1`이 된다.

### equals와 hashCode를 활용한 HashSet

```java
@Test
void duplicateBigDecimalHashSetTest() {
    // given
    Set<BigDecimal> bigDecimals = new HashSet<>(Set.of(new BigDecimal("2.00"), new BigDecimal("2.0")))

    // when
    int size = bigDecimals.size();

    // then
    assertThat(size).isEqualTo(2);
}
```

equals와 hashCode를 사용하는 HashSet의 경우 `new BigDecimal("2.00")`, `new BigDecimal("2.0")`를 서로 다르다고 판단하기 때문에 집합의 크기는 `2`가 된다.

## 제공되는 연산

BigDecimal에는 아래와 같은 기본적인 연산이 제공된다.

```java
public class BigDecimal extends Number implements Comparable<BigDecimal> {
    ...
    public BigDecimal add(BigDecimal augend) { ... }
    ...
    public BigDecimal subtract(BigDecimal subtrahend) { ... }
    ...
    public BigDecimal multiply(BigDecimal multiplicand) { ... }
    ...
    public BigDecimal divide(BigDecimal divisor) {...}
    ...
    public BigDecimal remainder(BigDecimal divisor) { ... }
}
```

아래는 연산에 대한 간단한 학습 테스트를 작성하였다.

```java
@Test
void calculateBigDecimalTest() {
    // given
    BigDecimal bigDecimal1 = new BigDecimal("10.0");
    BigDecimal bigDecimal2 = new BigDecimal("5.0");

    // when
    BigDecimal add = bigDecimal1.add(bigDecimal2);
    BigDecimal subtract = bigDecimal1.subtract(bigDecimal2);
    BigDecimal multiply = bigDecimal1.multiply(bigDecimal2);
    BigDecimal divide = bigDecimal1.divide(bigDecimal2);
    BigDecimal remainder = bigDecimal1.remainder(bigDecimal2);

    // given
    assertAll(
        () -> assertThat(add).isEqualTo(new BigDecimal("15.0")),
        () -> assertThat(subtract).isEqualTo(new BigDecimal("5.0")),
        () -> assertThat(multiply).isEqualTo(new BigDecimal("50.00")),
        () -> assertThat(divide).isEqualTo(new BigDecimal("2")),
        () -> assertThat(remainder).isEqualTo(new BigDecimal("0.0"))
    );
}
```

### 단점

하지만 이러한 BigDecimal도 단점은 존재한다.

 * 기본 타입보다 쓰기 `불편`하다.
 * 기본 타입보다 `느리다.`

## int 혹은 long

BigDecimal의 대안으로 `int` 혹은 `long` 타입을 사용할 수 있다. 하지만 다룰 수 있는 값의 크기가 제한되며 소수점이 필요한 경우 따로 관리해야 한다. 

### int

 * `4 byte`
 * `저장 가능 범위`: –2,147,483,648 ~ 2,147,483,647

만약 다뤄야 하는 숫자가 `9자리 이하`이면 int의 사용을 고려할 수 있다.

### long

 * `8 byte`
 * `저장 가능 범위`: -9,223,372,036,854,775,808 ~ 9,223,372,036,854,775,807

만약 다뤄야 하는 숫자가 `18자리 이하`이면 long의 사용을 고려할 수 있다.

## 정리

정확한 답이 필요한 경우 float나 double을 피해야 한다. 소수점 관리 및 성능 저하를 신경 쓰지 않는 다면 BigDecimal의 사용은 좋은 대안이 된다. 

하지만 성능이 중요하고 숫자가 너무 크지 않으며 소수점을 직접 관리할 자신이 있다면 int나 long 사용을 고려할 수 있다.

## References

조슈아 블로크 지음, 개앞맴시(이복연) 옮김, 『이펙티브 자바』, 프로그래밍 인사이트(2020), p355-357.
[아이템 14. Comparable을 구현할지 고려하라. (오찌)](https://github.com/woowacourse-study/2022-effective-java/blob/main/03%EC%9E%A5/%EC%95%84%EC%9D%B4%ED%85%9C_14/Comparable%EC%9D%84_%EA%B5%AC%ED%98%84%ED%95%A0%EC%A7%80_%EA%B3%A0%EB%A0%A4%ED%95%98%EB%9D%BC.pdf)
[[Java] BigDecimal에 관한 고찰 🕵️‍♀️](https://velog.io/@new_wisdom/Java-BigDecimal%EA%B3%BC-%ED%95%A8%EA%BB%98%ED%95%98%EB%8A%94-%EC%95%84%EB%A7%88%EC%B0%8C%EC%9D%98-%EB%84%88%EB%93%9C%EC%A7%93)