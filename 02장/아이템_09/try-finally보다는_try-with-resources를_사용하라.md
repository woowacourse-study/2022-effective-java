# 아이템 09 try-finally 보다는 try-with-resources를 사용하라

## try-finally란

자바에는 close를 호출해 직접 닫아줘야 하는 자원이 있다.  
ex) InputStream, OutputStream, java.sql.Connection

```java
public static String inputString() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String result = br.readLine();
    br.close();
    return result;
}
```

위 코드는 BufferedReader를 사용해준 뒤에 close를 호출해 직접 닫아주는 코드다. 하지만 위 방법대로 close를 호출하게 되면 문제가 발생할 수 있다. BufferedReader는 사용 중 IOException이 발생할 수 있는데, 만약 br.readLine() 메서드에서 IOException이 발생하게 되면 메서드가 종료되므로 close가 호출되지 않고 스트림이 메모리에 남아있게 된다.

따라서 예외가 발생하더라도 자원을 닫을 수 있도록 해줘야 하는데, 전통적으로 try-finally 문을 사용해서 close 처리를 해주었다.

```java
public static String inputString() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
        return br.readLine();
    } finally {
        br.close();
    }
}
```

finally 블록은 try, catch 블록이 끝난 뒤 실행할 로직을 정의해 주는 블록이다. 따라서 이제 IOException이 발생하게 되더라도 상위 메서드로 IOException 객체를 던져준 뒤 finally 메서드를 종료하게 된다.

그렇다면 만약 close를 여러 번 호출해야 하는 상황이 오면 어떻게 될까?

```java
public static void inputAndWriteString() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        try {
             String line = br.readLine();
            bw.write(line);
        } finally {
            bw.close();
        }
    } finally {
        br.close();
    }
}
```

코드가 굉장히 지저분해지게 될 수 있다.

사실 가장 큰 문제는 이런 것들이 아니다. 다시 맨 위의 inputString 메서드로 돌아가 보자.

inputString 메서드의 try 블록을 실행하던 도중 기기에 문제가 생긴다면 readLine이 정상적으로 실행되지 못하고 예외를 던지게 되고, 같은 이유로 finally 블록의 close 메서드도 예외를 던지게 된다.

만약 이 예외들을 catch해서 상위 메서드에서 예외 정보를 체크해본다면, finally 블록에서 터진 예외가 try 블록에서 생긴 예외를 집어 삼켜서 finally 블록의 예외만 체크하게 된다. try 블록에서 터진 예외로 인해 finally 블록에서 예외가 발생했음에도 불구하고 최초 원인인 예외를 체크하지 못하게 되는 것이다. 물론 적절한 코드를 통해 최초 원인 예외를 체크할 수는 있지만, 코드가 너무 더러워지기 때문에 추천하는 방법은 아니다.

실제로 다음과 같은 방법으로 체크해 보았다.

```java
public class Application {

    public static void main(String[] args) {
        try {
            check();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void check() {
        try {
            throw new IllegalArgumentException();
        } finally {
            throw new NullPointerException();
            // 예외는 관련 없이 임의로 선택했다.
        }
    }
}
```

```
실행 결과

java.lang.NullPointerException 
    at Application.check(Application.java:14)  
    at Application.main(Application.java:4
```

발생하는 예외를 main의 catch 블록에서 체크해서 printStackTrace를 호출했는데, 14번 라인, 즉 finally 블록에서 던져진 NullPointerException을 catch 하고 try 블록의 IllegalArgumentException은 무시된 것을 볼 수 있다.

정리하자면 try-finally는 가독성을 해칠 가능성이 높으며, 예외 처리 로직을 작성하기에 미묘한 결점이 존재한다.

## 해결책: try-with-resources

이러한 try-finally 방식의 단점을 보완하기 위해 자바 7 버전부터는 try-with-resources가 도입되었다. try-with-resources를 사용하기 위해서는 사용하는 자원이 AutoCloseable 인터페이스를 구현해야 한다.

```java
public interface AutoCloseable {

    void close() throws Exception;
}
```
AutoCloseable 인터페이스는 단지 close 메서드 하나만을 정의해 놓은 간단한 인터페이스이며, 자바 라이브러리와 서드파티 라이브러리의 수많은 클래스와 인터페이스는 이미 AutoCloseable을 구현하거나 확장해두었다.

따라서, close가 필요한 자원 클래스를 커스텀 할 일이 있다면 AutoCloseable을 반드시 구현할 것을 권장한다.

그렇다면 이 try-with-resources는 어떻게 사용해야 할까? 앞선 inputString 메서드를 try-with-resources 방식으로 재구성해보았다.

```java
public static String inputString() throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStream(System.in))) {
        return br.readLine();
    }
}
```

놀랍게도 코드의 가독성이 대단히 좋아졌다! 또한 가독성 뿐만이 아니다. 예외가 발생했을 때 디버깅 하기에도 더 편리하다. 아까 가정한 상황처럼 inputString 메서드의 readLine과 close 모두에서 예외가 발생하는 경우, close(물론 코드 상으로는 보이지 않지만) 호출 시 발생하는 예외는 숨겨지고 readLine의 예외가 기록된다.

이렇게 숨겨진 예외는 무시되는 것이 아니라, suppressed 상태가 되어 stackTrace 시 숨겨졌다는 메시지로 출력된다. suppressed 상태가 된 예외는 자바 7부터 도입된 getSuppressed 메서드를 통해 가져와서 사용할 수 있다. 다음의 테스트 코드를 보자.

```java
public class Application {

    public static void main(String[] args) {
        try {
            check();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void check() throws Exception {
        try (IllegalArgumentExceptionThrower thrower = new IllegalArgumentExceptionThrower()) {
            throw new NullPointerException();
        }
    }

    static class IllegalArgumentExceptionThrower implements AutoCloseable {
        @Override
        public void close() throws Exception {
            throw new IllegalArgumentException();
        }
    }
}
```

```
실행 결과

java.lang.NullPointerException  
    at Application.check(Application.java:17)
    at Application.main(Application.java:9)
    Suppressed: java.lang.IllegalArgumentException
        at Application$IllegalArgumentExceptionThrower.close(Application.java:24)
        at Application.check(Application.java:16)
        ... 1 more
```

직접 throw 한 NullPointerException이 catch되어 출력되며, 이 때 close 메서드에서 던지는 IllegalArgumentException은 Suppressed: 태그 뒤로 출력되는 것을 볼 수 있다.

## try-with-resources와 catch

try-with-resources 구조 역시 기존 try-finally 처럼 catch를 병용해서 사용할 수 있다.

```java
public static String inputString() {
    try (BufferedReader br = new BufferedReader(new InputStream(System.in))) {
        return br.readLine();
    } catch (IOException e) {
        return "IOException 발생";
    }
}
```

## 정리

close를 통해 회수해야 하는 자원을 다룰 때는 try-finally를 사용하는 대신 **반드시** try-with-resources를 사용하자. 보다 가독성 좋으며, 쉽고 정확하게 자원을 회수할 수 있다.

또한 커스텀 자원을 회수해야 하는 경우 AutoCloseable 인터페이스를 구현하는 것을 잊지 말도록 하자.