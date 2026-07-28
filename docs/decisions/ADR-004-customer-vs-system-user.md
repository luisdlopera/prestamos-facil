# ADR-004: Distinción entre Customer y SystemUser

## Estado
Aceptado

## Contexto
El sistema maneja dos tipos de personas: quienes solicitan préstamos (clientes) y quienes gestionan el sistema (analistas, administradores). Comparten algunos atributos (nombre, email) pero tienen propósitos y ciclos de vida diferentes.

## Decisión
Modelar dos entidades separadas en el dominio:

- **Customer**: Persona que solicita un préstamo. Pertenece al módulo de clientes.
- **SystemUser**: Cuenta que inicia sesión como analista o administrador. Pertenece al módulo de autenticación.

## Consecuencias
- No hay una clase `User` genérica que represente ambos roles
- Los **customers** pueden registrarse e iniciar sesión en el portal de clientes para consultar sus préstamos y solicitudes
- Los **SystemUsers** son cuentas de back-office (analistas, administradores) que gestionan el sistema. Se implementarán en una fase posterior
- La autenticación JWT es única para `Customer` y `SystemUser`, usando `users` y `auth_tokens`
- `customers.user_id` relaciona el perfil del cliente con su identidad de acceso
- `SystemUser` y `Role` se mantienen como conceptos del back-office sin mezclar sus datos con el perfil financiero del cliente
- La separación permite evolucionar cada entidad independientemente

## Alternativas Consideradas
- **User único con roles**: Mezcla conceptos diferentes en una misma tabla
- **Herencia**: Complejidad innecesaria para dos entidades con poca superposición
