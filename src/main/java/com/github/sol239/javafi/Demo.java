package com.github.sol239.javafi;

import com.github.sol239.javafi.instruments.SqlInstruments;
import com.github.sol239.javafi.postgre.DBHandler;

import java.lang.reflect.Method;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Demo {



    public static void main(String[] args) {
        SqlInstruments sql = new SqlInstruments();
        String sma = sql.sma("eth", 30);
        System.out.println(sma);

    }
}
