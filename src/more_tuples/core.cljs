(ns more-tuples.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [<! timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

; defaults

(def colors ["blue" "indigo" "green"])

; parts of drawing things

; http://www.codestore.net/store.nsf/unid/epsd-5dtt4l
(defn disk-slice
  [{:keys [begin portion color
           radius inner-radius-ratio border-width border-color]
    :or {radius 75
         inner-radius-ratio 0.4
         border-width 6
         border-color "grey"}}]
  (let [end (+ begin portion)
        inner-radius (* inner-radius-ratio radius)
        setback-radius (- radius (/ border-width 2))]
    (letfn [(x [port r] (+ radius (* r (Math/sin (* 2 Math/PI port)))))
            (y [port r] (+ radius (* -1 r (Math/cos (* 2 Math/PI port)))))]
      (dom/path
       #js {:d (str "M" (x begin inner-radius) "," (y begin inner-radius) " "
                    "L" (x begin setback-radius) "," (y begin setback-radius) " "
                    "A" setback-radius "," setback-radius " 0 0,1 " (x end setback-radius) "," (y end setback-radius) " "
                    "L" (x end inner-radius) "," (y end inner-radius) " "
                    "Z")
            :stroke border-color
            :strokeWidth border-width
            :fill color}))))

(defn draw-disk
  [{:keys [values radius segment selected on-click
           selected-color border-color margin]
    :or {selected false
         border-color "grey"
         selected-color "black"
         segment {}
         radius 75
         margin 5}}]
  (let [n (count values)
        portion (/ 1 n)
        segment-params (merge segment {:radius radius
                                       :portion portion
                                       :border-color (if selected
                                                       selected-color
                                                       border-color)})]
    (apply dom/svg
           #js {:width (* 2 radius) :height (* 2 radius)
                :onClick on-click
                :style #js {:margin margin}}
           (if values
             (mapv #(disk-slice (merge segment-params
                                       {:begin %1
                                        :color (colors %2)}))
                   (range 0 0.99 portion)
                   values)
             []))))


(defn disk-view
  [disk owner]
  (reify
    om/IInitState
    (init-state [_]
                {:on-click (fn [_] (om/transact! disk :selected not))})
    om/IRenderState
    (render-state [_ state]
                  (draw-disk (merge disk state)))))

; functions for working with values as sets

(defn matching-disk
  [disks]
  {:values (apply mapv #(rem (- 6 (+ %1 %2)) 3) (map :values disks))})

(defn is-set?
  [disks]
  (and (= 3 (count disks))
       (every? :values disks)
       (apply = 0 (apply map #(rem (+ %1 %2 %3) 3) (map :values disks)))))

; board of multiple disks

(defn layout
  [{:keys [per-row elements]}]
  (apply dom/div nil
         (map #(apply dom/div nil %)
              (partition-all per-row elements))))

(defn board-view
  [data owner]
  (reify
    om/IRender
    (render [_]
            (layout {:elements (om/build-all disk-view (:disks data))
                     :per-row 3}))))

; more stuff

(defn random-disk
  [{:keys [size]}]
  {:values (vec (repeatedly size #(rand-int 3)))})

(defn remove-selected-disks
  [{:keys [disks] :as data}]
  (assoc data :disks (mapv #(if (:selected %) {} %) disks)))

(defn choices
  [{choose :choose from :from}]
  (letfn [(indices [n k]
                   (if (= 0 k)
                     '(())
                     (mapcat (fn [i] (map #(conj % i)
                                          (indices i (dec k))))
                             (range n))))]
    (mapv #(mapv (vec from) %) (indices (count from) choose))))

(defn set-exists?
  [{:keys [disks]}]
    (some is-set? (choices {:choose 3 :from disks})))

(defn set-making-disk
  [disks]
  (matching-disk (rand-nth (choices {:choose 2 :from (filter :values disks)}))))

(defn fill-all-but-one
  [{:keys [disks size] :as data}]
  (let [empty-indices (keep-indexed (fn [i d] (when-not (:values d) i)) disks)
        to-fill (rand-nth (choices {:choose (dec (count empty-indices))
                                    :from empty-indices}))]
    (assoc data
      :disks
      (reduce #(assoc %1 %2 (random-disk {:size size})) disks to-fill))))

(defn fill-last-guaranteeing-set
  [{:keys [disks size] :as board}]
  (let [new-disk (if (set-exists? board)
                   (random-disk {:size size})
                   (set-making-disk disks))]
    (assoc board :disks (mapv #(if (:values %) % new-disk) disks))))

(defn fill-guaranteeing-set
  [{:keys [disks] :as data}]
  (-> data fill-all-but-one fill-last-guaranteeing-set))

; time bar

(def fraction-jump (/ 1 4))
(def max-time-added 60)

(defn add-time
  [{t :t :as data}]
  (print "adding time")
  (let [max-total-time (/ max-time-added fraction-jump)
        lost-time (- max-total-time t)
        to-gain (* lost-time fraction-jump)]
    (assoc data :t (Math/ceil (+ t to-gain)))))

(defn remove-time
  [{t :t :as data}]
  (print "removing time")
  (assoc data :t (Math/ceil (- t (* t fraction-jump)))))

(defn time-view
  [t owner]
  (reify
    om/IWillMount
    (will-mount
     [_]
     (go (loop [] (<! (timeout 1000)) (om/transact! t :t dec) (recur))))

    om/IRender
    (render [_] (dom/div nil (str "remaining time: " (:t t))))))

; board

(defn replace-selected
  [board]
  (-> board remove-selected-disks fill-guaranteeing-set))

(defn new-board
  [{:keys [size number]; TODO: "size" is ambiguous
    :or {number 9}}]
  (fill-guaranteeing-set {:size size :disks (vec (repeat number {}))}))

(defn game-view
  [data owner]
  (reify
    om/IWillReceiveProps
    (will-receive-props
     [_ next-props]
     (let [selected (filter :selected
                            (get-in next-props
                                    [:board :disks]))]

       (when (= 3 (count selected))
         (if (is-set? selected)
           (do
             (if (>= (inc (:score next-props))
                     (* 16 (dec (:size (:board next-props)))))

               ; every 16 sets, increase disk segments
               (om/update! data :board
                           (new-board {:size (-> next-props
                                                 :board
                                                 :size
                                                 inc)}))
               ; but usually just replace
               (om/transact! data :board replace-selected))

             ; increment score
             (om/transact! data :score inc)

             ; add more time
             (om/transact! data :time add-time))

           ; if it wasn't a set, deselect and penalize time
           (do
             (om/transact! data
                           [:board :disks]
                           (partial mapv
                                    #(dissoc % :selected)))
             (om/transact! data :time remove-time))))))

    om/IRender
    (render [_]
            (if (> (get-in data [:time :t]) 0)
              (dom/div nil
                       (dom/div nil (str "score: " (:score data)))
                       (om/build time-view (:time data))
                       (om/build board-view (:board data)))
              (dom/div nil
                       (dom/p nil "game over")
                       (dom/p nil (str "score: " (:score data)))
                       (dom/p nil "(reload to play again)"))))))

(def game-state (atom {:board (new-board {:size 2})
                       :score 0
                       :time {:t 120}}))

(om/root
  game-view
  game-state
  {:target (. js/document (getElementById "app"))})
