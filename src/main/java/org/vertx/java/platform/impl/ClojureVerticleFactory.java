/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.java.platform.impl;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.VerticleFactory;

import clojure.lang.RT;
import clojure.lang.Var;

public class ClojureVerticleFactory implements VerticleFactory {

    private static final Logger log = LoggerFactory.getLogger(ClojureVerticleFactory.class);
    private ClassLoader cl;
    public static Vertx vertx;
    public static Container container;

    public ClojureVerticleFactory() {
        super();
    }

    @Override
    public void init(Vertx vertx, Container container, ClassLoader cl) {
        this.cl = cl;
        ClojureVerticleFactory.vertx = vertx;
        ClojureVerticleFactory.container = container;
    }

    @Override
    public Verticle createVerticle(String scriptName) throws Exception {
        return new ClojureVerticle(scriptName);
    }

    @Override
    public void reportException(Logger logger, Throwable t) {
        logger.error("Unexpected exception in Clojure verticle", t);
    }

    @Override
    public void close() {}

    private class ClojureVerticle extends Verticle {
        private final String scriptName;
        private Var stopFunc;

        ClojureVerticle(String scriptName) {
            this.scriptName = scriptName;
        }

        public void start() {
            try {
                RT.load("clojure/core");
                clojure.lang.Var.pushThreadBindings(RT.map(clojure.lang.Compiler.LOADER, cl));
                RT.var("vertx.core", "vertx", ClojureVerticleFactory.vertx);
                RT.var("vertx.core", "container", ClojureVerticleFactory.container);
                RT.loadResourceScript(scriptName);
                stopFunc = RT.var("vertx.core", "vertx-stop");
            } catch(Exception e) {
                log.info(e.getMessage(), e);
            }
            log.info("Started clojure verticle: " + scriptName);
        }

        public void stop() {
            try {
                if(stopFunc != null)
                    stopFunc.invoke();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            log.info("Stop verticle: " + scriptName);
        }
    }
}