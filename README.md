![Mi-Stack logo](assets/logo.png)

[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Java 11.0+](https://img.shields.io/badge/java-11.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.igormaznitsa/mi-stack/badge.svg)](http://search.maven.org/#artifactdetails|com.igormaznitsa|mi-stack|1.0.0|jar)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-cyan.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
[![YooMoney donation](https://img.shields.io/badge/donation-Yoo.money-blue.svg)](https://yoomoney.ru/to/41001158080699)

# What is it?

Just abstract stack allows to mark its items by tags and get elements through either stream or iterator.

# Example

The example below shows minimal use case with pushing several tagged items on the stack and iteration through them with
predicated streams.

```java
    var tagSun=MiStackStringTag.tagsOf("Sub");
    var tagGalaxy=MiStackStringTag.tagsOf("Galaxy");

    try(var stack=new MiStackArrayList<>()){
      stack.push(MiStackItemImpl.itemOf("Earth",tagSun));
      stack.push(MiStackItemImpl.itemOf("Moon",tagSun));
      stack.push(MiStackItemImpl.itemOf("Antares",tagGalaxy));
      stack.push(MiStackItemImpl.itemOf("Vega",tagGalaxy));

      assertArrayEquals(new Object[]{"Moon","Earth"},stack.stream(stack.allTags(tagSun)).map(MiStackItem::getValue).toArray());
      assertArrayEquals(new Object[]{"Moon","Earth"},stack.stream(stack.anyTag(tagSun)).map(MiStackItem::getValue).toArray());
      assertArrayEquals(new Object[]{"Vega","Antares"},stack.stream(stack.allTags(tagGalaxy)).map(MiStackItem::getValue).toArray());
    }
```