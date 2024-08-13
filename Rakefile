task :build_jar do
    system "lein", "do", "clean"
    system "lein", "javac"
    cpjars = `lein exec -ep '(prn (System/getProperty "java.class.path"))'`
    system "kotlinc", "-cp", cpjars, "-d", "target/classes", "src/java/meeruman"
    system "lein", "uberjar"
    system "rm", "-rf", "package"
    system "mkdir", "package"
    jar =  Dir["./target/*-standalone.jar"][0]
    system "cp", jar, "./package"
    system "cp", "-r", "./resources", "./package"
    included_modules = %w(
        java.base
        java.desktop
        java.naming
        java.prefs
        java.sql
        jdk.unsupported
        java.management
        jdk.management
        java.logging
        jdk.internal.vm.ci
        jdk.internal.vm.compiler
        java.scripting
        java.net.http
        org.graalvm.sdk
        org.graalvm.js
    )
    system "jpackage", "--input", "./package",
    "--name", "meeruman",
    "--main-jar", jar.split("/")[2],
    "--main-class", "meeruman.core",
    "--add-modules", included_modules.join(","),
    "--module-path", "./jars",
    "--type", "dmg"
end