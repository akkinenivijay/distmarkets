(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [clojure.pprint :refer [pprint]]
            [clojure.test :refer [run-all-tests]]
            [clojure.repl :refer [apropos dir doc find-doc pst source]]
            [clojure.java.io :as io]
            [mount.core :as mount]
            [distmarkets-service.conf :as conf]
            [distmarkets-service.topics :as topics]
            [distmarkets-service.web-server :as web-server]
            [taoensso.timbre :as timbre]))

(def +states+
  "The default environment. You _most-often_ want to use this for
  development. Start the system with `(start +states+)`.
  "
  {:swap-states {}

   :only
   #{#'conf/config
     #'web-server/http-server
     #'topics/producer
     #'topics/data-topic-consumer
     #'topics/reciepts-topic-consumer
     #'topics/proof-topic-consumer}

   :config
   {:environment :dev
    :config (io/resource "config.edn")}})

(defn start
  "Starts the current development system."
  ([] (start +states+))
  ([{:keys [swap-states only config]
     :or  {swap-states {} only #{} config {}}}]
   (timbre/info "Starting distribtued markets service")
   (let [states (-> (mount/swap-states swap-states)
                    (mount/with-args config)
                    (mount/only only)
                    mount/start)]
     (timbre/info "started with states: " states)
     :started)))

(defn stop
  "Shuts down and destroys distmarkets-service system."
  []
  (timbre/info "Stopping distmarkets-service"))
(mount/stop)

(defn go
  "Initializes the current development system and starts it running."
  []
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn test-all []
  (run-all-tests #"distmarkets-service.*test$"))

(defn reset-and-test
  []
  (reset)
  (time (test-all)))
