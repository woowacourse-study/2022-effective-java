# 아이템 16 : public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라

## 🙀 캡슐화의 이점을 제공하지 못하는 클래스

```JAVA
class Point {
    public int x;
    public int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
```

### API를 수정하지 않고는 내부 표현을 바꿀 수 없음

<details>
<summary>getter,setter 를 이용하면 표현 변경 가능</summary>
<div markdwon="1">

```JAVA
public double getX() {
	return x;
}

public double getY() {
	return y;
}
```

</div>
</details>

### 불변식을 보장할 수 없음

<details>
<summary>클라이언트가 직접 값을 변경할 수 있음</summary>
<div markdwon="1">

```JAVA
public static void main(String[] args) {
	Point point = new Point(1, 2);
	System.out.println(point.x); // 1

	point.x += 1;
	System.out.println(point.x); // 2
}
```

</div>
</details>

### 외부에서 필드에 접근할 때 부수 작업을 수행할 수 없음

＞ 1차원적인 접근만 가능하고, 추가 로직을 삽입할 수 없음

---

## 😼 철저한 객체 지향 프로그래머가 캡슐화한 클래스

1️⃣ 필드들을 모두 private으로 변경  
2️⃣ public 접근자(getter), 변경자(setter) 추가

```JAVA
public class Point {
	private int x;
	private int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() { return x; }
	public int getY() { return y; }

	public void setX(int x) { this.x = x; }
	public void setY(int y) { this.y = y; }
}
```

### 유연성을 얻음

＞ getter/setter 메서드를 통해 언제든지 내부 표현을 바꿀 수 있음

---

## 😽 private 중첩 클래스

```JAVA
public class TopPoint {
	private static class Point {
		public double x;
		public double y;
	}

	public Point getPoint() {
		Point point = new Point();
		point.x = 3.5;
		point.y = 4.5;
		return point;
	}
}
```

### 처음 제시된 3가지 문제점을 해결할 수 있음

＞ TopPoint 클래스에서는 얼마든지 Point 클래스의 필드를 조작할 수 있음

＞ 외부 클래스에서는 Point 클래스의 필드에 직접 접근할 수 없음

### package-private 클래스 역시 3가지 문제 해결할 수 있음

＞ 해당 클래스가 포함되는 패키지 내에서만 조작할 수 있음

＞ 패키지 외부에서는 접근할 수 없음

### 클래스와 필드를 선언하는 입장에서나 클라이언트 입장에서나 훨씬 깔끔함

＞ 클래스를 통해 표현하려는 추상 개념만 상세하고 올바르게 표현하면 됨

---

## 😿 자바 플랫폼 라이브러리에서 필드를 노출시킨 사례

> java.awt.package 패키지 - Point, Dimension 클래스

_java.awt.Component 클래스 내부_

```JAVA
    ...

    public Dimension getSize() {
		return size();
	}

	@Deprecated
	public Dimension size() {
		return new Dimension(width, height);
	}

    ...
```

_java.awt.Dimension 클래스 내부_

```JAVA
public class Dimension extends Dimension2D implements java.io.Serializable {
	public int width;
	public int height;

    ...
}
```

### Dimesion 클래스의 필드는 가변으로 설계됨

＞ getSize 를 호출하는 모든 곳에서 방어적 복사를 위해 인스턴스를 새로 생성해야 함

---

## 😾 불변 필드를 노출한 public 클래스

```JAVA
public class Time {
	private static final int HOURS_PER_DAY = 24;
	private static final int MINUTES_PER_HOUR = 60;

	public final int hour;
	public final int minute;

	public Time(int hour, int minute) {
		if (hour < 0 || hour > HOURS_PER_DAY) {
			throw new IllegalArgumentException("시간: " + hour);
		}
		if (minute < 0 || minute > MINUTES_PER_HOUR) {
			throw new IllegalArgumentException("분: " + minute);
		}
		this.hour = hour;
		this.minute = minute;
	}

	...
}
```

### 불변식은 보장할 수 있음

＞ 각 인스턴스가 유효한 시간을 표현함을 보장함

### 여전히 불변식 보장 외의 두가지 문제를 해결할 수 없음
