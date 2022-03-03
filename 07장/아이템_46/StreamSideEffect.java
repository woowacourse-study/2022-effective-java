package chapter07.item_46;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamSideEffect {

    public static Map<String, Long> streamSideEffect(final Stream<String> strings) {
        Map<String, Long> sideEffect = new HashMap<>();
        strings.forEach(string -> {
                    sideEffect.merge(string, 1L, Long::sum);
                });

        return sideEffect;
    }

    public static Map<String, Long> streamBestCase(final List<String> strings) {
        return strings.stream()
                .collect(groupingBy(string -> string, counting()));
    }

    static void func1(){
        List<Integer> matched = new ArrayList<>();
        List<Integer> elements = new ArrayList<>();
        for(int i=0 ; i< 100 ; i++) {
            elements.add(i);
        }
        elements.parallelStream()
                .forEach(e -> {
                    if(e>=50) {
                        System.out.println(Thread.currentThread().getId() +  " " + matched);
                        matched.add(e);
                    }
                });
        System.out.println(matched.size());

    }
    static void func2(){
        List<Integer> elements = new ArrayList<>();
        for(int i=0 ; i< 100 ; i++) {
            elements.add(i);
        }
        List<Integer> matched = elements.parallelStream()
                .filter(e -> e >= 50)
                .collect(toList());
        System.out.println(matched.size() + "  " + matched);
    }

    public static void main(String[] args) {
        List<String> strings = List.of("a", "a", "b", "b", "c", "c", "d");
        System.out.println(streamSideEffect(strings.stream()));
        System.out.println(streamBestCase(strings));

        //func1();
        func2();
    }

}
