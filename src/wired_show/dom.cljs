(ns wired-show.dom
  (:require-macros wired-show.dom)
  (:refer-clojure :exclude [map meta time])
  (:require
    [wired-show.events :as events]
    [wired-show.data   :as wired-data]
    show.dom
    [wire.core         :as wire]
    [clojure.string :refer [lower-case]]))

;; wire-up
;;
(defn- dom-event-fn [dom type action]
  (fn [event]
    (let [wires (aget dom "__wires")
          data {:type type, :action action, :event event, :context :dom}]
      (if (not (empty? wires))
        (let [criteria    (wired-data/build-criteria data)
              return-data (wired-data/build-data data)]
          (doseq [wire wires]
            (wire/act wire criteria return-data)))))))

(defn- keyword->event [kw]
  (get events/react-handler->dom-listener kw))

(defn- determine-tag-name
  "Since we are recieveng dom objects, we must tease out its actual tag name.
  It gets a little more complicated with non-tag dom objects"
  [dom]
  (if-let [name (.-tagName dom)]
    (.toLowerCase name)
    (condp = dom
      js/window "window"
      js/document "document")))

(defn- inject-events [wire dom]
  (doseq [[kw f] (events/events-for-tag (determine-tag-name dom)
                                        (partial dom-event-fn dom))]
    (.addEventListener dom (keyword->event kw) #(f %))))

(defn wire-up
  "Attach wire to dom object and inject act fn calles to all appropriate
  events. If already wired up, inject wire into wires property"
  [wire dom]
  (if-let [wires (aget dom "__wires")]
    (aset dom "__wires" (conj wires wire))
    (do (inject-events wire dom)
        (aset dom "__wires" [wire])))
  wire)

(defn unwire
  "Remove wire from dom object"
  [wire dom]
  (let [wires (into [] (remove #(= (:id (wire/data wire))
                                   (:id (wire/data %)))
                               (aget dom "__wires")))]
    (aset dom "__wires" wires)))

;; wired dom
;;
(defn- wired-event-fn [wire tag-name opts type action]
  (fn [event]
    (let [data {:type     type
                :action   action
                :event    event
                :tag-name tag-name
                :opts     opts
                :context  :show}]
      (wire/act wire (wired-data/build-criteria data)
                     (wired-data/build-data data)))))

(defn- inject-acts-for-tag [tag-name opts wire]
  (events/events-for-tag tag-name (partial wired-event-fn wire tag-name opts)))

(defn- array-map? [o]
  (instance? cljs.core/PersistentArrayMap o))

(defn- parse-tag-options [vs]
  (let [vs (remove nil? vs)
        end (if (map? (first vs))
              [(first vs) (next vs)]
              [{}         (seq vs)])]
    end))

;; Inject tags into CLJS
(wired-show.dom/build-tags)
