# 아이템70 - 복구할 수 있는 상황에는 검사 예외를, 프로그래밍 오류에는 런타임 예외를 사용하라

## 시작은 검증을 위한 예외 처리

체스 미션을 진행하며 다양하고 복잡한 요구사항을 만족하기 위해서 검증을 진행하였다. 검증에 실패하게 되면 예외 상황을 메시지로 명시하고 호출한 쪽으로 `예외를 전달`하였다.

```java
public class LinearMovingStrategy implements MovingStrategy {

    private final List<Direction> directions;
    ...
    @Override
    public void validateMove(List<List<Piece>> board, Position sourcePosition, Position targetPosition) {
        Direction direction = Direction.of(sourcePosition, targetPosition);
        validateDirection(direction);
        ...
    }
    ...
    private void validateDirection(Direction direction) {
        if (!directions.contains(direction)) {
            throw new IllegalArgumentException("해당 기물이 갈 수 없는 경로입니다.");
        }
    }
    ...
}
```

예외에 대한 처리를 진행하던 중 수업 중 언급된 checked exception과 unchecked exception 등 예외에 대한 키워드들이 떠올랐고 각자 어떠한 상황에서 사용되길 권장하는지 궁금해졌다. 또한 현재 사용하고 있는 예외 처리들이 적절한 상황이 맞는지 확인해야 했다.

## 문제 상황을 알리는 타입

Java에서는 문제 상황을 알리는 타입(throwable)으로 `error`, `checked exception`, `unchecked exception(runtime exception)`로 세 가지를 제공하고 있다. 

![](https://user-images.githubusercontent.com/59357153/144165617-d389bb7c-0a11-47dc-b24a-5aab326a5982.png)

## Error

`Error`는 시스템 혹은 하드웨어의 오작동으로 인해 발생한다. Error의 종류로는 대표적으로 `OutOfMemoryError`, `StackOverFlowError` 등으로 하드웨어 등의 기반 시스템의 문제로 발생한다. 이러한 수준의 Error는 심각한 수준의 오류이기 때문에 개발자는 이것을 미리 예측하여 방지할 수 없다.

## Exception

`Exception`은 개발자가 구현한 로직에서 발생한 실수나 사용자의 영향으로 발생한다. `Exception`이 발생하면 프로그램이 종료되는 것은 동일하지만 적절한 `예외 처리(Exception Handling)`을 통해 종료되지 않고 작동을 유지하도록 만들어줄 수 있다. Java에서는 이러한 예외 처리를 `try-catch`를 통해 제공된다.

`Exception`은 크게 `Checked Exception`과 `Unchecked Exception`으로 나눠진다.

### Checked Exception

 * 반드시 처리해야 하는 예외이다.
 * `컴파일 단계`에서 확인이 가능하다.
 * `RuntimeException` 및 `RuntimeException의 하위 클래스`들을 제외한 나머지 예외가 이예 해당한다.
 * `IOException` 등이 `Checked Exception`에 해당한다.

![](https://user-images.githubusercontent.com/59357153/160269014-cfca3ed3-792c-49d5-b583-c52662be7149.png)

> ### Unhandled exception
>
> 처리 되지 않은 예외 타입인 경우 발생한다. 반드시 예외를 처리해야 한다.

적절히 예외 처리를 진행해야 컴파일 에러를 해결할 수 있다.

![](https://user-images.githubusercontent.com/59357153/160269164-4f68225c-cf30-4030-8d97-d6ab524799eb.png)

### Unchecked Exception (Runtime Exception)

 * 예외 처리를 하지 않아도 된다.
 * `런타임 단계`에서 확인이 가능하다.
 * `RuntimeException`의 하위 클래스들이 이에 해당한다.
 * 대표적으로 `IllegalArgumentException`, `NullPointerException` 등 이 이에 해당한다.

![](https://user-images.githubusercontent.com/59357153/160269442-9217a556-4885-4f0a-b6ec-58f3c09efc63.png)

### Checked Exception이 적절한 상황

**호출한 쪽에서 복구하리라 여겨지는 상황이라면 Checked Exception을 사용한다.** 이것이 `Checked Exception`과 `Unchecked Exception`을 구분하는 가장 기본적인 규칙으로 활용된다. `Checked Exception`는 기본적으로 호출한 쪽에서 해당 예외를 catch하거나 더 바깥으로 예외를 전파하여 반드시 해결해야 한다. 

하지만 일반적으로 `Checked Exception` 예외가 발생했을 경우 복구 전략을 활용하여 복구할 수 있는 경우는 많지 않다. 만약 유니크해야 하는 `이메일 값이 중복`되어 `SQLException`이 발생했다고 가정한다. `유저가 입력 했던 이메일 + 난수`를 입력하여 유니크한 상태를 만들고 insert하면 처리가 가능하지만 이것은 적절한 복구 방법이 아니다. 결국 `catch` 이후 `Unchecked Exception`을 발생 시켜 예외를 다시 전파하는 것이 현실적인 해결 방법이다. 

결국 사용자에게 예외 상황을 회복하거나 추가적인 예외를 던져 전파할 것을 강제한다. 사용자는 예외를 catch하고 아무 행위를 하지 않아도 괜찮지만 권장하지 않는다. 자세한 내용은 `아이템 77: 예외를 무시하지 말라`에 언급되어 있다. 정리하면 `Checked Exception의 사용`은 결국 고민해야할 사항만 늘어나게 된다. 

## Unchecked Exception이 적절한 상황

**프로그래밍 오류를 나타낼 때는 Unchecked Exception을 사용해야 한다.** `Unchecked Exception`은 대부분 전제조건을 만족하지 못했을 때 발생한다. 

> ### 전제조건 위배
> 
> 사용자가 해당 API의 명세에 기록된 제약을 지키지 못했다는 의미이다. 한 예시로 `ArrayIndexOutOfBoundsException`의 경우 잘못된 배열의 인덱스에 접근하는 경우 발생하게 된다.

하지만 이러한 `Unchecked Exception`은 복구할 수 있는 상황인지 프로그래밍 오류로 인한 상황인지 명확하게 구별할 수 없다. 보통 이것은 API의 설계자의 판단이지만 확신하기 어렵다면 `Unchecked Exception`을 사용하는 편이 낫다. 자세한 내용은 `아이템 71: 필요 없는 검사 예외 사용은 피하라` 언급되어 있다.

## Error와 Exception을 대하는 적절한 자세

`Error`는 보통 JVM의 자원이 부족하거나 불변식이 깨지는 등 더 이상 수행을 지속할 수 없을 때 사용한다. Java 언어 명세에 된 것은 아니지만 업계에 널리 퍼진 규약으로 `Error` 클래스를 상속해 하위 클래스를 만드는 일은 자제해야 한다. 정리하면 개발자가 구현하는 Unchecked throwable은 모두 `RuntimeException의 하위 클래스`여야 한다. `Error`는 `상속`하지 말아야 하고 `throw`로 직접 던지지도 말아야 한다.

`Exception`, `RuntimeException`, `Error`를 상속하지 않는 throwable을 만드는 것도 가능하다. Java 언어 명세에서 공식적으로 다루는 것은 아니지만 암묵적으로 일반적인 `Checked Exception`처럼 다룬다. 하지만 책에서는 이러한 throwable을 사용하지 말 것을 권장한다. 오히려 API 사용자에게 혼란을 야기할 수 있다.

## 예외도 객체다

예외도 어떠한 메서드를 정의할 수 있는 객체이다. 예외의 메서드는 주로 해당 메서드를 일으킨 상황에 관한 정보를 코드 형태로 전달할 때 사용한다. 특히 `Checked Exception`은 예외 상황에서 벗어나는데 필요한 정보를 알려주는 메서드를 함께 제공할 수 있다. 자세한 정보는 호호의 [아이템 75 : 예외는 상세 메시지에 실패 관련 정보를 담으라](https://github.com/woowacourse-study/2022-effective-java/blob/main/10%EC%9E%A5/%EC%95%84%EC%9D%B4%ED%85%9C_75/%EC%98%88%EC%99%B8%EB%8A%94_%EC%83%81%EC%84%B8_%EB%A9%94%EC%8B%9C%EC%A7%80%EC%97%90_%EC%8B%A4%ED%8C%A8_%EA%B4%80%EB%A0%A8_%EC%A0%95%EB%B3%B4%EB%A5%BC_%EB%8B%B4%EC%9C%BC%EB%9D%BC.md)에서 확인할 수 있다.

## 정리

복구할 수 있는 상황이면 `Checked Exception`, 프로그래밍 오류라면 `Unchecked Exception`을 던지는 것이 바람직하다. 만약 확신이 없다면 `Unchecked Exception`을 던진다. `Checked Exception`은 결국 예외 처리를 위한 고민만 늘어날 뿐이다. 

또한 새로운 throwable을 정의하지 말고 Java에서 제공하는 `Checked Exception`, `Unchecked Execption`을 적절히 활용한다. 만약` Checked Exception`을 활용할 경우 `복구에 필요한 정보`를 알려주는 메서드도 함께 제공하는 것이 바람직하다.

## References

[Checked Exception을 대하는 자세](https://cheese10yun.github.io/checked-exception/)
[아이템 75 : 예외는 상세 메시지에 실패 관련 정보를 담으라](https://github.com/woowacourse-study/2022-effective-java/blob/main/10%EC%9E%A5/%EC%95%84%EC%9D%B4%ED%85%9C_75/%EC%98%88%EC%99%B8%EB%8A%94_%EC%83%81%EC%84%B8_%EB%A9%94%EC%8B%9C%EC%A7%80%EC%97%90_%EC%8B%A4%ED%8C%A8_%EA%B4%80%EB%A0%A8_%EC%A0%95%EB%B3%B4%EB%A5%BC_%EB%8B%B4%EC%9C%BC%EB%9D%BC.md)
[[Java Study 9주차] 예외 처리](https://wisdom-and-record.tistory.com/46)