package com.prestamosfacil.domain.auth.port.out;

import java.util.Map;

public interface TokenParserPort {

    Map<String, Object> parse(String token);
}
