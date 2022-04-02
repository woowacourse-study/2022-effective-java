## Item 86. Serializable을 구현할지는 신중히 결정하라


### 직렬화 정말 써야할까??

어떤 클래스에 직렬화를 구현하고자 하면 `implements Serializable`을 붙이기만 하면 된다. 매우 간단해 보이고 편리해 보이지 않는가??

이렇게 간단하게 만들 수 있게되면 개발자가 특별히 신경 쓸게 없다는 오해?가 생길 수 있지만, 그에 상응하는 대가를 치룰수도 있다.

가장 큰 문제는 `Serializable` 구현시 릴리스한 뒤에는 수정이 `매우` 어렵다는 점이다.

클래스가 `Serializable`을 구현하면 직렬하된 바이트 스트림 인코딩도 하나의 공개 API가 된다. 만약 이 클래스가 널리널리 퍼지게 된다면 그 직렬화 형태도 영원히 지원해줘야 한다.

<br>

### Serializable을 구현하면 캡슐화가 깨진다.

`Serializable`을 사용한 기본 직렬화 형태에서는 `private`, `package-private` 수준의 필드마저도 API로 공개가 된다. 즉, 캡슐화가 깨져버리게 된다.

예시 코드를 통해서 한번 살펴보자.

<img width="785" alt="스크린샷 2022-04-02 오후 10 06 50" src="https://user-images.githubusercontent.com/48710213/161384737-9f6a9bf2-1d82-48bb-b007-dc951b3dea8d.png">


위의 코드는 `Crew` 객체를 `ObjectOutputStream`을 통해 직렬화를 한 뒤에 이를 `FileOutputStream`을 통해 `crew.ser` 이라는 파일에 객체 정보를 저장한 것이다.

직렬화 결과를 한번 살펴보자.

<img width="1090" alt="스크린샷 2022-04-02 오후 9 39 20" src="https://user-images.githubusercontent.com/48710213/161384769-12d438b0-618d-403c-b50d-5d7c0e6707ba.png">

위의 코드 내용을 살펴보면 분명 `private`으로 설정한 필드인 `name`과 `type`이 노출되는 것을 확인할 수 있다. 즉, 앞서서 말한 것처럼 캡슐화가 깨진다고 할 수 있다.

<br>

### 직렬화의 또 다른 문제

또 다른 문제도 있다.

만약에 Crew 객체에 새로운 필드가 추가되었다고 가정하자.

<img width="785" alt="스크린샷 2022-04-02 오후 10 10 54" src="https://user-images.githubusercontent.com/48710213/161384849-d58fde69-5914-43b5-96d8-b2862010c6f2.png">

이렇게 필드가 추가한 후 앞서서 직렬화된 객체에 역직렬화를 수행하면 어떻게 될까??

<img width="1300" alt="스크린샷 2022-04-02 오후 9 49 00" src="https://user-images.githubusercontent.com/48710213/161384862-0001fc35-2084-443c-8dac-f6468749ccff.png">

`java.io.InvalidClassException`이 발생하게 된다.

그 원인을 한번 살펴보기 위해서 자바의 직렬화 스팩을 한번 살펴보자.

```text
    It may be declared in the original class but is not required. 
    The value is fixed for all compatible classes. 
    If the SUID is not declared for a class, the value defaults to the hash for that class. 
```

위의 스펙을 토대로 정리하자면 `serialVersionUID`는 기본적으로 개발자가 직접 정의하지 않을 경우 해당 객체의 `hashCode`를 기반으로 설정이 된다. 하지만 필드 추가로 인하여 클래스의 구조가 바뀌게 되어 hashCode 역시 변경되고 이로 인해서 연쇄적으로 `serialVersionUID`가 바뀌게 되는 것이다. 즉, 이로 인해 역직렬화에 실패하게 되는 것이다.

<br>

그렇다면 `serialVersionUID`를 개발자가 직접 정의해주면 문제가 해결되지 않을까라는 생각을 할 수 있다.

하지만 이 역시 100% 문제를 해결하지 못한다.

만약 어떤 문제로 인해서 인스턴스 필드의 이름이 수정된 상태에서 역직렬화를 수행하면 어떻게 될까??

<img width="1054" alt="스크린샷 2022-04-02 오후 9 53 00" src="https://user-images.githubusercontent.com/48710213/161384895-e4d41a2c-0867-45e6-ae90-76e6393c9d7e.png">

역직렬화는 실패하지 않지만 필드가 변경됨으로 인해 정보를 가져오지 않아 `NULL` 값을 들고 오게 된다.

<br>

또 필드 타입이 변경된 상태에서 역직렬화를 수행하면 어떻게 될까??

이렇게 기존에 존재하는 타입을 타입으로 변경했다. 이 상황에서 역직렬화를 하면 `java.io.InvalidClassException` 또는 `java.lang.ClassCastException`이 발생한다.

<img width="1334" alt="스크린샷 2022-04-02 오후 9 55 57" src="https://user-images.githubusercontent.com/48710213/161384939-7f48cb69-5b8c-4f7a-9902-34108ddcda93.png">

<br>

### 결론

위의 상황을 바탕으로 하여 자바 직렬화가 사용된 구조에서 유의해야 할 점을 체크리스트를 통해서 정리해보자.

- [ ] 특별한 문제가 없으면 자바 직렬화 버전 `serialVersionUID`은 개발자가 직접 관리해라
- [ ] 역직렬화 대상 클래스의 멤버 변수 타입 변경을 지양해라. 자바 역직렬화는 타입 변경에 엄격하다.
- [ ] DB, 캐시에 장기간 저장될 정보는 자바 직렬화 사용을 지양해라. (결국 지뢰가 되어 터질 가능성이 존재한다)
- [ ] 개발자가 직접 컨트롤 하지 못하는 클래스는 직렬화 사용을 지양해라.

<br>

자바 직렬화는 되도록 사용하지 않거나 매우 충분히 고려를 하고 사용해야 한다.

그래도 정말 직렬화를 정말 사용하고 싶으면 다음과 같은 상황에서만 직렬화를 사용하는 것을 추천한다.

1. 외부 저장소로 저장되는 데이터는 짧은 만료시간의 데이터를 제외하고 자바 직렬화 사용을 지양
2. 역직렬화시 반드시 예외가 생긴다는 것을 생각하고 개발
3. 자주 변경되는 비즈니스적인 데이터를 자바 직렬화을 사용을 금지
4. 긴 만료 시간을 가지는 데이터는 JSON 등 다른 포맷을 사용하여 지정
