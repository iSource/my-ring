(ns my-ring.middleware.stacktrace
  (:use (clj-stacktrace core repl)
        (hiccup core def))
  (:require [my-ring.adapter.jetty :as jetty]))

(def #^{:private true} css
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

table.trace td.header {
  font-weight:bold;
}
  ")

(defmacro domap-str [[binding-form list] & body]
  `(apply str (map (fn [~binding-form] ~@body) ~list)))

;{:file "Numbers.java", :line 158, :java true, :class "clojure.lang.Numbers", :method "divide"}

(defhtml trace-elem [elem]
  [:tr
   [:td.source (:file elem)]
   [:td.source.even (:line elem)]
   [:td.source (:java elem)]
   [:td.source.even (:class elem)]
   [:td.source (:method elem)]])

(defn- response [request e]
  (let [exc (parse-exception e)]
    (prn (parse-exception e))
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body
     (html
       [:html
        [:head
         [:meta {:http-equiv "Content-Type" :content "text/html"}]
         [:title "Ring: Exception"]]
        [:style {:type "text/css"} css]
        [:body
         [:div#content
           [:h3.info (str e)]
           [:table.trace
            [:tbody
             [:tr
              [:td.source (pr-str (:class exc))]
              [:td.source.even (pr-str (:message exc))]]]]
           [:br]
           [:table.trace
            [:tbody
             [:tr [:td.source.header "File"] [:td.source.even.header "Line"] [:td.source.header "Java"] [:td.source.even.header "Class"] [:td.source.header "Method"]]
             (domap-str [parsed (:trace-elems exc)]
                        (html (trace-elem parsed)))]]]]])}))

(defn app [request]
  (/ 1 0 (map))
  {:status 200
   :body "<h1>OK</h1>"})

(defn wrap [app]
  (fn [request]
    (try
      (app request)
      (catch Exception e
        (response request e)))))

(defn -main [& args]
  (jetty/run-jetty (-> app wrap) {:port 3000}))
