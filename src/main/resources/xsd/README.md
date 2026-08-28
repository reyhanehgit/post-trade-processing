# XSD Schema-First Model Generation

This folder is the schema source of truth for trade payload shape.

## Files

- `trade-types.xsd`: defines shared trade entities and product-specific structures.

## Build Integration

Gradle task `generateXsdModel` compiles this XSD into Java classes under:

- `build/generated/sources/xjc/main/org/example/fidstp2/schema/trade`

`compileJava` depends on `generateXsdModel`, so generated classes are always present during compile.

## Quick Try

```bash
./gradlew generateXsdModel
./gradlew test --tests "*XsdTradeEnvelopeReaderTest"
```

## Recommended Architecture

Use generated JAXB classes as transport/schema models (`schema.trade.*`) and map them into handwritten domain entities (`domain.*`).

