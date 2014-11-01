(ns more-tuples-devcards.cards
  (:require
   [devcards.core :as dc :include-macros true]
   [devcards.util.edn-renderer :as edn-rend]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true])
  (:require-macros
   [devcards.core :refer [defcard]]))

(defn edn-view
  [data owner]
  (reify
    om/IRender
    (render [_] (edn-rend/html-edn data))))

(defn make-fn-button [{:keys [f title]}]
  (fn [data owner]
    (reify
      om/IRender
      (render [_] (dom/button  #js {:onClick (fn [_] (om/transact! data f))}
                                     title)))))

(defcard edn-view-test
  (dc/om-root-card edn-view {:edn-view "this is useful to have"}))

(defn om-manipulation-card
  [{:keys [view manipulators analyzers data]
    :or {view edn-view
         manipulators []
         analyzers []
         data {}}}]
  (dc/om-root-card
   (fn [d _]
     (reify
       om/IRender
       (render [_]
               (dom/div nil
                        (dom/div #js {:style #js {:float "right"}}
                                 (om/build view d))
                        (dom/div #js {:style #js {:float "left"
                                                  :borderRightStyle "solid"
                                                  :borderRight "lightgrey"
                                                  :marginRight "10px"
                                                  :paddingRight "10px"}}
                                 (apply dom/div
                                        nil
                                        (map #(dom/div nil (om/build % d))
                                             manipulators))
                                 (dom/hr nil)
                                 (apply dom/div
                                        nil
                                        (map #(dom/div nil (om/build % d))
                                             analyzers)))
                        (dom/div #js {:className "clearfix"})))))
   data))


(defcard about-om-manipulation-card
  (dc/markdown-card "## om-manipulation-card
                    this is a custom devcard type for manipulating and analyzing om data"))

(defn int-view [data owner]
  (reify om/IRender (render [_] (dom/p nil (str (:i data))))))

(defn int-square-view [data owner]
  (reify om/IRender (render [_] (dom/p nil "square: " (str (* (:i data) (:i data)))))))

(defcard om-manipulation-card-example
  (om-manipulation-card
   {:view int-view
    :manipulators [(make-fn-button {:f #(assoc % :i (inc (:i %)))
                                    :title "increment"})
                   (make-fn-button {:f #(assoc % :i (dec (:i %)))
                                    :title "decrement"})]
    :analyzers [int-square-view]
    :data {:i 0}}))
