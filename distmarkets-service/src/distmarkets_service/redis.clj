(ns distmarkets-service.redis
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def redis-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})

(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(defn set-inspectionid
  [id]
  (wcar*
   (car/hmset id "INSPECTION_ID" id)))

(defn set-receiptid
  [id receiptid]
  (wcar*
   (car/hmset id "receiptid" receiptid)))

(defn set-proof
  [id proof]
  (wcar*
   (car/hmset id "final-proof" proof)))

(defn set-spatial-data
  [id long lat]
  (wcar*
   (car/geoadd "iotspatialdata" long lat id)))
