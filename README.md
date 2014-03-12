ModelMapper
===========

ModelMapper is an Android library to help parsing JSON strings and mapping it to objects of model classes automatically.

This is your json.
```json
{
  "name": "Eunjae Lee",
  "noshow": false,
  "albuminfo": {
    "count": 10
  }
}

```

This is your model.

```java
class User {
  
  String name;
  
  @JsonProperty("noshow")
  boolean noShow;
  
  @JsonProperty("albuminfo.count")
  int albumCount;
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
    <version>1.0.6</version>
  </dependency>
```
## Gradle

```
  compile 'net.eunjae.android.modelmapper:ModelMapper:1.0.6'
```

# Usage Documentation

Check out the wiki page: https://github.com/eunjae-lee/ModelMapper/wiki

# License
[MIT](http://opensource.org/licenses/mit-license.html)

# Changelog

## 1.0.6 (2014/03/12)

* OnBeforeMapping has changed a little bit.
* A field with "HashMap" type is now mapped well.
* Null check

## 1.0.5 (2014/03/06)

* Bugs are fixed when it can't parse json strings with unusual structure.
* Test cases are added.

## 1.0.4

* Now ModelMapper just returns json string if the first argument of ModelMapper.getInstance().generate(...) is String.class.
** There was a bug that it returned an empty string in that case.

## 1.0.3

* Now this library throws exceptions when callback method(@AfterMapping) is not declared properly.

## 1.0.2

* Minor bug fix that couldn't recognize array class when it is not directly extending ArrayList.

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/eunjae-lee/modelmapper/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

