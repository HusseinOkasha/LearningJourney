package com.project3.registration;

import org.springframework.stereotype.Service;

import java.util.function.Predicate;
@Service
public class EmailValidator implements Predicate<String> {
    @Override
    public boolean test(String s){
        // TODO: add regex to validate email
         return true;
    } ;



}
