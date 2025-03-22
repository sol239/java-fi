package com.github.sol239.javafi.instrument.java;

public record IdValueRecord(Long id, Double value) {
    @Override
    public String toString() {
        return "ID=" + id + ", VALUE=" + value;
    }
}
