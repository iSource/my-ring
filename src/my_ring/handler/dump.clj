(ns my-ring.handler.dump
  (:use [hiccup core def])
  (:require [ring.mock.request :as mock]
            [clojure.string :as str]
            [my-ring.adapter.jetty :as jetty]
            [clojure.set :as set]))

(def css
  "
  html{color:#000;background:#FFF;}
  body,div,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,code,form,fieldset,legend,input,textarea,p,blockquote,th,td{margin:0;padding:0;}
  table{border-collapse:collapse;border-spacing:0;}
  fieldset,img{border:0;}
  address,caption,cite,code,dfn,em,strong,th,var{font-style:normal;font-weight:normal;}
  li{list-style:none;}
  caption,th{text-align:left;}
  h1,h2,h3,h4,h5,h6{font-size:100%;font-weight:normal;}
  q:before,q:after{content:'';}
  abbr,acronym{border:0;font-variant:normal;}
  sup{vertical-align:text-top;}
  sub{vertical-align:text-bottom;}
  input,textarea,select{font-family:inherit;font-size:inherit;font-weight:inherit;}
  input,textarea,select{*font-size:100%;}
  legend{color:#000;}
  del,ins{text-decoration:none;}
h3.info {
 font-size: 1.5em;
 margin-left: 1em;
 padding-top: .5em;
 padding-bottom: .5em;
}
table.trace {
  margin-left: 1em;
  background: lightgrey;
}
table.trace tr {
  line-height: 1.4em;
}
table.trace td.method {
  padding-left: .5em;
  padding-right: .5em;
  text-aligh: left;
  border:1px solid #444;
  font-family:consolas;
  font-size:0.5em;
  color:#444;
}
table.trace td.source {
  padding-left: 0.5em;
  padding-right: .5em;
  text-align: left;
  border:1px solid #444;
  font-family:consolas;
  font-size:0.5em;
  color:#444;
}

table.trace td.even {
  background-color:#c3c3c3;
}
table.trace td.even {
  background-color:#c3c3c3;
}
  ")

(def ring-keys
  '(:server-port :server-name :remote-addr :uri :query-string
    :request-method :content-type :content-length :character-encoding
    :headers))

(defhtml req-pair
  [k req is-even?]
  (if (not is-even?)
    [:tr [:td.source.even (h (str k))]
     [:td.method.even (h (pr-str (k req)))]]
    [:tr [:td.source (h (str k))]
     [:td.method (h (pr-str (k req)))]]))

(defmacro domap-str
  [[binding-form list] & body]
  `(apply str (map (fn [~binding-form] ~@body) ~list)))

(defn table-str
  [ring-keys req]
  (apply str (map (fn [[key value]] (req-pair value req (even? key))) (sort (zipmap (range) ring-keys)))))

(defhtml template
  [req]
  [:html
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html"}]
    [:title "Ring: Environment Dump"]]
   [:style {:type "text/css"} css]
   [:body
    [:div#content
     [:h3.info "Ring Env. Values"]
     [:table.trace
      [:tbody
         (table-str ring-keys req)]]
     (if-let [user-keys (set/difference (set (keys req)) (set ring-keys))]
       (html
         [:br]
         [:table.trace
          [:tbody
           (table-str user-keys req)]]))]]])

(defn app
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (template req)})

#_(defn app [req]
  {:status 200
   :body "<h1>Changed</h1>"})

(defn -main
  [& args]
  (jetty/run-jetty app {:port 3000}))
