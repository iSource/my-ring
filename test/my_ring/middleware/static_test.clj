(ns my-ring.middleware.static-test
  (:require [clojure.test :refer :all]
            [my-ring.middleware.static :refer :all]
            [my-ring.handler.dump :as dump]))

(deftest static-test
  (is ((complement nil?) (wrap "resources" ["my"] dump/app))))
