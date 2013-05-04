package com.jerry.utils;

import java.util.Comparator;

public class MyComparator implements Comparator{ 
	public MyComparator(){ 
		super(); 
	} 

	public int compare(Object o1, Object o2)   { 
		String stringA = (String)o1; 
		String stringB = (String)o2; 
		return (stringA.toUpperCase()).compareTo(stringB.toUpperCase()); 
	} 

}