(ns my-ring.util.servlet-test
  (:require [ring.mock.request :as mock])
  (:use my-ring.util.servlet clojure.test))

(deftest get-headers-test
  (is {} (get-headers (mock/request :get "/"))))
