(ns distmarkets-service.topics
  (:require [franzy.serialization.serializers :as serializers]
            [franzy.clients.producer.client :as producer]
            [franzy.clients.producer.defaults :as pd]
            [franzy.clients.producer.protocols :refer :all]
            [franzy.clients.consumer.client :as client]
            [franzy.clients.consumer.protocols :as consumer-protocols]
            [franzy.clients.consumer.callbacks :as consumer-callbacks]
            [franzy.clients.consumer.defaults :as consumer-defaults]
            [taoensso.timbre :as log]
            [franzy.serialization.deserializers :as deserializers]
            [clojure.core.async :as async :refer [thread]] 
            [distmarkets-service.conf :as conf]
            [mount.core :as mount :refer [defstate]])
  (:import [franzy.clients.producer.client FranzProducer]
           [franzy.clients.consumer.client FranzConsumer]
           (org.apache.kafka.common.errors WakeupException)
           (java.util.concurrent TimeUnit)))

(defn create-offset-commit-callback
  "Creates an offset commit callback"
  []
  (consumer-callbacks/offset-commit-callback
   (fn [offset-metadata]
     (log/info "Offsets committed!: " offset-metadata))
   (fn [e]
     (log/info "Offsets failed to commit!: " e))))

(defmulti consumer-options
  (fn [topic-name] topic-name))

(defmethod consumer-options
  "data-topic" [_]
  {:bootstrap.servers       (conf/kafka-bootstrap-servers)
   :group.id                (conf/data-topic-consumer-group)
   :client.id               (str (conf/data-topic-name) "-client-" (str (java.util.UUID/randomUUID)))
   :auto.offset.reset       :earliest
   :enable.auto.commit      false
   :auto.commit.interval.ms 6000
   :offset-commit-callback  (create-offset-commit-callback)})

(defmethod consumer-options
  "reciepts-topic" [_]
  {:bootstrap.servers       (conf/kafka-bootstrap-servers)
   :group.id                (conf/reciepts-topic-consumer-group)
   :client.id               (str (conf/reciepts-topic-name) "-client-" (str (java.util.UUID/randomUUID)))
   :auto.offset.reset       :earliest
   :enable.auto.commit      false
   :auto.commit.interval.ms 6000
   :offset-commit-callback  (create-offset-commit-callback)})

(defmethod consumer-options
  "proof-topic" [_]
  {:bootstrap.servers       (conf/kafka-bootstrap-servers)
   :group.id                (conf/proof-topic-consumer-group)
   :client.id               (str (conf/proof-topic-name) "-client-" (str (java.util.UUID/randomUUID)))
   :auto.offset.reset       :earliest
   :enable.auto.commit      false
   :auto.commit.interval.ms 6000
   :offset-commit-callback  (create-offset-commit-callback)})

(defn create-consumer
  "Creates a kafka topic consumer"
  [topic]
  (let [cc      (consumer-options topic)
        options (consumer-defaults/make-default-consumer-options)]
    (client/make-consumer cc
                          (deserializers/string-deserializer)
                          (deserializers/string-deserializer)
                          options)))

(defn process-message
  [message fn]
  (apply fn [message]))

(defn topic-partition-for-record
  "Creates a map of topic partition for a ConsumerRecord
  {:topic \"ds-id\" :partition 102}"
  [message]
  {:topic (.topic message) :partition (.partition message)})

(defn offset-for-record
  "Creates a map of offset from ConsumerRecord
  {:offset 124, :metadata \"uber for spoiled people\"}"
  [message metadata]
  {:offset (.offset message) :metadata metadata})

(defn topic-partition-offset
  "Creates an offset-map for mapr-streams given a message
  where the key is topic partitions and the value is offset
  metadata.
  {{:topic \"ds-id\" :partition 102}
  {:offset 124, :metadata \"uber for spoiled people\"}}"
  [message]
  {(topic-partition-for-record message)
   (offset-for-record message "Meta!!")})

(defn process-messages-with-offsets
  [messages func]
  (reduce (fn [offsets message]
            (process-message message func)
            (merge offsets (topic-partition-offset message)))
          (hash-map)
          messages))

(defn start-consumer
  [topic func]
  (let [consumer (create-consumer topic)]
    (async/thread
      (with-open [c consumer]
        (try
          (consumer-protocols/subscribe-to-partitions! c [topic])
          (while true
            (let [cr          (consumer-protocols/poll! c)
                  msg-batches (partition-all 10 cr)]
              (doseq [msg-batch msg-batches :when (seq msg-batch)]
                (let [offsets (process-messages-with-offsets msg-batch func)]
                  (consumer-protocols/commit-offsets-async! c offsets)))))
          (catch WakeupException we
            (consumer-protocols/commit-offsets-sync! c)
            (consumer-protocols/clear-subscriptions! c)))))
    consumer))

(defstate data-topic-consumer
  :start
  (start-consumer (conf/data-topic-name)
                  (fn [msg]
                    (log/info "Processing data!!!" msg)))
  :stop
  (.close data-topic-consumer))

(defstate reciepts-topic-consumer
  :start
  (start-consumer (conf/reciepts-topic-name)
                  (fn [msg]
                    (log/info "Processing Reciepts!!!" msg)))
  :stop
  (.close reciepts-topic-consumer))

(defstate proof-topic-consumer
  :start
  (start-consumer (conf/proof-topic-name)
                  (fn [msg]
                    (log/info "Processing Proofs!!!" msg)))
  :stop
  (.close proof-topic-consumer))

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
