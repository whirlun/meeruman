(ns meeruman.http-req
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [cheshire.core :refer :all])
  (:import (com.fasterxml.jackson.core JsonParseException)
           (org.graalvm.polyglot Context)
           (com.fasterxml.jackson.core.io JsonEOFException)
           (meeruman HttpClient BodyType)))

(comment (defn parse-rep [rep]
           (try (generate-string (parse-string rep) {:pretty true})
                (catch JsonParseException rep))
           ))

(def *jsctx
  (let [path-vec (str/split (.getPath (io/resource "beautify.js")) #"/")
        cwd (str "/" (str/join "/" (subvec path-vec 1 (- (count path-vec) 2))))
        options {"js.commonjs-require" "true", "js.commonjs-require-cwd" cwd}]
    (prn cwd)
    (.build (doto (Context/newBuilder (into-array String ["js"]))
              (.allowExperimentalOptions true)
              (.allowIO true)
              (.options options)
              ))))

(def js-beautify (.getMember (.eval *jsctx "js" "require('./resources/beautify.js')") "js_beautify"))

(defn parse-json-req [text]
  (.asString (.execute js-beautify (object-array [text])))
  )

(defn dispatch-req [method url headers body]
  (let [http (HttpClient.)
        rep (case method
              "GET" (.get http url headers)
              "POST" (.post http url headers body BodyType/FORMDATA)
              "PUT" (.put http url headers body BodyType/FORMDATA)
              "PATCH" (.patch http url headers body BodyType/FORMDATA)
              "DELETE" (.delete http url headers body BodyType/FORMDATA)
              "HEAD" (.head http url headers)
              )]
    [
     (parse-json-req (.getFirst rep))
     (.getSecond rep)
     (.getThird rep)
     (.getFourth rep)
     ]
    )
  )