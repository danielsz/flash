(ns flash.react
  (:require [react :as react]
            [cljs.reader :refer [read-string]]
            [cljs-utils.react.hooks :refer [useLens]])
  (:require-macros [cljs-utils.compilers.hicada :refer [html]])
  (:import [goog.net XhrIo]))

(defn warn [app-state message]
  (swap! app-state assoc :flash {:message message :level :warning :timestamp (.getTime (js/Date.))}))
(defn bless [app-state message]
  (swap! app-state assoc :flash {:message message :level :success :timestamp (.getTime (js/Date.))}))
(defn alert [app-state message]
  (swap! app-state assoc :flash {:message message :level :danger :timestamp (.getTime (js/Date.))}))
(defn info [app-state message]
  (swap! app-state assoc :flash {:message message :level :info :timestamp (.getTime (js/Date.))}))

(defn display [app-state payload]
  (when-let [message (:message payload)]
    (case (:level payload)
      :success (bless app-state message)
      :warning (warn app-state message)
      :info (info app-state message)
      :danger (alert app-state message)
      (info app-state message))))

(defn widget [props]  
  (let [[timeout app-state fetch] ((juxt :timeout :state :fetch) (js->clj props :keywordize-keys true))
        get-messages (fn [app-state]
                       (.send XhrIo "/flash" (fn [e] (let [payload (.getResponseText (.-target e))]
                                                               (display app-state (read-string payload))))))
        flash (useLens app-state :flash)
        types {:success {:class "alert-success" :prefix "Well done!"} 
               :info {:class "alert-info" :prefix "Info!"}
               :warning {:class "alert-warning" :prefix "Sorry!"}
               :danger {:class "alert-danger" :prefix "Oh no!"}}]
    (react/useEffect (fn [] (get-messages app-state)) #js [])
    (react/useEffect (fn [] (js/setTimeout #(swap! app-state assoc :flash {}) (* timeout 1000))) #js [flash])
    (when (seq flash)
      (html [:div {:id "flash"
                   :className (str "alert fade in " (:class ((:level flash) types)))}
             [:strong (:prefix ((:level flash) types))]
             (str " " (:message flash))]))))

