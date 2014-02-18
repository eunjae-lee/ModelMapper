ModelMapper
===========

ModelMapper is an Android library to help parsing JSON strings and mapping it to objects of model classes automatically.

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
User user = ModelMapper.getInstance().generate(User.class, jsonString);
```

And if you're using AndroidAnnotations, it gets simpler. You just need to put an converter at rest client interface.

```java
@Rest(converters = {JsonToModelConverter.class})
public interface MyRestClient {
 
  @Get("/...")
  User getUser();
}
```

It's done.

# Download

## Maven

```xml
  <dependency>
    <groupId>net.eunjae.android.modelmapper</groupId>
    <artifactId>ModelMapper</artifactId>
    <version>1.0.0</version>
  </dependency>
```
## Gradle

```
  compile 'net.eunjae.android.modelmapper:ModelMapper:1.0.0'
```

# Usage

Check out the wiki page: https://github.com/eunjae-lee/ModelMapper/wiki


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/eunjae-lee/modelmapper/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

