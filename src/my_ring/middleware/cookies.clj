(ns my-ring.middleware.cookies
  (:require [clojure.string :as str]))

(def re-token #"[!#$%\^&*_\-+~`.'\|a-zA-Z0-9]+")

(def re-quoted #"\"(\\\"|[^\"])*\"")

(def re-value (str re-token "|" re-quoted))

(def re-cookie
  (re-pattern (str "\\s*(" re-token ")=(" re-value ")\\s*[;,]?")))

(def cookie-attrs {"$Path" :path "$Domain" :domain "$Port" :port})

(def set-cookie-attrs
  {:comment "Comment" :comment-url "CommentURL" :discard "Discard"
   :domain "Domain" :max-age "Max-Age" :path "Path" :port "Port"
   :secure "Secure" :version "Version"})


(defn parse-cookie-header
  "[[name value] [name value] ...]"
  [header]
  (for [[_ name value] (re-seq re-cookie header)]
    [name value]))

(defn normalize-quoted-strs
  [cookies]
  (for [[name value] cookies]
    (if (str/starts-with? #^String value "\"")
      [name (read-string value)]
      [name value])))

(defn get-cookie
  [[[name value] & cookie-values]]
  {name (reduce
                (fn [m [k v]] (assoc m (cookie-attrs k) v))
                {:value value}
                (take-while (comp cookie-attrs first) cookie-values))})

(defn to-cookie-map
  [values]
  (loop [values values cookie-map {}]
    (if (seq values)
      (let [cookie (get-cookie values)]
        (recur 
               (drop (-> cookie first val count) values)
               (merge cookie-map cookie)))
      cookie-map)))

(defn parse-cookies
  [request]
  (if-let [cookie (get-in request [:headers "cookie"])]
    (-> cookie
        parse-cookie-header
        normalize-quoted-strs
        to-cookie-map
        (dissoc "$Version"))))


(defn- write-attr
  [name value]
  (str name "=" (pr-str value)))

(defn- write-attr-map
  [attrs]
  (for [[key value] attrs]
    (let [name (set-cookie-attrs key)]
      (cond
            (true? value) (str ";" name)
            (false? value) ""
            :else (str ";" (write-attr name value))))))

(defn- write-cookies 
  [cookies]
  (for [[name value] cookies]
    (if (map? value)
      (apply str (write-attr name (:value value))
             (write-attr-map (dissoc value :value)))
      (write-attr name value))))

(defn- set-cookies
  [response]
  (if-let [cookies (:cookies response)]
    (assoc-in response
              [:headers "Set-Cookie"]
              (write-cookies cookies))
    response))

(defn wrap-cookies
  [handler]
  (fn [request]
    (let [request (assoc request :cookies (parse-cookies request))
          response (handler request)]
      (-> response
          set-cookies
          (dissoc :cookies)))))
