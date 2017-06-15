(ns my-ring.example.wrapping
  (:require [my-ring.middleware file file-info stacktrace reload params]
            [my-ring.handler dump]
            [my-ring.adapter jetty])
  (:import java.io.File))

(def app
  (-> #'my-ring.handler.dump/app
       (my-ring.middleware.reload/wrap '(my-ring.handler.dump))
       (my-ring.middleware.params/wrap-params)
       (my-ring.middleware.file/wrap (java.io.File. "resources"))
       my-ring.middleware.file-info/wrap
       my-ring.middleware.stacktrace/wrap))

(defn- jetty-configurator
  [server]
  (println "**********************")
  (println "* Jetty Server Start *")
  (println "**********************"))

(defn -main [& args]
  (my-ring.adapter.jetty/run-jetty app {:configurator jetty-configurator}))
