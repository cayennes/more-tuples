(ns more-tuples-devcards.core
  (:require
   [devcards.core :as dc :include-macros true]
   [more-tuples.core :as mt]
   [more-tuples-devcards.cards :as cards]
   [more-tuples-devcards.scratch]; TODO: instructions suggest this isn't necessary
   [more-tuples-devcards.timer]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true])
  (:require-macros
   [devcards.core :refer [defcard]]))

(enable-console-print!)

(devcards.core/start-devcard-ui!)
(devcards.core/start-figwheel-reloader!)

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/devcards/index.html

(defcard more-tuples-devcards-intro
  (dc/markdown-card "# devcards for more-tuples
                     Writing a simple web game using devcards as a process"))

; parts of drawing things

; om disk component

(defcard disk-component
  (dc/markdown-card "## disk component"))

(defcard draw-disk-parameters
  (dc/slider-card
   (fn [{:keys [number-of-values border-width inner-radius-ratio :disabled]
         :as params}]
     (-> params
         (dissoc :number-of-values :border-width :inner-radius-ratio :disabled)
         (assoc :values (if disabled
                          nil
                          (map #(rem % 3) (range number-of-values)))
           :segment {:border-width border-width
                     :inner-radius-ratio inner-radius-ratio})))
   {:number-of-values (range 2 21)
    :radius (range 50 151)
    :border-width (conj (range 0 31) 6)
    :inner-radius-ratio (conj (range 0.01 1 0.01) 0.4)
    :selected [false true]
    :selected-color ["black" "darkkhaki" "darkgoldenrod" "grey" "wheat"]
    :disabled [false true]}
   :value-render-func mt/draw-disk))

(defcard disk-view-card
  (dc/om-root-card mt/disk-view {:values [0 1 2]}))

; functions for working with values as sets

(defcard sets-of-disks
  (dc/markdown-card "## functions dealing with sets of disks"))

(defcard make-set-slider
  (dc/slider-card
   (fn [{:keys [first-values second-values]}]
     (let [disk1 {:values first-values}
           disk2 {:values second-values}]
       [disk1 disk2 (mt/matching-disk [disk1 disk2])]))
   (let [all (for [i0 (range 3)
                   i1 (range 3)
                   i2 (range 3)
                   i3 (range 3)]
               [i0 i1 i2 i3])]
     {:first-values all
      :second-values all})
   :value-render-func
   (fn [disks] (om/build-all mt/disk-view disks))))

(defcard is-set-slider
  (dc/slider-card
   (fn [{:keys [first-values second-values third-values]}]
     (let [disks (map #(hash-map :values %)
                      [first-values second-values third-values])]
       {:disks disks
        :result (if (mt/is-set? disks) "yes" "no")}))
   (let [all (for [i0 (range 3)
                   i1 (range 3)
                   i2 (range 3)]
               [i0 i1 i2])]
     {:first-values all
      :second-values all
      :third-values all})
   :value-render-func
   (fn [{:keys [disks result]}]
     (dom/div nil
              (apply dom/div nil (om/build-all mt/disk-view disks))
              (dom/h2 nil result)))))

; board of multiple disks

(defcard board-component
  (dc/markdown-card "## board component"))

(defcard layout-slider
  (dc/slider-card
   (fn [{:keys [number-of-elements per-row]}]
     {:elements (repeat number-of-elements " (: ")
      :per-row per-row})
   {:number-of-elements (conj (range 1 21) 5)
    :per-row (conj (range 1 11) 3)}
   :value-render-func mt/layout))

(defcard board-view-card
  (dc/om-root-card mt/board-view {:disks (vec (repeat 9 {:values [0 1 2 2 1]}))}))

; reporting on selected sets

(defcard board-analysis
  (dc/markdown-card "## board analysis"))

(defn selected-count-view
  [data owner]
  (reify
    om/IRender
    (render [_]
            (let [selected (filter :selected (:disks data))]
              (dom/p nil "selected: " (count selected))))))

(defn selected-check-view
  [data owner]
  (reify
    om/IRender
    (render [_]
            (let [selected (filter :selected (:disks data))]
              (dom/p nil "set: " (if (mt/is-set? selected) "yes" "no"))))))

(defcard check-selected-sets
  (cards/om-manipulation-card
   {:view mt/board-view
    :analyzers [selected-check-view selected-count-view]
    :data {:disks (vec (for [a (range 3) b (range 3)] {:values [a b]}))}}))

; more stuff

(defcard adding-disks
  (dc/markdown-card "## adding disks to the board"))

(defn make-random-disk-view
  [disk owner]
  (reify
    om/IRender
    (render [_]
            (dom/button #js {:onClick
                             (fn [_] (om/update! disk
                                                 (mt/random-disk {:size 3})))}
                        "randomize"))))

(defcard random-disk
  (cards/om-manipulation-card
   {:view mt/disk-view
    :manipulators [make-random-disk-view]
    :analyzers [cards/edn-view]
    :data (mt/random-disk {:size 3})}))

(defn set-exists-view
  [data owner]
  (reify
    om/IRender
    (render [_]
            (dom/p nil
                   (if (mt/set-exists? data)
                     "there is a set"
                     "there are no sets")))))

(defn remove-selected-if-set
  [{:keys [disks] :as data}]
  (if (mt/is-set? (filter :selected disks))
    (mt/remove-selected-disks data)
    data))

(defn populate-with-random
  [{:keys [disks] :as data}]
  (assoc data :disks (mapv #(if(:values %)
                             %
                             (mt/random-disk {:size 3}))
                          disks)))


(defcard random-board-manipulation
  (cards/om-manipulation-card
   {:view mt/board-view
    :manipulators [(cards/make-fn-button {:f populate-with-random
                                          :title "fill with random"})
                   (cards/make-fn-button {:f mt/fill-all-but-one
                                          :title "fill all but one"})
                   (cards/make-fn-button {:f mt/fill-guaranteeing-set
                                          :title "fill guaranteeing set"})
                   (cards/make-fn-button {:f mt/remove-selected-disks
                                          :title "remove selected"})
                   (cards/make-fn-button {:f remove-selected-if-set
                                          :title "remove set"})]
    :analyzers [set-exists-view]
    :data {:disks (vec (repeat 9 {}))
           :size 3}}))

; responsive board

(defcard game-view-card
  (cards/om-manipulation-card
   {:view mt/game-view
    :data {:board (mt/new-board {:size 2}) :score 0 :time 120}
    :analyzers [cards/edn-view]}))
