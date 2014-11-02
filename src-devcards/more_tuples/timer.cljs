(ns more-tuples-devcards.timer
  (:require [devcards.core :as dc :include-macros true]
            [more-tuples.core :as mt]
            [more-tuples-devcards.cards :as cards]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [devcards.core :refer [defcard]]))

(defcard time-view-card
  (cards/om-manipulation-card
   {:view (fn [data owner] (om/component (om/build mt/time-view data)))
    :manipulators [(cards/make-fn-button {:title "more" :f mt/add-time})
                   (cards/make-fn-button {:title "less" :f mt/remove-time})
                   mt/pause-button]
    :data {:t 120}}))
