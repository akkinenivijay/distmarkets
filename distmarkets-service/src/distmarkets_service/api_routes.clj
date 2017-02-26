(ns distmarkets-service.api-routes
  "This name space will contain bidi route definitions for api endpoints."
  (:require [bidi.bidi :as bidi]
            [yada.yada :as yada]
            [cheshire.core :as cheshire-core]
            [distmarkets-service.topics :as topics]
            [distmarkets-service.redis :as redis]
            [yada.swagger :as swagger]))

(defn capture-data
  [ctx] 
  (topics/put "data-topic" (cheshire-core/generate-string (:body ctx)))
  {:status :success})

(defn geo-query
  [ctx]
  (redis/geo-radius-query))

(defn id-data
  [ctx]
  (println ctx)
  (redis/get-data-info (get-in ctx [:request :parameters :path :id])))

(defn data-resource
  "yada resource for posting data"
  []
  (yada/resource
   {:id          :distmarkets-service/data
    :description "Endpoint to post data."
    :consumes    "application/json"
    :produces    "application/json"
    :methods     {:post {:response capture-data}}}))

(defn geo-resource
  "yada resource for geo query"
  []
  (yada/resource
   {:id          :distmarkets-service/geo
    :description "Endpoint to geo query."
    :consumes    "application/json"
    :produces    "application/json"
    :methods     {:get {:response geo-query}}}))

(defn id-resource
  "yada resource for posting data"
  []
  (yada/resource
   {:id          :distmarkets-service/id
    :description "Endpoint to get id."
    :parameters  {:path {:id String}}
    :consumes    "application/json"
    :produces    "application/json"
    :methods     {:get {:response id-data}}}))

(defn api-routes []
  "Defines the distmarkets service rest API paths following the bidi syntax" 
  (let [routes
        ["/distmarkets"
         [["/data" (data-resource)]
          ["/geo" (geo-resource)]
          ["/" :id (id-resource)]]] 
        swagger-setup
        ["/distmarkets/swagger.json"
         (bidi/tag
          (yada/handler
           (swagger/swagger-spec-resource
            (swagger/swagger-spec
             routes
             {:info     {:title       "DistMarkets API"
                         :version     "1"
                         :description "Distmarkets IOT API Hackathon."}
              :schemes  ["http"]
              :basePath ""})))
          :distmarkets.resources/swagger)]]
    ["" [routes swagger-setup]]))
