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
                                          :f #(update-in % [:time] mt/add-time)})
                   (cards/make-fn-button {:title "less"
                                          :f #(update-in % [:time] mt/remove-time)})
                   (cards/make-fn-button {:title "pause"
                                          :f #(update-in % [:time :pause] not)})]
    :data {:time {:t 120}}}))
