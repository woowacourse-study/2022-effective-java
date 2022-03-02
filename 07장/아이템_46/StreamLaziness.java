package chapter07.아이템46;

import java.util.stream.IntStream;

public class StreamLaziness {

    private static IntStream createAStreamAndPerformSomeSideEffectWithPeek() {
        return IntStream.of(1, 2, 3)
                .peek(number -> System.out.printf("First. My number is %d\n", number))
                .map(number -> number + 1);
    }

    private static void consumeTheStream(IntStream stream) {
        stream.filter(number -> number % 2 == 0)
                .forEach(number -> System.out.printf("Third. My number is %d\n", number));
    }

    public static void main(String[] args) {
        IntStream stream = createAStreamAndPerformSomeSideEffectWithPeek();
        System.out.println("Second. I should be the second group of prints");
        consumeTheStream(stream);
    }

}
