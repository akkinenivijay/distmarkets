(ns distmarkets-service.api-routes
  "This name space will contain bidi route definitions for api endpoints."
  (:require [bidi.bidi :as bidi]
            [yada.yada :as yada]
            [cheshire.core :as cheshire-core]
            [distmarkets-service.topics :as topics]
            [yada.swagger :as swagger]))

(defn capture-data
  [ctx] 
  (topics/put "data-topic" (.getBytes (cheshire-core/generate-string (:body ctx))))
  {:status :success})

(defn data-resource
  "yada resource for posting data"
  []
  (yada/resource
   {:id          :distmarkets-service/data
    :description "Endpoint to post data."
    :consumes    "application/json"
    :produces    "application/json"
    :methods     {:post {:response capture-data}}}))

(defn api-routes []
  "Defines the distmarkets service rest API paths following the bidi syntax" 
  (let [routes
        ["/distmarkets"
         [["/data" (data-resource)]]] 
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
