(ns user
  (:require [mount.core :as mount]
            paintings2.core))

(defn start []
  (mount/start-without #'paintings2.core/repl-server))

(defn stop []
  (mount/stop-except #'paintings2.core/repl-server))

(defn restart []
  (stop)
  (start))


