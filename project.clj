(defproject my-ring "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [javax.servlet/javax.servlet-api "3.1.0"]
                 [org.eclipse.jetty/jetty-server "9.4.5.v20170502"]
                 [hiccup "1.0.5"]
                 [ring/ring-mock "0.3.0"]
                 [clj-stacktrace "0.2.8"]
                 [commons-io/commons-io "2.5"]]
  :main ^:skip-aot my-ring.dump
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
