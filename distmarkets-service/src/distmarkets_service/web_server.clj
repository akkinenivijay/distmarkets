(ns distmarkets-service.web-server
  (:require
   [bidi.bidi :refer [tag]]
   [bidi.vhosts :refer [make-handler vhosts-model]]
   [yada.resources.webjar-resource :refer [new-webjar-resource]]
   [yada.yada :refer [handler resource] :as yada]
   [aleph.netty]
   [mount.core :as mount :refer [defstate]] 
   [taoensso.timbre :as timbre]
   [distmarkets-service.api-routes :refer [api-routes]]
   [distmarkets-service.conf :as conf])
  (:import java.io.Closeable
           [java.io File FileInputStream]))

(defn routes
  "Create the URI route structure for our application."
  []
  [""
   [(api-routes)
    ["/swagger" (-> (new-webjar-resource "/swagger-ui" {:index-files ["index.html"]})
                    (tag :empi.resources/swagger-ui))]
    ;; This is a backstop. Always produce a 404 if we ge there. This
    ;; ensures we never pass nil back to Aleph.
    [true (handler nil)]]])

(defn create-listener
  "Create Web Server."
  []
  (let [routes        (routes)
        aleph-options {:port (get-in conf/config [:http-kit :port])}]
    (timbre/info "Web server started!")
    (yada/listener routes aleph-options)))

(defn stop
  "Stop Web Server"
  [http-server]
  ((:close http-server)))

(defstate http-server
  :start (do 
           (create-listener))
  :stop (stop http-server))
