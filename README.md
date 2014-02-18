ModelMapper
===========

ModelMapper helps parse JSON strings and map it to objects of model classes automatically.

This is your json.
```json
{
  "name": "Eunjae Lee",
  "noshow": false,
  "albumcnt": 10
}

```

This is your model.

```java
class User {
  String name;
  boolean noshow;
  int albumcnt;
}
```

And all you need to do is:
```java
