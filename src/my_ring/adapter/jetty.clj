(ns my-ring.adapter.jetty
  (:import (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.server.handler AbstractHandler))
  (:use my-ring.util.servlet))

(defn- proxy-handler
  [handler]
  (proxy [AbstractHandler] []
    (handle [target base-request request response]
      (let [req-map (build-request-map request)
            resp-map (handler req-map)]
        (update-servlet-response response resp-map)
        (.setHandled request true)))))

(defn run-jetty
  ([app options]
    (let [port (or (:port options) 3000)
          server (Server. port)
          handler (proxy-handler app)]
      (.setHandler server handler)
      (.start server)
      (.join server)))
  ([app]
   (run-jetty app {})))

(defn app
  [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body '(:foo 1 \a [:bar 2] {:x 1 :y 2})})

(defn -main
  [& args]
  (run-jetty app {:port 3000}))
