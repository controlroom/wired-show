(ns wired-show.dom)

(defn wired-tag [tag]
 `(defn ~tag [wire# & vs#]
    (assert (satisfies? wire/BaseWire wire#)
            "The first argument for a wired tag should be a wire")
    (let [[opts# body#] (parse-tag-options vs#)
          opts# (merge (inject-acts-for-tag ~(name tag) opts# wire#)
                       opts#)]
      (apply show.dom/element ~(str tag) opts# body#))))

(defmacro build-tags []
 `(do ~@(map wired-tag show.dom/tags)))
