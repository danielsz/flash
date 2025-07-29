(ns flash.react
  (:require [react :as react]
            [clojure.edn :refer [read-string]]
            [cljs-utils.react.hooks :refer [useLens]])
  (:require-macros [cljs-utils.compilers.hicada :refer [html]])
  (:import [goog.net XhrIo]))

(defonce state (atom {}))

(defn widget [props]
  (let [{:keys [timeout checkOnLoad] :or {checkOnLoad false timeout 3}} (js->clj props :keywordize-keys true)
        flash (useLens state :flash)
        check #(.send XhrIo "/flash" (fn [e] (let [payload (read-string (.getResponseText (.-target e)))]
                                              (when (seq payload)
                                                (swap! state assoc :flash payload)))))
        types {:success {:class "alert-success" :prefix "Well done!"} 
               :info {:class "alert-info" :prefix "Info!"}
               :warning {:class "alert-warning" :prefix "Sorry!"}
               :danger {:class "alert-danger" :prefix "Oh no!"}}]
    (react/useEffect (fn [] (let [id (js/setTimeout #(swap! state assoc :flash {}) (* timeout 1000))]
                             (fn [] (js/clearTimeout id)))) #js [flash])
    (react/useEffect (fn [] (when checkOnLoad (check))
                       (fn [] (println "flash widget cleanup, check on load" checkOnLoad))) #js [])
    (html (if (seq flash)
            [:div {:id "flash"
                   :className (str "alert fade in " (:class ((:level flash) types)))}
             [:strong (:prefix ((:level flash) types))
              (str " " (:message flash))]]
            [:div ""]))))


