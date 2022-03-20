
## Item 31. 한정적 와일드카드를 사용해 API 유연성을 높여라

<br>

앞선 스터디에서 매개변수화 타입은 불공변이라는 설명을 한적이 있습니다. (Item 28. 배열보다는 리스트를 사용하라)

<br>

잠깐 정리하자면, 서로 다른 타입 `Type1`, `Type2`가 있을 때 `List<Type1>은 List<Type2>`의 하위 타입도 상위 타입도 아니라는 말입니다.

<br>

예를 들어보죠. `List<String>`은 `List<Object>`의 하위 타입이 아닙니다. 그 이유는 List<String>이 List<Object>가 하는 일을 재대로 수행하지 못하기 때문입니다. (리스코프 치환원칙을 위배)

<br>

이렇게 매개변수화 타입은 불공변이지만, 때론 유연하게 `API`를 설계해야 하는 상황이 있습니다. 대표적으로 `public API`를 설계할 때 필요한데, `Stack API`를 통해서 한 번 예시를 들어보겠습니다.

<br><br>

### 한정적 와일드카드를 사용해서 API의 유연성을 높여보자!!

<hr>

#### pushAll (producer - extends)

다음은 `Stack` 클래스에 `pushAll()` 이라는 일련의 원소를 스택에 넣는 메서드를 추가한 상태입니다.

```java
public class Stack<E> {
    private List<E> list = new ArrayList<>();
    
    public void pushAll(Iterable<E> src) {
        for(E e : src) {
            push(e);
        }
    }
    
    public void push(E e) {
        list.add(e);
    }
}
```

위의 `Stack` 클래스의 `pushAll()` 메서드는 결함이 존재합니다. `Iterable`의 원소 타입이 스택의 원소 타입과 일치하면 잘 작동하지만 타입이 다를 경우 매개변수화 타입이 불공변이기 때문에 컴파일 에러가 발생하게 됩니다.

```java
@DisplayNmae("Number Stack에 Integer를 삽입이 가능한지")
@Test
void pushAll() {
    Stack<Number> stack = new Stack<>();
    List<Integer> intList = Arrays.asList(1, 2, 3, 4);
    
    // Compile Error
    stack.pushAll(intList);
}
```

코드로 좀 더 자세히 살펴보면 논리적으로는 `Number`을 담을 수 있는 스택이 `Integer`도 담을 수 있어야 할 것 같습니다.
하지만 앞서서 말했듯이 제네릭은 불공변이기 때문에 `Integer`가 `Number`의 하위타입인 것은 전혀 상관이 없게 됩니다.

<br>

그렇다면 이러한 문제를 어떻게 해결할 수 있을까요??

<br>

바로 이번 글의 주제인 `한정적 와일드카드` 타입을 이용하여 이를 해결할 수 있습니다.

```java
public class Stack<E> {
    private List<E> list = new ArrayList<>();

    public void pushAll(Iterable<? extends E> src) {
        for(E e : src) {
            push(e);
        }
    }

    public void push(E e) {
        list.add(e);
    }
}
```

`pushAll()` 입력 매개변수 타입은 `E의 Iterable`이 아니라 `E의 하위타입의 Iterable` 이라고 변경해주어 타입을 안전하고 깔끔하게 사용할 수 있게 됩니다.

<br>

#### popAll (consumer - super)

이제 `pushAll()` 과 대칭되는 `popAll()` 메서드를 작성해보겠습니다. `popAll()` 메서드는 `Stack`안의 모든 원소를 주어진 컬렉션으로 옮겨 담는 역할을 수행합니다.

```java
public void popAll(Collection<E> dst) {
    while (!isEmpty()) {
        dst.add(pop());
    }    
}
```

해당 코드 역시 주어진 컬렉션의 원소 타입이 스택의 원소 타입과 일치한다면 말끔히 컴파일되고 문제없이 동작하게 됩니다.

<br>

하지만 역시 `Stack<Number>`의 원소를 `Object`용 컬렉션으로 옮기려 한다고 하면, 결과는 앞에서 보았던것 처럼 `Compile Error`가 발생하게 됩니다.

```java
@DisplayName("Number stack을 Object 타입의 컬렉션으로 반환이 가능한지")
@Test
void popAll() {
    Stack<Number> stack = new Stack();
    List<Integer> intList = Arrays.asList(1, 2, 3, 4);
    stack.pushAll(intList);
    
    List<Object> objList = new ArrayList<>();
    List<Number> numberList = new ArrayList<>();
    
    // Compile Error
    stack.popAll(objList);
    stack.popAll(numberList);
    
    assertThat(numberList).contains(1, 2, 3, 4);
}
```

해당 에러 역시 원인은 제네릭이 불공변이기 때문입니다. 즉, 이번에도 와일드카드 타입으로 문제를 해결할 수 있다는 뜻입니다.

<br>

`E의 Collection`이 아니라 `E의 상위 타입의 Collection` 이라고 변경해주어 깔끔하게 사용이 가능합니다.

```java
public void popAll(Collection<? super E> dst) {
    dst.addAll(list);
    list.clear();
}
```

이제 `Stack`과 클라이언트 코드 모두 말끔하게 컴파일이 가능합니다. 여기서 우리가 얻어가야 할 점을 한가지 문장으로 정리 할 수 있습니다.

>유연성을 극대화하려면 원소의 생산자나 소비자용 입력 매개변수에 와일드카드 타입을 사용하라.


```text
참고.

입력 매개변수가 생산자와 소비자 역할을 동시에 한다면 와일드카드 타입을 써도 좋을게 없습니다.
그 이유는 타입을 정확히 지정해야 하는 상황이기 때문입니다.
```


<br><br>

### PECS 공식 (producer-extends, consumer-super)

<hr>

앞에서 설명했지만 이를 공식화하면 와일드카드 타입을 써야하는 상황에서 어떤 타입을 사용해야 할지 기억하는데 도움이 될 것으로 보입니다.

<br>

PECS를 풀어서 설명하면 매개변수화 타입 T가 생상자라면 `<? extends T>`를 사용하고, 소비자라면 `<? super T>`를 사용한다는 의미입니다.

<br>

이 공식을 기억하고 다시 실제 코드에 적용시키는 과정을 살펴보겠습니다.

<br>

`Choose` 생성자로 넘겨지는 `choices` 컬렉션은 T 타입의 값을 생산하고 저장하는 역할을 수행합니다.

```java
public Choose(Collection<T> choices)
```

하지만 앞에서 배운 공식을 이용해 T를 확장하는 와일드카드 타입을 사용해 선언하면 이전 코드에서 사용할 수 없는 `Choose<Number>` 생성자에 `List<Integer>`를 넣을 수 있게 됩니다.

```java
public Choose(Collection<? extends T> choices) {
    choiceList = new ArrayList<>(choices);
}
```

<br>

`union` 메서드도 한번 살펴보겠습니다.

```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2);
```

s1, s2, 모두 E의 생산자이니 `PECS` 공식에 따라 다음과 같이 선언해야 합니다.

```java
public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2);
```

앞에서 말했던 것처럼 반한 타입에는 한정적 와일드카드 타입을 사용하면 안됩니다. 클라이언트 코드에서도 와일드 카드 타입을 사용하야 하기 때문에 오히려 유연성을 떨어트리기 때문입니다.

<br>

이런 코드를 통해 클라이언트 코드는 다음과 같이 사용될 수 있게 됩니다.

```java
Set<Integer> integers = Set.of(1, 2, 3);
Set<Double> doubles = Set.of(2.0, 4.0, 6.0);
Set<Number> numbers = union(integers, doubles)
```

정리하자면, 한정적 와일드카드를 제대로만 사용한다면 클래스 사용자는 와일드카드 타입이 쓰였다는 사실조차 의식하지 못할 것 입니다.

<br>

그 이유는, 받아들여야 할 매개변수를 받고 거절해야 할 매개변수는 거절하는 작업이 알아서 이뤄지기 때문입니다.

<br>

이를 좀 더 확장해서 생각한다면, 만약 클래스 사용자가 와일드카드 타입을 신경 써야 한다면 그 `API` 설계에 어떠한 문제가 있다는 의미로 해석이 가능합니다.

<br><br>

### 메서드 선언에 타입 매개변수가 한 번만 나오면 와일드카드로 대체하라

<hr>

와일드카드와 관련해 논의해야 할 주제가 하나 더 있습니다.

<br>

타입 매개변수와 와일드카드에는 공통되는 부분이 많아서, 메서드를 정의할 때 둘 중 어느 것을 사용해도 괜찮을 때가 많습니다.


```java
public static <E> void swap(List<E> list, int i, int j) // 타입 매개변수 
public static void swap2(List list, int i, int j)       // 와일드카드
```

여러분은 어떤 방법을 사용하는 것이 더 좋다고 생각하시나요? 책에서는 두 번째 방법을 추천하고 있습니다.

<br>

그 이유는 어떤 리스트든 이 메서드에 넘기면 명시한 인덱스의 원소들을 교환해주기 때문입니다.

<br>

하지만, 두 번째 swap 선언에는 문제가 하나 있는데, 다음과 같이 구현한 코드가 컴파일되지 않는다는 점입니다.

```java
public static void swap(List<?> list, int i, int j){
   list.set(i, list.get(j, list.get(i)));
}
```

이 코드를 컴파일 하면 방금 꺼낸 원소를 리스트에 다시 넣을 수 없다고 합니다. 원인은 알다시피 리스트 타입이 `List<?>인데 List<?>` 에는 `null` 외에는 어떤 값도 넣을 수 없기 때문입니다.

<br>

이럴 때 방법으로 `도우미 메서드`라는 것을 사용하면 됩니다.

<br>

`도우미 메서드`의 역할은 와일드카드 타입의 실제 타입을 알려주는 `private 메서드`라고 할 수 있습니다.

```java
public static void swap(List<?> list, int i, int j) {
    swapHelper(list, i, j); 
}
private static <E> void swapHelper(List<E> list, int i, int j) {
    list.set(i, list.set(j, list.get(i)));    
}
```

`swapHelper` 메서드는 리스트가 `List<E>`임을 알고 있습니다. 즉, 이 리스트에서 꺼낸 값이 항상 `E`이고, `E 타입의 값이라면 이 리스트에 넣어도 안전함`을 알고 있게 됩니다.

<br>

다소 복잡하게 구현했지만 이제 깔끔히 컴파일되게 됩니다.

<br><br>

### 결론

<hr>

조금 복잡하더라도 와일드카드 타입을 적용하면 `API`가 훨씬 유연해진다는 것을 알 수 있습니다.

<br>

그러니 널리 쓰일 라이브러리를 작성한다면 반드시 와일드카드 타입을 적절히 사용하는 것을 고려했으면 합니다.
