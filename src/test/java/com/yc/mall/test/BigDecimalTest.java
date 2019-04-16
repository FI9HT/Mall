package com.yc.mall.test;

import com.google.common.base.Splitter;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BigDecimalTest {
    @Test
    public void test1(){
        double d1 = 0.0002;
        double d2 = 0.0003;
        System.out.println(d1 + d2);
        BigDecimal b = new BigDecimal("0.1");
    }
    @Test
    public void test2(){
        double a = 0.25;
        double b = 4.0;
        System.out.println(a * b);
    }
    @Test
    public void test3(){
        String s = "你好：Yung";
        int a = s.indexOf("：");
        String sub = s.substring(a+1, s.length());
        System.out.println(sub);
    }
}





















