;; Copyright 2013 the original author or authors.
;; 
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;; 
;;      http://www.apache.org/licenses/LICENSE-2.0
;; 
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns vertx.eventbus
  "Functions for operating on the eventbus and its messages."
  (:refer-clojure :exclude [send])
  (:require [vertx.core :as core]
            [vertx.utils :refer :all]))

(def ^{:dynamic true
       :doc "The currently active EventBus instance.
             If not bound, the EventBus from vertx.core/*vertx* will be used.
             You should only need to bind this for advanced usage."}
  *event-bus* nil)

(defn get-event-bus
  "Returns the currently active EventBus instance."
  []
  (or *event-bus* (.eventBus (core/get-vertx))))

(defn send
  ([addr content]
     (send addr content nil))
  ([addr content handler]
     (.send (get-event-bus)
            addr
            (encode content)
            (core/as-handler handler))))

(defn publish
  [addr content]
  (.publish (get-event-bus) addr (encode content)))

(defn reply
  ([m]
     (.reply m))
  ([m content]
     (.reply m (encode content)))
  ([m content handler]
     (.reply m (encode content)) (core/as-handler handler)))

(let [registered-handlers (atom {})]

  (defn register-handler
    "Registers a handler fn to receive messages on an address.
Returns an id for the handler that can be passed to
unregister-handler."
    ([addr handler]
       (register-handler addr handler false))
    ([addr handler local-only?]
       (let [h (core/as-handler handler)
             id (uuid)]
         (if local-only?
           (.registerLocalHandler (get-event-bus) addr h)
           (.registerHandler (get-event-bus) addr h))
         (swap! registered-handlers assoc id [addr h])
         id)))

  (defn unregister-handler
    [id]
    (if-let [[addr h] (@registered-handlers id)]
      (do
        (.unregisterHandler (get-event-bus) addr h)
        (swap! registered-handlers dissoc id))
      (throw (IllegalArgumentException.
              (format "No handler with id %s found." id))))
    nil))

(defn message-body [m]
  (decode (.body m)))

