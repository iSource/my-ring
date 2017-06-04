(ns my-ring.adapter.jetty
  (:import (org.eclipse.jetty.server Server Request)
           (org.eclipse.jetty.server.handler AbstractHandler)
           (javax.servlet.http HttpServletRequest HttpServletResponse)
           (java.io File FileInputStream InputStream OutputStream)
           (org.apache.commons.io IOUtils)))

(defn- build-req-map
  [#^HttpServletRequest request]
  {:server-port (.getServerPort request)
   :server-name (.getServerName request)
   :remote-addr (.getRemoteAddr request)
   :uri (.getRequestURI request)
   :query-string (.getQueryString request)
   :schema (keyword (.getScheme request))
   :request-method (keyword (.toLowerCase (.getMethod request)))
   :content-type (.getContentType request)
   :content-length (let [len (.getContentLength request)]
                     (if (>= len 0) len))
   :character-encoding (.getCharacterEncoding request)
   :body (.getInputStream request)})

(defn- apply-response-map
  [#^HttpServletResponse response {:keys [status headers body]}]
  (.setStatus response status)
  (doseq [[key val-or-vals] headers]
    (if (string? val-or-vals)
      (.setHeader response key val-or-vals)
      (doseq [val val-or-vals]
        (.addHeader response key val))))
  (when-let [content-type (get headers "Content-Type")]
    (.setContentType response content-type))
  (cond
    (string? body)
    (with-open [writer (.getWriter response)]
      (.println writer body))
    (instance? InputStream body)
    (let [#^InputStream in body]
      (with-open [out (.getOutputStream response)]
        (IOUtils/copy in out)
        (.close in)
        (.flush out)))
    (seq? body)
    (with-open [writer (.getWriter response)]
      (doseq [chunk body]
        (.print writer (str chunk))))
    (instance? File body)
    (let [#^File f body]
      (with-open [fin (FileInputStream. f)]
        (with-open [out (.getOutputStream response)]
          (IOUtils/copy fin out)
          (.flush out))))
    (nil? body)
    nil
    :else
    (throw (Exception. "Unreceognized body"))))

(defn- proxy-handler
  [app]
  (proxy [AbstractHandler] []
    (handle [target base-request request response]
      (let [req (build-req-map request)
            resp (app req)]
        (apply-response-map response resp)
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
