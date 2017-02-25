(ns distmarkets-service.topics
  (:require [kafka-clj.client :as client]
            [distmarkets-service.conf :as conf]
            [mount.core :as mount :refer [defstate]]))

(defstate connector
  :start (client/create-connector [(conf/kafka-cfg)] {:flush-on-write true}))

(defn put
  [topic message]
  (println "Sending to topic: " topic) 
  (client/send-msg connector topic message))
