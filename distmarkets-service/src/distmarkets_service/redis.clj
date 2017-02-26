(ns distmarkets-service.redis
  (:require [taoensso.carmine :as car :refer (wcar)]
            [cheshire.core :as cheshire-core]))

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

(defn get-data-info
  [id]
  (let [[first second third] (wcar*
                              (car/hmget id "final-proof" "INSPECTION_ID" "receiptid"))]
    [(cheshire-core/parse-string first true) second third]))

(defn geo-radius-query
  []
  (for [item (wcar*
              (car/georadius "iotspatialdata" 0.0 0.0 18000 "km"))]
    (let [posdata (wcar*
                   (car/geopos "iotspatialdata" item))
          data-info (get-data-info item)]
      (into [data-info] (first posdata)))))
