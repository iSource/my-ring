(ns my-ring.adapter.jetty
  (:import (org.eclipse.jetty.server Server)
           (org.eclipse.jetty.server.handler AbstractHandler))
  (:use my-ring.util.servlet))

#_(defn- add-ssl-connector!
  [server options]
  (let [ssl-connector (SslSocketConnector.)]
    (doto ssl-connector
      (.setPort (options :ssl-port 443))
      (.setKeystore (options :keystore))
      (.setKeyPassword (options :key-password)))
    (when-let [truststroe (options :truststroe)]
      (.setTrustStroe ssl-connector truststore))
    (when-let [trust-password (options :trust-password)]
      (.setTrustPassword ssl-connector trust-password))))

(defn- create-server
  [options]
  (let [port (options :port 3000)]
    (doto
      (Server. port))))

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
    (doto (create-server options)
      (.setHandler (proxy-handler app))
      (.start)
      (.join)))
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
