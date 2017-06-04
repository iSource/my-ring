(ns my-ring.middleware.lint
  (:require [my-ring.adapter.jetty :as jetty]))

(defn throwf [& args]
  (throw (Exception. (apply format args))))

(defn lint [val spec message]
  (try
    (if-not (spec val)
      (throwf "Ring lint error: specified %s, but %s was not" message (pr-str val)))
    (catch Exception e
      (if-not (re-find #"^Ring lint error:" (.getMessage e))
        (throwf "Ring lint error: exception occured when checking that %s on %s: %s"
                message
                (pr-str val)
                (.getMessage e))
        (throw e)))))

(defn check-resp [resp]
  (lint resp map? "Ring response must be a Clojure map")
  (lint (:status resp) #(and (integer? %) (> % 100)) ":status must be an Integer greater than or equal to 100")
  (let [headers (:headers resp)]
    (lint headers map? ":headers must be a Clojure map")
    (doseq [[header-key header-value] headers]
      (lint header-key string? "header names must Strings")
      (lint header-value #(or (string? %) (every? string? %)) "header values must be Strings or colls of String?"))))

(defn wrap [app]
  (fn [request]
    (let [resp (app request)]
      (check-resp resp)
      resp)))

(defn app [request]
  {:status 200
   :body "<h1>Good</h1>"})

(defn -main [& args]
  (jetty/run-jetty (-> app wrap)))
