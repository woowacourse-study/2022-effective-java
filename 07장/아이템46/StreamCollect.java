package chapter07.item_46;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

public class StreamCollect {

    static List<String> collectToList(final List<User> users) {
        return users.stream()
                .map(User::getName)
                .collect(toList());
    }

    static Set<User> collectToSet(final List<User> users) {
        return users.stream()
                .sorted(comparingInt(User::getAge))
                .collect(toCollection(TreeSet::new));
    }

    static Map<String, Integer> groupingToMap(final List<User> users) {
        return users.stream()
                .collect(groupingBy(User::getName, summingInt(User::getAge)));
    }

    static Map<String, User> collectorsToMap(final List<User> users) {
        return users.stream()
                .collect(toMap(User::getName, user -> user, BinaryOperator.maxBy(comparingInt(User::getAge))));
    }

    static String joiningCollection(final List<User> users) {
        return users.stream()
                .map(User::getName)
                .collect(joining(", "));
    }

    static Map<Boolean, List<User>> partitioningByFilter(final List<User> users) {
        return users.stream()
                .collect(partitioningBy(user -> user.getAge() > 25));
    }

    static class User {
        private String name;
        private int age;

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
