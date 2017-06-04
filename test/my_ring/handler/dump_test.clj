(ns my-ring.handler.dump-test
  (:require [clojure.test :refer :all]
            [my-ring.handler.dump :refer :all]))

(deftest dump-test
  (is ((complement nil?) (app {:request-method :get}))))
