package com.abcbank.shortcode.shortcode.utils;

public class StringOperator {
public static void main(String[] args) {
	String no = "001190001000062";
	System.out.println(no.substring(0,5) + "********" + no.substring(no.length() - 2, no.length()));
}
}
