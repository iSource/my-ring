(ns my-ring.middleware.cookies-test
  (:use clojure.test
        my-ring.middleware.cookies))

(deftest cookie-test-1
  (let [req {:headers {"cookie" "a=b"}}
        resp ((wrap-cookies :cookies) req)]
    (is (= {"a" {:value "b"}} resp))))

(deftest cookie-test-2
  (let [req  {:headers {"cookie" "a=b; c=d,e=f"}}
        resp ((wrap-cookies :cookies) req)]
    (is (= {"a" {:value "b"}, "c" {:value "d"}, "e" {:value "f"}}
             resp))))

(deftest cookie-test-3
  (let [req {:headers {"cookie" "a=\"b=c;e=f\""}}
        resp ((wrap-cookies :cookies) req)]
    (is (= {"a" {:value "b=c;e=f"}} resp))))

(deftest cookie-test-4
  (let [req {:headers {"cookie" "a=\"\\\"b\\\"\""}}
        resp ((wrap-cookies :cookies) req)]
    (is (= {"a" {:value "\"b\""}} resp))))

(deftest cookie-test-5
  (let [req {:headers {"cookie" "a=b;$Path=\"/\";$Domain=localhost"}}
        resp ((wrap-cookies :cookies) req)]
    (is (= {"a" {:value "b" :path "/" :domain "localhost"}} resp))))

(deftest cookie-test-6
  (let [handler (constantly {:cookies {"a" "b"}})
        resp ((wrap-cookies handler) {})]
    (is (= {"Set-Cookie" ["a=\"b\""]}
           (:headers resp)))))

(deftest cookie-test-7
  (let [handler (constantly {:cookies {"a" "b" "c" "d"}})
        resp ((wrap-cookies handler) {})]
    (is (= {"Set-Cookie" ["a=\"b\"" "c=\"d\""]}
           (:headers resp)))))

(deftest cookie-test-8
  (let [handler (constantly {:cookies {:a "b"}})
        resp ((wrap-cookies handler) {})]
    (is (= {"Set-Cookie" ["a=\"b\""]}
           (:headers resp)))))

(deftest cookie-test-9
  (let [handler (constantly {:cookies {:a "b" :path "/" :secure true}})
        resp ((wrap-cookies handler) {})]
    (is (= {"Set-Cookie" ["a=\"b\";Path=\"/\";Secure"]}
           (:headers resp)))))
