(ns flash.react
  (:require [react :as react]
            [cljs.reader :refer [read-string]]
            [cljs-utils.react.hooks :refer [useLens]])
  (:require-macros [cljs-utils.compilers.hicada :refer [html]])
  (:import [goog.net XhrIo]))

(defonce state (atom {}))

(defn widget [props]
  (let [{timeout :timeout } (js->clj props :keywordize-keys true)
        flash (useLens state :flash)
        types {:success {:class "alert-success" :prefix "Well done!"} 
               :info {:class "alert-info" :prefix "Info!"}
               :warning {:class "alert-warning" :prefix "Sorry!"}
               :danger {:class "alert-danger" :prefix "Oh no!"}}]
    (react/useEffect (fn [] (let [id (js/setTimeout #(swap! state assoc :flash {}) (* timeout 1000))]
                             (fn [] (js/clearTimeout id)))) #js [flash])
    (when (seq flash)
      (html [:div {:id "flash"
                   :className (str "alert fade in " (:class ((:level flash) types)))}
             [:strong (:prefix ((:level flash) types))]
             (str " " (:message flash))]))))

(defn check []
  (.send XhrIo "/flash" (fn [e] (let [payload (read-string (.getResponseText (.-target e)))]                                 
                                 (when (seq payload)
                                   (swap! state assoc :flash payload))))))
