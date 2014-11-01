(ns more-tuples-devcards.scratch
  (:require
   [devcards.core :as dc :include-macros true]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! timeout put! alts! close! chan]])
  (:require-macros
   [devcards.core :refer [defcard]]
   [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

; changing props based on props

(defn incrementing-button
  [data owner]
  (reify
    om/IRender
    (render [_] (dom/button #js {:onClick (fn [_] (om/transact! data :n inc))}
                            (:n data)))))

(defcard incrementing-button-card
  (dc/om-root-card incrementing-button {:n 0}))

(defn button-collection
  [data owner]
  (reify
    om/IWillReceiveProps
    (will-receive-props [_ next-props]
                        (when (>= (apply max (map :n (:ns next-props)))
                                  (count (:ns next-props)))
                          (om/transact! data :ns #(conj % {:n 0}))))

    om/IRender
    (render [_] (apply dom/div nil
                       (om/build-all incrementing-button (:ns data))))))

(defcard button-collection-card
  (dc/om-root-card button-collection {:ns [{:n 0}]}))


; time

(defn counting-button
  [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
                (go (loop [] (<! (timeout 1000)) (om/transact! data :t inc) (recur))))

    om/IRender
    (render [_] (dom/button #js {:onClick (fn [_] (om/update! data :t 0))}
                            (:t data)))))

(defcard counting-button-card
  (dc/om-root-card counting-button {:t 0}))
