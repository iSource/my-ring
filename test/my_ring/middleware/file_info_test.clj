(ns my-ring.middleware.file-info-test
  (:require [my-ring.middleware.file-info :refer :all]
            [clojure.test :refer :all])
  (:import (java.io File)))

(def non-file-app (wrap (constantly {:headers {} :body "body"})))

(def known-file (File. "resources/index.html"))
(def known-file-app (wrap (constantly {:headers {} :body known-file})))

(def unknown-file (File. "resources/random.www"))
(def unknown-file-app (wrap (constantly {:headers {} :body unknown-file})))

(def unknown-file-app-with-custom (wrap (constantly {:headers {} :body unknown-file}) {"www" "text/mytype"}))

(deftest non-file-test
  (is (= {:headers {} :body "body"} (non-file-app {}))))

(deftest known-file-test
  (is (= {:headers {"Content-Type" "text/html" "Content-Length" "44"} :body known-file}
         (known-file-app {}))))

(deftest unknown-file-test
  (is (= {:headers {"Content-Type" "application/octet-stream" "Content-Length" "0"} :body unknown-file}
         (unknown-file-app {}))))

(deftest unknown-file-with-custom-test
  (is (= {:headers {"Content-Type" "text/mytype" "Content-Length" "0"} :body unknown-file}
         (unknown-file-app-with-custom {}))))
