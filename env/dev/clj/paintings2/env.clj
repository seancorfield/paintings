(ns paintings2.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [paintings2.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[paintings2 started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[paintings2 has shut down successfully]=-"))
   :middleware wrap-dev})
