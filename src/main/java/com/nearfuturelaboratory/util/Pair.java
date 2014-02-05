package com.nearfuturelaboratory.util;

public class Pair<F, S> {
    protected F first; //first member of pair
    protected S second; //second member of pair

    protected Pair() {
    	
    }
    
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
    
    public String toString() {
    	return "{"+first.toString()+","+second.toString()+"}";
    }
}