(ns my-ring.middleware.params-test
  (:use clojure.test my-ring.middleware.params)
  (:require [clojure.java.io :as io]))

(def body (io/input-stream (.getBytes "woo=gua&ka=%E6%88%91%E7%9A%84")))
(def body-2 (io/input-stream (.getBytes "woo=gua&ka=%E6%88%91%E7%9A%84")))
(def body-3 (io/input-stream (.getBytes "woo=gua&ka=%E6%88%91%E7%9A%84")))

(deftest warp-params-test
  (is (= {:query-string "foo=bar&foo=%e9%a9%ac%e5%bb%ba%e5%bb%ba"
          :content-type "application/x-www-form-urlencoded"
          :body body
          :query-params {"foo" ["bar" "马建建"]}
          :form-params {"woo" "gua", "ka" "我的"}
          :params {"foo" ["bar" "马建建"], "woo" "gua", "ka" "我的"}}
         ((wrap-params identity) {:query-string "foo=bar&foo=%e9%a9%ac%e5%bb%ba%e5%bb%ba"
                                  :content-type "application/x-www-form-urlencoded"
                                  :body body}))))

(deftest warp-params-2-test
  (is (= {:query-string "foo=bar&foo=%e9%a9%ac%e5%bb%ba%e5%bb%ba"
          :content-type "application/x-www-form-urlencoded"
          :body body-2
          :query-params {"foo" ["bar" "马建建"]}
          :form-params {"woo" "gua", "ka" "我的"}
          :params {"foo" ["bar" "马建建"], "woo" "gua", "ka" "我的"}}
         ((wrap-params identity "UTF-8") {:query-string "foo=bar&foo=%e9%a9%ac%e5%bb%ba%e5%bb%ba"
                                  :content-type "application/x-www-form-urlencoded"
                                  :body body-2}))))


(deftest warp-params-3-test
  (is (= {:query-string "foo=bar&foo=%e9%a9%ac%e5%bb%ba%e5%bb%ba"
          :content-type "application/x-www-form-urlencoded"
          :body body-3
          :query-params {"foo" "马建建"}
          :form-params {"woo" "gua", "ka" "我的"}
          :params {"foo" "马建建", "woo" "gua", "ka" "我的"}}
         ((wrap-params identity "UTF-8" true) {:query-string "foo=bar&foo=%e9%a9%ac%e5%bb%ba%e5%bb%ba"
                                  :content-type "application/x-www-form-urlencoded"
                                  :body body-3}))))
