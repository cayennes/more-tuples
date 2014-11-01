(ns test.more-tuples.tests
  (:require [cemerick.cljs.test]
            ; it seems to be that two bugs cancel each other out, and
            ; this works iff more-tuples.core is *not* required
            #_more-tuples.core)
  (:require-macros [cemerick.cljs.test :refer (is deftest testing)]))

(deftest is-set
  (testing "set"
    (is (= true (more-tuples.core/is-set? (map #(hash-map :values %) [[0 0] [0 1] [0 2]])))))
  (testing "not a set"
    (is (= false (more-tuples.core/is-set? (map #(hash-map :values %) [[1 0] [0 0] [0 0]])))))
  (testing "wrong number of elements"
    (is (= false (more-tuples.core/is-set? (repeat 2 {:values [0 0 0]}))))
    (is (= false (more-tuples.core/is-set? (repeat 4 {:values [0 0 0]})))))
  (testing "empty elements"
   (is (= false (more-tuples.core/is-set? [{} {} {}])))
   (is (= false (more-tuples.core/is-set? [{} {:values [0 0]} {:values [0 0]}])))))

(deftest set-exists
  (testing "not enough"
    (is (= false (boolean (more-tuples.core/set-exists? {:disks [{}]}))))
    (is (= false (boolean (more-tuples.core/set-exists? {:disks [{:values [0 0 0]}]})))))

  (testing "set exists"
    (is (= true (boolean (more-tuples.core/set-exists? {:disks [{:values [0 0 0]}
                                                  {:values [0 0 0]}
                                                  {:values [0 0 0]}]}))))
    (is (= true (boolean (more-tuples.core/set-exists? {:disks [{:values [0 0 0]}
                                                  {:values [0 1 2]}
                                                  {:values [2 0 0]}
                                                  {:values [1 2 1]}]}))))
  (testing "doesn't exist"
    (is (= false (boolean (more-tuples.core/set-exists? {:disks [{:values [0 0 0]}
                                                   {:values [0 0 1]}
                                                   {:values [0 0 0]}]})))))))

(deftest test-fill-guaranteeing-set
  (is (= {:disks [{:values [0 0]} {:values [1 1]} {:values [2 2]}]}
         (more-tuples.core/fill-guaranteeing-set {:disks [{:values [0 0]}
                                   {:values [1 1]}
                                   {}]}))))
