package com.nearfuturelaboratory.humans.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.commons.collections4.CollectionUtils;

public class ApacheCommonsCollectionsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void test() {
		Collection<String> bag_1 = new ArrayList<String>(){{
			add("apple"); 
			add("orange");
			add("blueberry");
			add("tomato");
			add("lettuce");
		}};

		Collection<String> bag_2 = new ArrayList<String>() {{
			add("apple");
			add("orange");
			add("banana");
			add("strawberry");
			add("onion");
		}};

		System.out.println("bag one: "+bag_1);
		System.out.println("bag two: "+bag_2);
		
		Collection<String> disjunction = CollectionUtils.disjunction(bag_1, bag_2);
		System.out.println("disjunction: "+disjunction);
		
		Collection<String> union = CollectionUtils.union(bag_1, bag_2);
		System.out.println("union: "+union);
		
		Collection<String> intersection = CollectionUtils.intersection(bag_1, bag_2);
		System.out.println("intersection: "+intersection);
		
		Collection<String> subtract = CollectionUtils.subtract(bag_2, bag_1);
		System.out.println("bag_2 - bag_1: "+subtract);

		
		Collection<String> subtract_2 = CollectionUtils.subtract(bag_1, bag_2);
		System.out.println("bag_1 - bag_2: "+subtract_2);
		
		
	}

}
