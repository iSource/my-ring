(ns my-ring.adapter.jetty
  (:import (org.eclipse.jetty.server Server Request)
           (org.eclipse.jetty.server.handler AbstractHandler)
           (javax.servlet.http HttpServletRequest HttpServletResponse))
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
  (let [#^Integer port (options :port 3000)]
    (Server. port)))

(defn- proxy-handler
  [handler]
  (proxy [AbstractHandler] []
    (handle [target #^Request base-request #^HttpServletRequest request #^HttpServletResponse response]
      (let [req-map (build-request-map request)
            resp-map (handler req-map)]
        (update-servlet-response response resp-map)
        (.setHandled base-request true)))))

(defn run-jetty
  ([app options]
   (let [#^Server s (create-server (dissoc options :configurator))]
     (when-let [configurator (:configurator options)]
       (configurator s))
     (doto s
       (.setHandler (proxy-handler app))
       (.start))

     (when (:jon options true)
       (.join s))
     s))
  ([app]
   (run-jetty app {})))

(defn app
  [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body '(:foo 1 \a [:bar 2] {:x 1 :y 2})})

(defn -main
  [& args]
  (run-jetty app {:port 3000 :host "10.1.2.3"}))
