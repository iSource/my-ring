(ns my-ring.middleware.reload
  (:require [my-ring.handler.dump :as dump]
            [my-ring.adapter.jetty :as jetty]))

(defn wrap
  [reloadables app]
  (fn [request]
    (doseq [sym reloadables]
      (require sym :reload))
    (app request)))

(defn -main [& args]
  (jetty/run-jetty (->> #'my-ring.handler.dump/app
                        (wrap '(my-ring.handler.dump)))))

#_(defn -main [& args]
    (jetty/run-jetty #'my-ring.handler.dump/app))
