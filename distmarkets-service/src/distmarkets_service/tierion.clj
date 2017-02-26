(ns distmarkets-service.tierion
  (:require [clj-http.client :as client]
            [cheshire.core :as cheshire-core]
            [digest :as digest]
            [mount.core :as mount :refer [defstate]]))

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

;;(submit-hashitem (cheshire-core/generate-string testdata))



