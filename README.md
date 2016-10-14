Bobril Native
===

What is this?
--

This is reimagination of React Native in context of Bobril.
It does not use anything from React Native except [CSSLayout](https://github.com/facebook/css-layout).
It uses identical naming of basic elements `Text` and `View`.

Differences to Bobril
--

`b.addRoot` method can create only fullscreen nodes.
Usage of `b.style` is mandatory for any styling.
There are no classnames, but b.styleDef works (without pseudo classes).
Lifecycle methods `postInitDom`, `postUpdateDom`, `postUpdateDomEverytime` are not called at all.
Lifecycle method `destroy` has just 2 parameters (ctx and me). There is no `b.getDomNode`, it would have no use.

Compatibility
--

Built application is compatible with Android 4.4 (API 19) or better.
Reason is that 4.4 is first version of Android with Chrome (modern fast browser) inside Webview.

How to start
--

1. install bobril-build (or upgrade to latest version)

    npm i bobril-build -g

2. install bobril native plugin to bobril-build

    bb plugins -i bb-bobriln-plugin

3. install JavaJDK and set JAVA_HOME enviromental variable

    for example: JAVA_HOME=c:\Program Files\Java\jdk1.8.0_92

4. install Android SDK and set ANDROID_HOME enviromental variable

    for example: ANDROID_HOME=c:\Program Files (x86)\Android\android-sdk\

   Inside Manager install: Android SDK Build-tools 24.0.1 and Android 7.0 (API 24) SDK Platform 

5. in empty directory

    bb bobriln -init

   Shortcut for lazy writters:

    bb n -i

6. start bobril-build in interactive mode

    bb

7. point your browser to [http://localhost:8080/bb](http://localhost:8080/bb)
