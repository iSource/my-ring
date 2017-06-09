(ns my-ring.middleware.params-test
  (:use clojure.test my-ring.middleware.params)
  (:require [clojure.java.io :as io]))

(def body (io/input-stream (.getBytes "woo=gua&ka=%E6%88%91%E7%9A%84")))
(def body-override (io/input-stream (.getBytes "woo=gua&ka=%E6%88%91%E7%9A%84")))

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

(deftest warp-params-override-test
  (is (= {:query-string "foo=bar&foo=%e9%a9%ac%e5%bb%ba%e5%bb%ba"
          :content-type "application/x-www-form-urlencoded"
          :body body-override
          :query-params {"foo" "马建建"}
          :form-params {"woo" "gua", "ka" "我的"}
          :params {"foo" "马建建", "woo" "gua", "ka" "我的"}}
         ((wrap-params true identity) {:query-string "foo=bar&foo=%e9%a9%ac%e5%bb%ba%e5%bb%ba"
                                  :content-type "application/x-www-form-urlencoded"
                                  :body body-override}))))
