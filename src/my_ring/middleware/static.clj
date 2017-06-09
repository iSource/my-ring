(ns my-ring.middleware.static
  (:require [my-ring.middleware.file :as file]
            [clojure.string :as str]))

(defn wrap
  [app public-dir static]
  (fn [req]
    (let [file-app (-> app (file/wrap public-dir))
          uri (:uri req)]
      (if (some #(str/starts-with? uri %) static)
        (file-app req)
        (app req)))))
