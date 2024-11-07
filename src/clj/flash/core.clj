(ns flash.core
  (:require [ring.util.response :refer [response content-type]]))

(def flash-route
  ["/flash" {:get (fn [{session :session :as request}]
                    (if-let [flash (:flash session)]
                      (let [session (dissoc session :flash)]
                        (-> (pr-str flash)
                           response
                           (assoc :session session)
                           (content-type "application/edn")))
                      (-> ""
                         response
                         (content-type "text/html"))))}])
