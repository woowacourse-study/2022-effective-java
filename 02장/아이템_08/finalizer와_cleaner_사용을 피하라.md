# 이펙티브 자바 아이템 8 : finalizer와 cleaner 사용을 피하라

결론! 위 두개를 사용해서 객체 사용이 끝나는 시점에 소멸시켜야지! 라고 사용하지 말자.

**GC의 대상은 되지만 바로 수거해가는게 아니다.**

<img width="688" alt="스크린샷 2022-03-31 오후 10 08 39" src="https://user-images.githubusercontent.com/58363663/161062068-419677f5-69be-412a-ac8d-2725b30d9fd1.png">


### finalizer와 cleaner를 유용하게 사용할 일은 극히 드물다.

- 안전망 역할로 자원을 반납하려 할 때 사용을 권한다.
- 네이티브 자원을 정리할 때 사용을 권한다.

### 즉시 수행된다는 보장이 없다.

`finalizer`와 `cleaner`는 **즉시 수행된다는 보장이 없다**. 언제 실행될 지 알 수 없으며 시간이 얼마나 걸릴지는 아무도 모른다. ***즉 원하는 시점에 실행하게 하는 작업은 절대 할 수 없다.***

1. 파일 리소스를 반납하는 작업을 처리한다면 그 파일 리소스가 언제 처리 될지 알 수 없다.
2. 반납이 되지 않아 새로운 파일을 열지 못 하는 상황이 발생할 수 있다.
3. 동시에 열 수 있는 파일 개수가 제한되어 있다.

### 얼마나 빠르게 실행될지 알 수 없다.

얼마나 신속히 수행될지는 가비지 컬렉터에게 기도해야 한다. 🙏

전적으로 가비지 컬렉터 알고리즘에 달려있으며 가바지 컬렉터마다 다르다.

현재 프로그래머가 테스트한 JVM에선 완벽하게 동작하여도 **고객의 시스템에선 재앙을 일으킬 수 있다.**

### 우선순위가 낮다.

불행히도 Finalizer 쓰레드는 **우선순위가 낮아서** 실행될 기회를 얻지 못 할 수도 있다. (언제 실행될지 모른다.) Finalizer안에 작업이 있고 그 작업을 쓰레드가 처리하지 못 하고 대기하다가, 해당 인스턴스가 GC되지 않고 쌓이다가 결국 `OutOfMemoryError`를 발생할 수 있다.

### 아예 실행되지 않을 수도 있다.

수행 여부조차 보장하지 않기 때문에 상태를 영구적으로 수정하는 작업에는 절대 `finalizer`와 `cleaner`에 의존해서는 안된다.

데이터베이스 같은 공유 자원의 영구 **락 해제**를 `finalizer`와 `cleaner`에게 맡겨 놓으면 분산 시스템 전체가 서서히 멈출 것이다.

```java
@Override
protected void finalize() throws Throwable {
// 락걸린거 이 객체가 소멸될 때 같이 락을 해제하면 되겠다! -> X
}
```

### 현혹되지 말자.

`System.gc` 나 `System.runFinalization` 메서드에 현혹되지 말자. `finalizer`와 `cleaner`가 실행될 가능성을 높여줄 순 있으나 보장하지 않는다.

이를 보장해주겠다는 메서드가 2개 등장했지만 심각한 결함때문에 몇년간 `deprecated` 상태다. → java11에선 삭제되었다.

<img width="769" alt="스크린샷 2022-03-31 오후 10 09 05" src="https://user-images.githubusercontent.com/58363663/161062147-80c287b9-3bc4-4697-8043-e4a9178c5caf.png">
11에 들어와서는 삭제되었다.

### 또 있다.

`finalizer` 동작 중 발생한 **예외는 무시**되며, 처리할 작업이 남아있더라도 **그 순간 종료**된다. 잡지 못한 예외 때문에 해당 객체는 자칫 마루리가 덜 된 상태로 남을 수 있다. 보통의 경우 잡지 못한 예외가 스레드를 중단시키고 스택 추적 내역을 출력한다. 하지만 같은 일이 finalizer에서 발생한다면 경고조차 출력하지 않는다.

그나마 cleaner를 사용하는 라이브러리는 자신의 쓰레드를 통제하기 때문에 이러한 문제가 발생하지 않는다.

### 성능에도 문제가 있다.

`AutoCloseable` 객체를 생성하고 `try-with-resources`로 자원을 닫아서 가비지컬렉터가 수거하기까지 12ns가 걸렸다면 `finalizer`를 사용한 객체를 생성하고 파괴하니 550ns가 걸렸다. (50배)

`finalizer`가 가비지 컬렉터의 효율을 떨어지게 한다. 하지만 잠시 후 알아볼 안전망 형태로만 사용하면 66ns가 걸린다. 안전망의 대가로 50배에서 5배로 성능차이를 낼 수 있다.

### 공격에 노출될 수 있다.

[자바에서 이런게 된다고? 네.. 가능합니다. 한번 보시죠!](https://www.youtube.com/watch?v=6kNzL1bl1kI)

생성이나 직렬화 과정에서 에외가 발생하면 생성되다 만 객체에서 악의적인 하위 클래스의 finalizer가 수행될 수 있게 된다.

```java
public class KakaoBank {

    private int money;

    public KakaoBank(final int money) {
        if (money < 1000) {
            throw new RuntimeException("1000원 이하로 생성이 불가능해요.");
        }
        this.money = money;
    }

    void transfer(final int money) {
        this.money -= money;
        System.out.println(MessageFormat.format("{0}원 입금 완료!!", money));
    }
}
```

```java
public class BankAttack extends KakaoBank {

    public BankAttack(final int money) {
        super(money);
    }

    @Override
    protected void finalize() throws Throwable {
        this.transfer(1000000000);
    }
}
```

```java
public class Main {
    public static void main(final String[] args) throws InterruptedException {
        KakaoBank bank = null;
        try {
            bank = new BankAttack(500);
            bank.transfer(1000);
        } catch (Exception e) {
            System.out.println("예외 터짐");
        }
        System.gc();
        sleep(3000);
    }
}
```
<img width="496" alt="스크린샷 2022-03-31 오후 10 09 46" src="https://user-images.githubusercontent.com/58363663/161062276-94bf2b6d-16bb-4479-8d04-ce8422a757c1.png">

```java
@Override
protected final void finalize() throws Throwable {
}
```

### 그럼 어떻게 사용해야 하나?

`**AutoCloseable**`을 구현하고 클라이언트에서 인스턴스를 다 쓰고나면 close 메서드를 호출하면 된다. t`ry-finally`나 `try-with-resourse`를 사용하여 자원을 종료실킬 수 있다.

```java
public class Sample implements AutoCloseable {
    @Override
    public void close() {
        System.out.println("close");
    }
}
```

### 그럼 언제 쓰나?

- 안전망 역할로 자원을 반납하려 할 때
- 네이티브 자원을 정리할 때

1. **자원의 소유자가 close 메서드를 호출하지 않는 것에 대비**

cleaner, finalizer가 즉시 호출될것이란 보장은 없지만, 클라이언트가 하지 않은 자원 회수를 늦게라도 해주는 것이 아예 안하는 것보다 낫다. 하지만 이런 안전망 역할로 finalizer를 작성할 때 그만한 값어치가 있는지 신중히 고려해야 한다. 자바에서는 안전망 역할의 `finalizer`를 제공한다. `FileInputStream`, `FileOutputStream`, `ThreadPoolExecutor`가 대표적이다.

1. **네이티브 자원 정리**

cleaner와 finalizer를 적절히 활용하는 두 번째 예는 네이티브 피어와 연결된 객체이다. 네이티브 피어란 일반 자바 객체가 네이티브 메서드를 통해 기능을 위임한 네이티브 객체를 말한다. 그 결과 자바 피어를 회수할 때 네이티브 객체까지 회수하지 못 한다. 성능 저하를 감당할 수 없거나 자원을 즉시 회수해야 한다면 close 메서드를 사용해야 한다.

### cleaner 사용하기

```java
public class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();

    // Room을 참조하지 말것!!! 순환 참조
    private static class State implements Runnable { 
        int numJunkPiles;

        State(int numJunkPiles) {
            this.numJunkPiles = numJunkPiles;
        }

        @Override
        public void run() {  // **colse가 호출되거나, GC가 Room을 수거해갈 때 run() 호출**
            System.out.println("Room Clean");
            numJunkPiles = 0;
        }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    public Room(int numJunkPiles) {
        state = new State(numJunkPiles);
        cleanable = cleaner.register(this, state);
    }

    @Override
    public void close() {
        cleanable.clean();
    }
}
```

State 인스턴스가 Room 인스턴스를 참조할 경우 순환참조가 발생하고 가비지 컬렉터가 Room을 회수해갈 기회가 오지 않는다. State가 static인 이유도 바깥 객체를 참조하지 않기 위해서이다.

위 코드는 안전망을 만들었을 뿐이다. 클라이언트가 try-with-resources 블록으로 감쌌다면 방 청소를 정상적으로 출력한다.

```java
public static void main(final String[] args) {
    try (Room myRoom = new Room(8)) {
        System.out.println("방 쓰레기 생성~~");
    }
}
```


하지만 아래와 같은 코드는 방 청소 완료라는 메시지를 항상 기대할 수는 없다.

```java
public static void main(final String[] args) {
    new Room(8);
    System.out.println("방 쓰레기 생성~~");
}
```
