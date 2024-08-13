(defproject meeruman "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.cache "1.0.225"]
                 [com.formdev/flatlaf "3.4"]
                 [com.formdev/flatlaf-intellij-themes "3.4"]
                 [seesaw "1.5.0"]
                 [com.fifesoft/rsyntaxtextarea "3.4.0"]
                 [cheshire "5.12.0"]
                 [com.github.clj-easy/graal-build-time "1.0.5"]
                 [org.clojure/core.async "1.6.681"]
                 [org.apache.xmlgraphics/batik-transcoder "1.17"]
                 [com.github.seancorfield/next.jdbc "1.3.909"]
                 [honeysql "1.0.461"]
                 [com.h2database/h2 "2.2.220"]
                 [com.squareup.okhttp3/okhttp "4.12.0"]
                 [org.jetbrains.kotlin/kotlin-stdlib "1.9.23"]
                 ]
  :repl-options {:init-ns meeruman.core}
  :prep-tasks [["javac"] ["kotlinc"] ["compile"]]
  :jvm-opts ["-Dfile.encoding=UTF-8"]
  :resource-paths ["resources/"]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :plugins [[lein-exec "0.3.7"]]
  :aliases {"kotlinc" ["exec", "-ep", "(require '[clojure.java.shell :refer [sh]]) (sh \"kotlinc\" \"-cp\" (System/getProperty \"java.class.path\") \"-d\" \"target/classes\" \"src/java/meeruman\") (System/exit 0)"]}
  :profiles {:uberjar {:aot :all}}
  :main meeruman.core)