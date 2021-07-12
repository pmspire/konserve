(ns konserve.utils)

(defn invert-map [m]
  (->> (map (fn [[k v]] [v k]) m)
       (into {})))

(defn now []
  #?(:clj (java.util.Date.)
     :cljs (js/Date.)))

(defn meta-update
  "Metadata has following 'edn' format
  {:key 'The stored key'
   :type 'The type of the stored value binary or edn'
   :timestamp Date timestamp in milliseconds.}
  Returns the meta value of the stored key-value tupel. Returns metadata if the key
  value not exist, if it does it will update the timestamp to date now. "
  [key type old]
  (if (empty? old)
    {:key key :type type :timestamp (now)}
    (clojure.core/assoc old :timestamp (now))))



(defmacro async+sync
  [sync? async->sync async-code]
  (let [res
        (if (true? sync?)
          (if sync?
            (clojure.walk/postwalk (fn [n]
                                      (if-not (meta n)
                                        (async->sync n n) ;; primitives have no metadata
                                        (with-meta (async->sync n n)
                                          (update (meta n) :tag (fn [t] (async->sync t t))))))
                                   async-code)
            async-code)
          `(if ~sync?
             ~(clojure.walk/postwalk (fn [n]
                                       (if-not (meta n)
                                         (async->sync n n) ;; primitives have no metadata
                                         (with-meta (async->sync n n)
                                           (update (meta n) :tag (fn [t] (async->sync t t))))))
                                     async-code)
             ~async-code))]
    #_(println "expansion" res)
    res))
