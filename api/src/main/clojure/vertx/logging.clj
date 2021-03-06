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

(ns vertx.logging
  "Functions for logging to the Vert.x logging subsystem."
  (:require [vertx.core :as core]))

(def ^{:dynamic true
       :internal true
       :no-doc true
       :doc "Used to provide a logger when we are outside of a vertx context. Should never be used by user code."}
  *logger*)

(defn get-logger
  "Returns the currently active Logger retrieved from vertx.core/*container*."
  []
  (or (.logger (core/get-container)) *logger*))

(defn info?
  "Returns true if info logging is enabled."
  []
  (.isInfoEnabled (get-logger)))

(defn debug?
  "Returns true if debug logging is enabled."
  []
  (.isDebugEnabled (get-logger)))

(defn trace?
  "Returns true if trace logging is enabled."
  []
  (.isTraceEnabled (get-logger)))

(defn fatal*
  "Prints a fatal log entry."
  ([obj]
     (.fatal (get-logger) obj))
  ([obj throwable]
     (.fatal (get-logger) obj throwable)))

(defn error*
  "Prints an error log entry."
  ([obj]
     (.error (get-logger) obj))
  ([obj throwable]
     (.error (get-logger) obj throwable)))

(defn warn*
  "Prints a warn log entry."
  ([obj]
     (.warn (get-logger) obj))
  ([obj throwable]
     (.warn (get-logger) obj throwable)))

(defn info*
  "Prints an info log entry if info is enabled."
  ([obj]
     (.info (get-logger) obj))
  ([obj throwable]
     (.info (get-logger) obj throwable)))

(defn debug*
  "Prints an debug log entry if debug is enabled."
  ([obj]
     (.debug (get-logger) obj))
  ([obj throwable]
     (.debug (get-logger) obj throwable)))

(defn trace*
  "Prints an trace log entry if trace is enabled."
  ([obj]
     (.trace (get-logger) obj))
  ([obj throwable]
     (.trace (get-logger) obj throwable)))

(defmacro ^:internal ^:no-doc log [cond f & args]
  `(when ~cond
     (let [args# [~@args]
           head# (first args#)]
       (if (instance? Throwable head#)
         (~f (apply print-str (rest args#)) head#)
         (~f (print-str ~@args))))))

(defmacro fatal
  "Prints a fatal log entry."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(log true fatal* ~@args))

(defmacro error
  "Prints an error log entry."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(log true error* ~@args))

(defmacro warn
  "Prints a warn log entry."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(log true warn* ~@args))

(defmacro info
  "Prints an info log entry if info is enabled."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(log (info?) info* ~@args))

(defmacro debug
  "Prints a debug log entry if debug is enabled."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(log (debug?) debug* ~@args))

(defmacro trace
  "Prints a trace log entry if trace is enabled."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(log (trace?) trace* ~@args))


