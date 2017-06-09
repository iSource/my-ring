(ns my-ring.middleware.reload
  (:require [my-ring.handler.dump :as dump]
            [my-ring.adapter.jetty :as jetty]))

(defn wrap
  [app reloadables]
  (fn [request]
    (doseq [sym reloadables]
      (require sym :reload))
    (app request)))
