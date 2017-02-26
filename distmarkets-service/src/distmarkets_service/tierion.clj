(ns distmarkets-service.tierion
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire-core]
            [digest :as digest]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :as log])
  (:use [slingshot.slingshot :only [throw+ try+]]
        [while-let.core :refer [while-let]]))

(def auth-data-map
  {:username "mtidwell021@gmail.com"
   :password "hackathon"})

(defn auth-token
  []
  (let [response (try
                   (client/post "https://hashapi.tierion.com/v1/auth/token"
                            {:body         (cheshire-core/generate-string auth-data-map)
                             :content-type :json
                             :accept       :json})
                   (catch Exception e {}))]
    (:access_token (cheshire-core/parse-string (:body response) true))))

(defstate authtoken
  :start (auth-token))

(defn submit-hashitem
  [data]
  (let [response (client/post "https://hashapi.tierion.com/v1/hashitems"
                              {:body         (cheshire-core/generate-string {:hash (digest/sha-256 data)})
                               :headers      {"Authorization" (str "Bearer " authtoken)}
                               :content-type :json
                               :accept       :json})]
    (cheshire-core/parse-string (:body response) true)))

(defn get-receipt
  [receiptid]
  (try+
   (cheshire-core/parse-string (:body (client/get (str "https://hashapi.tierion.com/v1/receipts/" receiptid)
                                                  {:headers {"Authorization" (str "Bearer " authtoken)}
                                                   :accept  :json})) true)
   (catch [:status 409] {:keys [request-time headers body]}
     (log/warn "409" request-time headers)
     {:status :not-ready})
   (catch [:status 404] {:keys [request-time headers body]}
     (log/warn "404" request-time headers)
     {:status :not-ready})
   (catch Object _
     (log/error (:throwable &throw-context) "unexpected error")
     {:status :not-ready})))

(defn retry-get-proof-indefinitely
  [receiptid dataid]
  (let [respone (get-receipt receiptid)]
    (if (= (:status respone) :not-ready)
      (do (Thread/sleep 10000)
          (println "procesing receiptid & dataid: " receiptid dataid)
          (retry-get-proof-indefinitely receiptid dataid))
      {:INSPECTION_ID dataid :receiptId receiptid :final-proof (cheshire-core/parse-string (:receipt respone) true)})))
