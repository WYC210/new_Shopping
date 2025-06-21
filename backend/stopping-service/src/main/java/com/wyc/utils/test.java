package com.wyc.utils;

import java.util.Map;
import java.util.HashMap;


public class test {

        public static void main(String[] args) {
            String input = "Chenda Information";
            Map<Character, Integer> letterCount = new HashMap<>();
            for (char c : input.toCharArray()) {
                if (Character.isLetter(c)) {
                    char key = Character.toLowerCase(c);
                    if (letterCount.containsKey(key)) {
                        letterCount.put(key, letterCount.get(key) + 1);
                    } else {
                        letterCount.put(key, 1);
                    }
                }
            }
            for (Map.Entry<Character, Integer> entry : letterCount.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }


}
