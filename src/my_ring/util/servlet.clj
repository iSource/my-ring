(ns my-ring.util.servlet
  (:require [clojure.string :as str]
            [my-ring.utils :as util])
  (:import (javax.servlet.http HttpServlet HttpServletRequest HttpServletResponse)
           (org.apache.commons.io IOUtils)
           (java.io File InputStream FileInputStream)))

(defn- get-headers
  [#^HttpServletRequest request]
  (reduce
          (fn [headers name]
            (assoc headers
                   (str/lower-case name)
                   (.getHeader request name)))
          {}
          (enumeration-seq (.getHeaderNames request))))

(defn- get-content-length
  [#^HttpServletRequest request]
  (let [length (.getContentLength request)]
    (if (>= length 0) length)))

(defn build-request-map
  [#^HttpServletRequest request]
  {:server-port (.getServerPort request)
   :server-name (.getServerName request)
   :remote-addr (.getRemoteAddr request)
   :uri (.getRequestURI request)
   :query-string (.getQueryString request)
   :scheme (keyword (.getScheme request))
   :request-method (keyword (str/lower-case (.getMethod request)))
   :headers (get-headers request)
   :content-length (get-content-length request)
   :content-type (.getContentType request)
   :character-encoding (.getCharacterEncoding request)
   :body (.getInputStream request)})

(defn- set-headers
  [#^HttpServletResponse response headers]
  (doseq [[key val-or-vals] headers]
    (if (string? val-or-vals)
      (.setHeader response key val-or-vals)
      (doseq [val val-or-vals]
        (.addHeader response key val))))
  (when-let [content-type (get headers "content-type")]
    (.setContentType response content-type)))

(defn- set-body
  [#^HttpServletResponse response body]
  (cond
        (string? body)
        (with-open [writer (.getWriter response)]
          (.println writer body))
        (seq? body)
        (with-open [writer (.getWriter response)]
          (doseq [chunk body]
            (.print writer (str chunk))
            (.flush writer)))
        (instance? InputStream body)
        (with-open [out (.getOutputStream response)]
          (IOUtils/copy #^InputStream body out)
          (.close #^InputStream body)
          (.flush out))
        (instance? File body)
        (with-open [stream (FileInputStream. #^File body)]
          (set-body response stream))
        (nil? body)
        nil
        :else
        (util/throwf "Unknow body: %s" body)))

(defn update-servlet-response
  [#^HttpServletResponse response {:keys [status headers body]}]
  (doto response
    (.setStatus status)
    (set-headers headers)
    (set-body body)))
