- 구체적인 클래스 타입을 알지 못해도 그 클래스의 정보(메서드, 타입, 변수 등등)에 접근할 수 있게 해주는 자바 API

```java

class Cat {

    public void introduceMyself() {
        System.out.println("나는 치치다옹");
    }

}

public class Reflection {

    public static void main(String[] args) {
        Object 치치 = new Cat();

//        컴파일 에러
//        치치.introduceMyself();
    }

}

```

Cat 이라는 구체적인 타입을 알지 못한다.

이 때 해당 메소드를 실행시켜줄 수 있는 것이 Reflection

```java

class Cat {

    public void introduceMyself() {
        System.out.println("나는 치치다옹");
    }
}

public class Reflection {

    public static void main(String[] args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object 치치 = new Cat();
        Class 치치_클래스 = 치치.getClass();
        Method[] 치치_클래스_Methods = 치치_클래스.getMethods();
        System.out.println(Arrays.toString(치치_클래스_Methods));
        // public void item65.Cat.introduceMyself(),
        // public boolean java.lang.Object.equals(java.lang.Object),
        // public java.lang.String java.lang.Object.toString(),
        // public native int java.lang.Object.hashCode() 등
        Method 치치_소개_함수 = 치치_클래스.getMethod("introduceMyself"); // NoSuchMethodException 에러 감싸줘야함
        치치_소개_함수.invoke(치치, null); // IllegalAccessException
        // 나는 치치다옹
    }
}

```

자바에서는 JVM이 실행되면 사용자가 작성한 자바 코드가 컴파일러를 거쳐 바이트 코드로 변환되어 static 영역에 저장된다. 

Reflection API는 이 정보를 활용한다. 그래서 클래스 이름만 알고 있다면 언제든 static 영역을 뒤져서 정보를 가져올 수 있는 것이다.

```java

public class Single {

    private static Single instance;

    private Single() {
    }

    public static Single getInstance() {
        if (instance == null) {
            instance = new Single();
        }
        return instance;
    }
}

```

```java

public class SingleMain {
    public static void main(String[] args)
            throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Class<?> singleClass = Class.forName("item65.Single"); // ClassNotFoundException
        Constructor<?> singleConstructor =  singleClass.getDeclaredConstructor(null);// NoSuchMethodException
        
        singleConstructor.setAccessible(true);
        
        Single single = (Single) singleConstructor.newInstance(); // reflection 을 활용한 객체 생성
        Single instance = Single.getInstance(); // singleton 객체 생성
        System.out.println(single);
        System.out.println(instance);
    }
}

//item65.Single@3cb5cdba
//item65.Single@56cbfb61

```
