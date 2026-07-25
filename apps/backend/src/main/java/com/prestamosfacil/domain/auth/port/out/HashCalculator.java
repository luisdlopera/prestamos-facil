package com.prestamosfacil.domain.auth.port.out;

public interface HashCalculator {

    String sha256(String input);
}
