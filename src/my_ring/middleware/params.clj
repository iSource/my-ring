(ns my-ring.middleware.params
  (:require [clojure.string :as str])
  (:import java.net.URLDecoder))

(defn- url-decode
  [encoded encoding]
  (URLDecoder/decode encoded encoding))

(defn- assoc-vec
  [params-map param]
  (merge-with (fn [to from]
                (cond
                  (vector? to)
                  (conj to from)
                  :else
                  (vector to from)))
              params-map param))

(defn- parse-params
  [#^String params-string encoding override]
  (reduce (fn [params-map param-pair]
            (let [[_ key value] (re-matches #"([^=]+)=(.*)" param-pair)]
              (let [decoded-key (url-decode (or key "") encoding)
                    decoded-value (url-decode (or value "") encoding)]
                (if override
                  (assoc params-map decoded-key decoded-value)
                  (assoc-vec params-map {decoded-key decoded-value})))))
          {}
          (str/split params-string #"&")))


(defn- assoc-query-params
  [request encoding override]
  (if-let [#^String query-string (:query-string request)]
    (let [params (parse-params query-string encoding override)]
      (merge-with merge request {:query-params params :params params}))
    request))

(defn- urlencoded-form?
  [request]
  (if-let [content-type (:content-type request)]
    (str/starts-with? content-type "application/x-www-form-urlencoded")))

(defn- assoc-form-params
  [request encoding override]
  (if-let [body (and (urlencoded-form? request) (:body request))]
    (let [params (parse-params (slurp body) encoding override)]
      (merge-with merge request {:form-params params :params params}))
    request))


(defn wrap-params
  ([handler encoding override]
   (fn [req]
     (-> req
         (assoc-query-params encoding override)
         (assoc-form-params encoding override)
         handler)))
  ([handler encoding]
   (wrap-params handler encoding false))
  ([handler]
   (wrap-params handler "UTF-8" false)))
