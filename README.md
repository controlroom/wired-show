**Wired Show**

Wire up Show dom elements.

## Simple usage

```clojure
(ns basic-wired.core
  (:require
    [show.core       :as show]
    [show.dom        :as dom]
    [wired-show.dom  :as wired]
    [wire.core       :as w]))

(show/defcomponent Widget [component]
  (render [{:keys [selected wire name]} _]
    (dom/div
      (wired/button wire name)
      (when selected
        (dom/p "You Selected me!")))))

(defn tapped-widget-wire [component]
  (w/tap (w/wire)
    :mouse-click #(show/assoc! component :selected-widget (:id %))))

(show/defcomponent App [component]
  (initial-state
    {:selected-widget nil
     :widgets (for [i (range 20)] {:id i
                                   :name (str "widget-" i)})
     :wire (tapped-widget-wire component)})
  (render [_ {:keys [widgets wire selected-widget]}]
    (map (fn [{:keys [name id]}]
           (Widget {:key id
                    :wire (w/lay wire nil {:id id})
                    :selected (= selected-widget id)
                    :name name}))
      widgets)))

(show/render-to-dom
  (App)
  (.getElementById js/document "app"))
```

## License

Copyright © 2018 controlroom.io

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
