(ns distmarkets-service.topics
  (:require [franzy.serialization.serializers :as serializers]
            [franzy.clients.producer.client :as producer]
            [franzy.clients.producer.defaults :as pd]
            [franzy.clients.producer.protocols :refer :all]
            [clojure.core.async :as core-async]
            [distmarkets-service.conf :as conf]
            [mount.core :as mount :refer [defstate]]))


(defstate producer
  :start (let [pc               {:bootstrap.servers ["127.0.0.1:9092"]
                                 :acks              "all"
                                 :retries           0
                                 :batch.size        16384
                                 :linger.ms         10
                                 :buffer.memory     33554432}
               key-serializer   (serializers/string-serializer)
               value-serializer (serializers/string-serializer)
               options          (pd/make-default-producer-options)]
           (producer/make-producer pc key-serializer value-serializer options))
  :stop (.close producer))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn put
  [topic message]
  (let [send-fut (send-async! producer {:topic topic :key (uuid) :value message})]
    (println "Async send results:" @send-fut)))
