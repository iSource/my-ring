(ns my-ring.utils)

(defn throwf
  [#^String message & args]
  (throw (Exception. #^String
           (apply format (cons message args)))))

(defn throw-if-not
  "throw a formatted exception if confidition is not satisfied."
  [condition message & args]
  (if-not condition
    (apply throwf (cons message args))))

(defn url-decode
  [encoded]
  (java.net.URLDecoder/decode encoded "UTF-8"))

(defn string-include?
  [#^String target #^String string]
  (>= (.indexOf string target) 0))
