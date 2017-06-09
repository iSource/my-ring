(ns my-ring.middleware.file-info
  (:require [my-ring.adapter.jetty :as jetty]
            [my-ring.middleware.file :as file])
  (:import org.apache.commons.io.FilenameUtils
           java.io.File))

(def base-mime-types
  {"ai"    "application/postscript"
   "asc"   "text/plain"
   "avi"   "video/x-msvideo"
   "bin"   "application/octet-stream"
   "bmp"   "image/bmp"
   "class" "application/octet-stream"
   "cer"   "application/pkix-cert"
   "crl"   "application/pkix-crl"
   "crt"   "application/x-x509-ca-cert"
   "css"   "text/css"
   "dms"   "application/octet-stream"
   "doc"   "application/msword"
   "dvi"   "application/x-dvi"
   "eps"   "application/postscript"
   "etx"   "text/x-setext"
   "exe"   "application/octet-stream"
   "gif"   "image/gif"
   "htm"   "text/html"
   "html"  "text/html"
   "jpe"   "image/jpeg"
   "jpeg"  "image/jpeg"
   "jpg"   "image/jpeg"
   "js"    "text/javascript"
   "lha"   "application/octet-stream"
   "lzh"   "application/octet-stream"
   "mov"   "video/quicktime"
   "mpe"   "video/mpeg"
   "mpeg"  "video/mpeg"
   "mpg"   "video/mpeg"
   "pbm"   "image/x-portable-bitmap"
   "pdf"   "application/pdf"
   "pgm"   "image/x-portable-graymap"
   "png"   "image/png"
   "pnm"   "image/x-portable-anymap"
   "ppm"   "image/x-portable-pixmap"
   "ppt"   "application/vnd.ms-powerpoint"
   "ps"    "application/postscript"
   "qt"    "video/quicktime"
   "ras"   "image/x-cmu-raster"
   "rb"    "text/plain"
   "rd"    "text/plain"
   "rtf"   "application/rtf"
   "sgm"   "text/sgml"
   "sgml"  "text/sgml"
   "tif"   "image/tiff"
   "tiff"  "image/tiff"
   "txt"   "text/plain"
   "xbm"   "image/x-xbitmap"
   "xls"   "application/vnd.ms-excel"
   "xml"   "text/xml"
   "xpm"   "image/x-xpixmap"
   "clj"   "text/plain"
   "xwd"   "image/x-xwindowdump"
   "swf"   "application/x-shockwave-flash"
   "zip"   "application/zip"})

(defn- guess-mime-type
  [#^File file mime-types]
  (get mime-types (FilenameUtils/getExtension (.getPath file))
       "application/octet-stream"))

(defn- rand-gen-error
  []
  (if (> (rand-int 10) 5) (/ 1 0)))

(defn wrap
  ([custom-mime-types app]
   (let [mime-types (merge base-mime-types custom-mime-types)]
    (fn [req]
      #_(rand-gen-error)
      (let [{:keys [headers body] :as resp} (app req)]
        (if (instance? File body)
          (assoc resp :headers
            (assoc headers "Content-Type" (guess-mime-type body mime-types)
              "Content-Length" (str (.length #^File body))))
          resp)))))
  ([app]
   (wrap {} app)))


(defn -main [& args]
  (jetty/run-jetty (->> file/app
                        (file/wrap (java.io.File. "resources"))
                        wrap)))
