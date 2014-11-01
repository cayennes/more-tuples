(ns more-tuples-devcards.timer
  (:require [devcards.core :as dc :include-macros true]
            [more-tuples.core :as mt]
            [more-tuples-devcards.cards :as cards]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [devcards.core :refer [defcard]]))

(defcard time-view-card
  (cards/om-manipulation-card
   {:view (fn [data owner] (om/component (om/build mt/time-view (:time data))))
    :manipulators [(cards/make-fn-button {:title "more"
                                          :f #(assoc % :time
                                                (mt/add-time (:time %)))})
                   (cards/make-fn-button {:title "less"
                                          :f #(assoc % :time
                                                (mt/remove-time (:time %)))})]
    :data {:time {:t 120}}}))
