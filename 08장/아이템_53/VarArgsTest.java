package item53;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VarArgsTest {

    public static void varArgError(String s, int ... args) {

    }

    public static <T> List<T> test(List<T>... variable) {
        Object[] objArr = variable; //오브젝트의 배열로 업캐스팅 된 상태이므로 다른 제너릭 타입의 리스트도 대입이 가능합니다.
        objArr[0] = Arrays.asList(1,2);

        return variable[0];
    }


    public static String firstOfFirst(List<String>... strings) {
        List<Integer> ints = Collections.singletonList(42);
        Object[] objects = strings;
        objects[0] = ints; // Heap pollution

        return strings[0].get(0); // ClassCastException
    }

    public static int min(int arg, int ... args) {
        return Arrays.stream(args)
                .min()
                .orElse(arg);
    }

    public static void main(String[] args) {
        min(1);
    }
}
