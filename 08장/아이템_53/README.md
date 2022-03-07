# 아이템 53. 가변인수는 신중히 사용하라

## 가변인수
Java 5부터 등장한 가변인수는 동일한 타입의 여러 매개변수를 받는 메소드 대해 축약된 형태의 인자를 제공합니다.
가변인수가 등장한 덕분에 아래와 같은 코드를 개선할 수 있게 됐습니다.

* **개선 전**
```java
static int sum(int arg1) {
    //...
}
static int sum(int arg1, int arg2) {
    //...
}
static int sum(int arg1, int arg2, int arg3) {
    //...
}
```

* **개선 후**
```java
static int sum(int ... args) {
    //...
}
```

심지어 가변인수 개수의 제한 없이 마음껏 사용할 수 있어 코드의 중복도 줄이고, 일반적으로 사용되던 코드도 개선할 수 있게 됐습니다.

하지만 이 가변인수를 사용할 때는 굉장히 신중해야 합니다.

## 주의사항

가변인수를 사용하는 데 있어서 몇가지 규칙과 주의사항이 있습니다.

### 1. 메서드 당 한 개의 가변인수만 사용 가능하다.

아래와 같이 코드를 작성해봅시다.
```java
public class VarArgsTest {
    
    // 컴파일 에러가 발생합니다.
    public static void varArgError(int ... args, String ... args2) {
        
    }
}
```

아래와 같이 컴파일 에러가 발생합니다.  
![스크린샷 2022-03-06 오후 12 24 33](https://user-images.githubusercontent.com/87312401/156907882-66b2e5c2-8b1a-4d2e-a0a7-94022641003c.png)  

가변인수는 가장 마지막에 작성이 되어야 한다고 경고하네요. 
즉, 두 개 이상의 가변인수를 하나의 메서드에서 사용할 수 없고 아래 코드처럼 사용하는 것도 불가합니다.

```java
// 가변인수는 항상 맨 뒤에 위치해야 합니다.
public static void varArgError(int ... args, String args2) {
    
}
```

### 2. 힙 오염

아래 코드를 보시죠.

```java
public class VarArgsTest {

    public static <T> List<T> test(List<T>... variable) {
        Object[] objArr = variable;
        objArr[0] = Arrays.asList(1,2);

        return variable[0];
    }


    public static String firstOfFirst(List<String>... strings) {
        List<Integer> ints = Collections.singletonList(42);
        Object[] objects = strings;
        objects[0] = ints; // Heap pollution

        return strings[0].get(0); // ClassCastException
    }

    public static void main(String[] args) {
        System.out.println(test(Arrays.asList("a","b"))); // 1번 지점
        firstOfFirst(Arrays.asList("a","b"), Arrays.asList("a","c")); // 2번 지점
    }
}
```

들어온 가변인수는 제너릭을 사용하든, 사용하지 않든 **Object** 배열로 업캐스팅이 가능합니다.

**Object** 배열의 각 요소는 들어온 가변인수의 참조값과 같은 참조값을 갖게 되죠.
이 때 따른 제네릭 타입의 값을 넣어 참조값을 변경하게 됩니다. 바로 **힙 오염** 이 발생하는 것이죠.

이렇게 사용하게 된다면 아래와 같은 문제가 생깁니다.

> 1번 지점 : String 리스트를 기대했으나 정수형 리스트 [1, 2] 가 출력된다.  
> 2번 지점 : String을 기대했으나 반환값이 정수형이기 때문에 ClassCastException이 발생한다.

이처럼 들어온 가변인수에 대해 업캐스팅하여 잘못된 사용을 할 수 있으므로 힙 오염이 되지 않게 주의해야 합니다.

### 3. 인수가 들어오지 않는 경우

가변인수를 받아 최소값을 구하는 메서드를 작성해봅시다.

```java
public class VarArgsTest {

    public static int min(int ... args) {
        return Arrays.stream(args)
                .min()
                .orElseThrow(() -> new IllegalArgumentException("인자 없음"));
    }

    public static void main(String[] args) {
        min();
    }
}
```

놀랍게도 main에서 실행되는 코드는 컴파일 에러가 발생하지 않습니다.

가변인수는 런타임에 배열의 길이를 확인하기 때문에 컴파일 에러가 발생되지 않고 프로그램을 실행할 수 있는 것이죠.
덕분에 main 메드를 실행하면 아래와 같이 에러가 발생합니다.

![스크린샷 2022-03-06 오후 12 50 09](https://user-images.githubusercontent.com/87312401/156908438-56301330-31f1-4f6c-8e58-5902e9738ac5.png)  

그래서 가변인수를 필수적으로 1개 이상 받아야 하는 경우에는 아래와 같이 코드를 작성합니다.

```java
public static int min(int arg, int ... args) {
        return Arrays.stream(args)
        .min()
        .orElse(arg);
}
```

프리코스동안 제공된 **ApplicationTest** 내부도 뜯어보면 아래와 같이 코드가 작성된 것을 볼 수 있습니다.

```java
// 필수로 Integer value가 1개는 들어오게 세팅 되어 있다.
public static void assertRandomNumberInRangeTest(
    final Executable executable,
    final Integer value,
    final Integer... values
) {
    assertRandomTest(
        () -> Randoms.pickNumberInRange(anyInt(), anyInt()),
        executable,
        value,
        values
    );
}
```

### 4. 성능 이슈

가변인수 메서드는 호출될 때마다 새로운 배열을 만들어냅니다.
가변인수를 사용했지만 매개변수가 1, 2, 3개.. 등 적게 들어오는 경우가 대부분이라면 괜히 성능이 떨어지는 이슈가 발생하게 됩니다.

이 문제를 **Set**의 ```of()```메서드를 보고 해결해봅시다.

```java
static <E> Set<E> of(E e1) {
    return new ImmutableCollections.Set12<>(e1);
}

static <E> Set<E> of(E e1, E e2) {
    return new ImmutableCollections.Set12<>(e1, e2);
}
static <E> Set<E> of(E e1, E e2, E e3) {
    return new ImmutableCollections.SetN<>(e1, e2, e3);
}

        ...

static <E> Set<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        return new ImmutableCollections.SetN<>(e1, e2, e3, e4, e5,
        e6, e7, e8, e9, e10);
}

@SafeVarargs
@SuppressWarnings("varargs")
static <E> Set<E> of(E... elements) {
        switch (elements.length) { // implicit null check of elements
            case 0:
            return ImmutableCollections.emptySet();
            case 1:
            return new ImmutableCollections.Set12<>(elements[0]);
            case 2:
            return new ImmutableCollections.Set12<>(elements[0], elements[1]);
            default:
            return new ImmutableCollections.SetN<>(elements);
        }
}
```

10개 까지의 매개변수를 받는 경우에는 메서드를 다중 정의하여 성능 문제를 해결합니다.
일반적으로는 크게 이득이 되지 않겠지만 정말 메서드가 많이 사용되는 경우에는 큰 성능 개선을 가져올 수 있게 되겠죠.

## 정리

가변인수를 신중히 사용해야 하는 것은 맞지만 그렇다고 아예 사용하지 않을 수는 없습니다.  
인수 개수가 일정하지 않은 경우에는 위 주의사항을 잘 살피어 가변인수를 사용해 봅시다.