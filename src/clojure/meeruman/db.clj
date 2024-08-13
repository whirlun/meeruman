(ns meeruman.db
  (:require [honeysql.core :as sql]
            [honeysql.helpers :refer :all :as helpers]
            [next.jdbc :as jdbc])
  )

(def db {:dbtype "h2" :dbname "test"})

