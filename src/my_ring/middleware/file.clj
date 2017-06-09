(ns my-ring.middleware.file
  (:use my-ring.utils)
  (:import (java.io File))
  (:require [my-ring.adapter.jetty :as jetty]))

(defn- ensure-dir
  [#^File dir]
  (throw-if-not (.exists dir)
                "Directory does not exist: %s" dir))


(defn- forbidden
  []
  {:status 403
   :headers {"Content-Type" "text/html"}
   :body "<h1>403 Forbidden</h1>"})


(defn- success
  [#^File file]
  {:status 200
   :headers {}
   :body file})

(defn- maybe-file
  [#^File dir #^String path]
  (let [file (java.io.File. dir path)]
    (and (.exists file) (.canRead file) file)))

(defn app
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "<h1>Not File I'm APP</h1>"})

(defn wrap
  [#^File dir app]
  (ensure-dir dir)
  (fn [request]
    (if (#{:get :head} (:request-method request))
      (let [uri (:uri request)]
        (prn uri)
        (if (string-include? ".." uri)
          (forbidden)
          (let [path (cond
                       (.endsWith "/" uri) (str uri "index.html")
                       (nil? uri) "index.html"
                       :else uri)]
            (if-let [file (maybe-file dir path)]
              (success file)
              (app request)))))
      (app request))))


(defn -main [& args]
  (jetty/run-jetty (->> app (wrap (java.io.File. "resources"))) {:port 3000}))
