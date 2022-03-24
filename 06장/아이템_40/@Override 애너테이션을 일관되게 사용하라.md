# 아이템40 - @Override 애너테이션을 일관되게 사용하라

## Annotation

애너테이션의 사전적 의미로 주석을 나타낸다. 이러한 애너테이션은 컴파일 과정에서 코드를 어떻게 컴파일 할 것인지, 실행 과정에서 코드를 어떻게 처리할 것인지 알려주는 메타 데이터로써의 역할을 한다. 

> ### 메타 데이터
> 
> 데이터를 위한 설명을 의미하는 데이터

Java에서는 주로 컴파일러에게 코드 문법 에러를 검사하도록 정보를 제공하거나 코드를 자동으로 생성할 수 있도록 정보를 제공한다. 또한 런타임 시에 특정 기능을 실행하도록 할 수 있다. 

사용 방식은 `@ + 애너테이션 명`으로 특정 클래스나 메서드, 변수에 붙여 사용이 가능하다.

```java
@Override
```

이러한 애너테이션은 주석과 비슷한 역할을 담당한다. 해당 클래스나 행위 등에 표식을 남겨둔 것이다. 애너테이션 자체가 특정 기능을 가지는 것은 아니다. 또한 동적인 값이 들어갈 수 없기 때문에 `정적인 값`, `컴파일러 수준에서 해석이 가능한 값`들로만 엘리먼트로 작성이 가능하다.

### 빌트인 애너테이션

Java에는 기본적으로 제공되는 애너테이션이 존재한다. 해당 애너테이션은 컴파일러에게 코드 문법 에러를 검사하도록 정보를 제공한다.

 * `@Override`
 * `@Deprecated`
 * `@SupperssWarning`
 * `@SafeVararags`
 * `@FunctionalInterface`

그 중 이번 아이템인 `@Override`에 찐하게 알아보려 한다.

## @Override

메서드 선언이 상위 유형의 메서드 선언을 재정의하기 위한 것임을 알려준다. 메서드에 해당 애너테이션이 달린 경우 컴파일러는 다음 조건 중 하나 이상이 충족되지 않는 한 오류 메시지를 생성해야 한다.

 * 메서드는 상위 유형에 선언된 메서드를 재정의하거나 구현한다.
 * 메서드에는 Object에 선언된 모든 공용 메서드의 시그니처가 재정의된다.

아래는 실제 Override의 구현 코드이다.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Override {
}
```

* `@Target(ElementType.METHOD)`: 애너테이션의 적용 범위이다. `ElementType.METHOD`는 메서드에만 적용이 가능하다.
* `Retention(RetentionPolicy.SOURCE)`: Java 컴파일러가 애너테이션을 다루는 방법을 기술한다. 즉 어느 시점 까지 영향을 미치는지 결정한다. `RetentionPolicy.SOURCE`의 경우 컴파일 전까지 유효하다. 컴파일 이후에는 사라진다. 그렇기 때문에 컴파일 이후 빌드된 바이트코드에서는 `@Override`존재를 확인할 수 없다.
* `@interface`: 애너테이션 타입 정의를 위해 작성한다.

### 메서드는 상위 유형에 선언된 메서드를 재정의하거나 구현한다.

아래는 이번 블랙잭 미션을 진행하며 상태 패턴 적용을 위해 공통 상태를 선언한 `State` 인터페이스이다.

```java
public interface State {

    State draw(Card card);

    State stay();

    boolean isRunning();

    boolean isFinished();

    Cards cards();

    double earningRate(State state);
}
```

인터페이스는 기본적으로 메서드 선언 시 `public abstract`가 생략되어 있기 때문에 상속하는 하위 클래스는 해당 메서드를 재정의하여 모두 구현해야 한다.

아래는 `State` 인터페이스의 구현체인 `Ready`이다.

```java
public final class Ready implements State {
    ...
    @Override
    public State draw(Card card) {
        cards.append(card);

        if (cards.isBlackjack()) {
            return new Blackjack(cards);
        }

        if (cards.isReady()) {
            return new Hit(cards);
        }

        return new Ready(cards);
    }

    @Override
    public State stay() {
        throw new IllegalStateException();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public Cards cards() {
        return cards;
    }

    @Override
    public double earningRate(State state) {
        throw new IllegalStateException();
    }
}
```

### 메서드에는 Object에 선언된 모든 공용 메서드의 시그니처가 재정의된다.

추상 메서드만 재정의할 수 있는 것은 아니다. 메서드의 구현을 포함하고 있는 메서드 또한 재정의가 가능하다.

가장 대표적인 예시는 `Object` 클래스에 작성된 `equals`, `toString` 메서드이다. 메서드의 구현부가 작성되어 있지만 하위 클래스들은 자유롭게 재정의하여 사용할 수 있다.

```java
public class Object {
    ...
    public boolean equals(Object obj) {
        return (this == obj);
    }
    ...
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }
    ...
}
```

```java
public class Crew {
    ...
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Crew crew = (Crew) o;
        return Objects.equals(name, crew.name) && course == crew.course;
    }
    ...
    @Override
    public String toString() {
        return "Crew{" +
                "name='" + name + '\'' +
                ", course=" + course +
                '}';
    }
}
```

## 메서드 재정의를 위한 조건

 * 상속 관계에서 발생한다.
 * 접근 제어자, 리턴 타입, 매개변수가 모두 일치해야 한다.
 * 접근 제어자는 확장은 가능하지만 축소는 불가능하다. 예를들어 상위 클래스에 접근 제어자가 `protected`인 메서드를 재정의할 경우 `protected`, `public`으로 가능하다.

간단히 Crew 클래스의 `equals` 메서드를 재정의한다고 가정한다. 의도적으로 실제 equals 메서드의 매개변수 리스트와 다르게 작성하였다.

```java
public class Crew {
    ...
    @Override
    public boolean equals(Crew o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Crew crew = (Crew) o;
        return Objects.equals(name, crew.name) && course == crew.course;
    }
    ...
}
```

매개 변수가 일치하지 않기 때문에 아래와 같은 컴파일 오류가 발생한다. 

![](https://user-images.githubusercontent.com/59357153/159148806-fa3a19f0-87f9-4009-9c2c-af155a9b706d.png)

> ### Method does not override method from its superclass
> 
> 메서드가 슈퍼클래스에서 메서드를 재정의하지 않는다.

컴파일러는 해당 메서드를 재정의한 메서드로 인식하지 않고 `오버로딩`한 것으로 인식한다.

## @Override가 없다면?

사실 `@Override`를 붙이지 않는다고 재정의가 불가능한 것은 아니다. 앞서 언급한 것 처럼 애너테이션은 단순히 컴파일러에게 알려주기 위한 주석일 뿐이고 자체적으로 특정 기능을 수행하는 것은 아니다.

```java
public class Crew {
    ...
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Crew crew = (Crew) o;
        return Objects.equals(name, crew.name) && course == crew.course;
    }
    ...
    public String toString() {
        return "Crew{" +
                "name='" + name + '\'' +
                ", course=" + course +
                '}';
    }
}
```

없어도 정상적으로 동작하지만 책에서는 @Override를 달 것을 권장한다. 컴파일 시점에 `재정의한 메서드가 잘못 작성되지 않았는지 확인`할 수 있을 뿐만 아니라 `개발자들에게 해당 메서드가 재정의 되었다는 것을 명시`적으로 알려준다.

## 정리

`@Override`는 컴파일 시점에 우리에게 `많은 정보를 제공`해준다. 해당 메서드가 재정의된 메서드임을 확인할 수 있고, 메서드를 잘못 작성한다면 친절하게 오류 메시지를 보여준다. 

그러니 `상위 클래스의 메서드를 재정의하려는 모든 메서드에 @Override 애너테이션을 명시`하자. 추가적으로 IDE를 사용하면 편리하게 상위 클래스의 추상 메서드들을 재정의할 때 자동적으로 명시하도록 도와준다.

## References

조슈아 블로크 지음, 개앞맴시(이복연) 옮김, 『이펙티브 자바』, 프로그래밍 인사이트(2020), p246 ~ 248.
[Annotation Type Override](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Override.html)