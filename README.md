# meeruman

An API testing tool written in Clojure using seesaw and other JVM packages.

# Compile

The project requires GraalVM to run since it uses GraalJS engine to run custom
javascript code. Kotlin compiler is also required as it uses OKHttp Client.

To compile a standalone jar, you also need to download the jars manually. The required jars are

* [compiler-23.0.3.jar](https://mvnrepository.com/artifact/org.graalvm.compiler/compiler/23.0.3)
* [compiler-management-23.0.3.jar](https://mvnrepository.com/artifact/org.graalvm.compiler/compiler-management/23.0.3)
* [graal-sdk-23.0.3.jar](https://mvnrepository.com/artifact/org.graalvm.sdk/graal-sdk/23.0.3)
* [icu4j-74.1-module.jar](https://mvnrepository.com/artifact/com.ibm.icu/icu4j/74.1)
* [js-23.0.3.jar](https://mvnrepository.com/artifact/org.graalvm.js/js/23.0.3)
* [js-scriptengine-23.0.3.jar](https://mvnrepository.com/artifact/org.graalvm.js/js-scriptengine/23.0.3)
* [regex-23.0.3.jar](https://mvnrepository.com/artifact/org.graalvm.regex/regex/23.0.3)
* [truffle-api-23.0.3.jar](https://mvnrepository.com/artifact/org.graalvm.truffle/truffle-api)

## License

Copyright 2024 whirlun <whirlun@yahoo.co.jp>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the “Software”), to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions
of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.