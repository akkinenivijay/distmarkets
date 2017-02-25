(ns distmarkets-service.conf
  (:require [clojure.string :as string]
            [mount.core :as mount :refer [defstate]]
            [aero.core :as aero] 
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io] 
            [cheshire.core :as json])
  (:import [java.nio.charset Charset]))

(def ^:private #^Charset +charset+
     (Charset/forName "UTF-8"))

(defn bytes-to-string
  "Convert the byte array to string"
  [byte-array]
  (String. byte-array +charset+))

(defn read-config
  "Given a cli options "
  ([]
   (aero/read-config
    (io/resource "config.edn")
    {:profile :dev}))
  ([mount-args]
   (aero/read-config
    (:config mount-args)
    {:profile (:environment mount-args)})))

(defn deep-merge-with
  "Performs a deep merge on `maps` using `func` to resolve conflicts."
  [func & maps]
  (let [par-func (partial deep-merge-with func)]
    (if (every? map? maps)
	(apply merge-with par-func maps)
      (apply func maps))))

(defn merge-config
  [map1 map2]
  (deep-merge-with
   (fn [_ s] s) map1 map2))

(defstate config
  :start
  (let [args (mount/args)
        cfg  (merge-config (read-config args) args)]
    (timbre/debug "Application started with config: " cfg)
    cfg))

(defn environment
  "The platform environment"
  []
  (get-in config [:environment]))

(defn kafka-bootstrap-servers
  "The platform environment"
  []
  (get-in config [:kafka :bootstrap-servers]))

(defn data-topic-name
  "Topic name that holds the data."
  []
  (get-in config [:data-topic :name]))

(defn data-topic-consumer-group
  []
  (get-in config [:data-topic :consumer-group]))

(defn reciepts-topic-name
  "Topic that holds the reciepts."
  []
  (get-in config [:reciepts-topic :name]))

(defn reciepts-topic-consumer-group
  []
  (get-in config [:reciepts-topic :consumer-group]))

(defn proof-topic-name
  "Topic that holds the proofs."
  []
  (get-in config [:proof-topic :name]))

(defn proof-topic-consumer-group
  []
  (get-in config [:proof-topic :consumer-group]))

(defn topic-create-opts
  "The default topic creation options"
  []
  (get-in config [:topics :create.opts]))

(defn topic-producer-opts
  "The default topic producer options"
  []
  (get-in config [:topics :producer.opts]))

(defn redis-host
  []
  (get-in config [:redis :host]))

#_(defn conflux-producer-opts
    "Default options for kafka Producer"
    []
    (merge
     {:type (streams-type)}
     (select-keys (:streams config)
                  [:producer.opts])))
