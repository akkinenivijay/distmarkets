(ns distmarkets-service.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :refer [join lower-case]]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.core.async :refer [<!! chan]]
            [taoensso.timbre :as timbre]
            [mount.core :as mount]
            [aero.core :as aero]
            [distmarkets-service.conf :as conf]
            [distmarkets-service.api-routes :as api-routes]
            [distmarkets-service.redis :as redis]
            [distmarkets-service.tierion :as tierion]
            [distmarkets-service.topics :as topics])
  (:gen-class))

(defn- usage
  "Returns usage information string"
  [options-summary]
  (join
   \newline
   [""
    "Distributed Markets API: Woosh, Woosh."
    ""
    "Usage:"
    "       With Jar  : java -jar distmarkets-service.jar [options]" 
    ""
    "Options:"
    options-summary
    ""]))

(defn- exit!
  "Exits with given status and prints the given message"
  [status msg]
  (println msg)
  (System/exit status))

(defn- error-msg
  "Returns an error message string"
  [errors]
  (str "Invalid command line arguments!!  Error(s):\n   "
       (join (str "   " \newline) errors)))

(def ^:private cli-options
  [["-c" "--config CONFIG_FILE" "Aero/EDN config file"
    :default (io/resource "config.edn")
    :default-desc "config.edn"
    ;; Load from class path or expect that user has provided an absolute path to external file.
    :parse-fn #(or (io/resource %) (io/as-url (io/file  %)))
    :validate [#(.exists (io/file %)) "Config file does not exist"
               aero/read-config "Not a valid Aero or EDN file"]]

   ["-e" "--environment ENV" "Envrionment to run in."
    :default :dev
    :parse-fn #(-> % lower-case keyword)
    :validate [#(some #{%} [:aws :dev])
               "Should be aws for prod"]]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)
        shutdown-chan (chan)]
    (cond
      (:help options)
      (exit! 0 (usage summary))

      errors
      (exit! 1 (error-msg errors))

      :default
      (let [_ (timbre/info "Starting API with command line options " options)
            states (-> options
                       mount/with-args
                       mount/start)]
        (timbre/info "Started with states " states)
        ;; Add a shutdown hook
        (.addShutdownHook
         (Runtime/getRuntime)
         (Thread.
          ^Runnable (fn [] (mount/stop))))
        ;; <!! is a function that takes a value from the `shutdown-chan`, blocking until it recieves a value.
        (<!! shutdown-chan)
        (mount/stop)))))


