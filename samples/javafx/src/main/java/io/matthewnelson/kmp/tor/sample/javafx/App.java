/*
 * Copyright (c) 2024 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.matthewnelson.kmp.tor.sample.javafx;

import io.matthewnelson.kmp.file.KmpFile;
import io.matthewnelson.kmp.tor.resource.noexec.tor.ResourceLoaderTorNoExec;
import io.matthewnelson.kmp.tor.runtime.Action;
import io.matthewnelson.kmp.tor.runtime.RuntimeEvent;
import io.matthewnelson.kmp.tor.runtime.TorRuntime;
import io.matthewnelson.kmp.tor.runtime.core.OnEvent;
import io.matthewnelson.kmp.tor.runtime.core.OnSuccess;
import io.matthewnelson.kmp.tor.runtime.core.TorEvent;
import io.matthewnelson.kmp.tor.runtime.core.config.TorOption;
import io.matthewnelson.kmp.tor.runtime.core.config.builder.BuilderScopePort;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;

/**
 * Very bare-bones example for Java & JavaFX consumers. Would still
 * definitely look at the :samples:compose sample project.
 * */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Label label = new Label("See Logs");
        Scene scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);

        String rootDir = System.getProperty("user.home");
        if (rootDir == null || rootDir.isBlank()) {
            rootDir = KmpFile.SysTempDir.getPath();
        }

        File appDir = new File(rootDir, "kmp_tor_samples");
        appDir = new File(appDir, "javafx");

        TorRuntime.Environment env = TorRuntime.Environment.Builder(
            /* workDirectory  */ new File(appDir, "kmptor"),
            /* cacheDirectory */ new File(new File(appDir, "cache"), "kmptor"),

            // Alternatively, can utilize the kmp-tor-resource:resource-exec-tor dependency
            // which will use Process execution under the hood (instead of JNI).
            /* loader         */ ResourceLoaderTorNoExec::getOrCreate,

            /* block          */ b -> {
                // Configure further...
            }
        );

        env.debug = true;

        TorRuntime runtime = TorRuntime.Builder(env, b -> {
            // Pipe all logs to System.out
            RuntimeEvent.entries().forEach(event -> {
                b.observerStatic(
                    /* event    */ event,
                    /* executor */ OnEvent.Executor.Immediate.INSTANCE,
                    /* onEvent  */ data -> {
                        System.out.println(data.toString());
                    }
                );
            });

            b.config((c, environment) -> {

                // Totally not necessary, but as an example for Java. TorRuntime will
                // always define SocksPort 9050 and (when Action.StartDaemon is executed)
                // will re-assign to "auto" if defined port is unavailable.
                c.configure(TorOption.__SocksPort.INSTANCE, p -> {
                    p.auto();
                    p.flagsSocks(f -> {
                        f.OnionTrafficOnly = true;
                    });

//                    p.reassignable(false);
                });

                // As an example for declaring unix domain socket instead of TCP port
                try {
                    c.configure(TorOption.__SocksPort.INSTANCE, p -> {
                        p.unixSocket(new File(environment.workDirectory, "socks.sock"));
                    });
                } catch (UnsupportedOperationException ignore) {
                    // Not supported by the platform (i.e. Windows or using JDK 15-)...
                }

                c.configure(TorOption.__HTTPTunnelPort.INSTANCE, BuilderScopePort.HTTPTunnel::auto);
            });

            b.required(TorEvent.ERR.INSTANCE);
            b.required(TorEvent.WARN.INSTANCE);
        });

        // Not absolutely necessary if you don't want to. The ResourceLoaderTorNoExec.getOrCreate
        // function that was passed in for the TorRuntime.Environment.Builder.loader parameter sets
        // a shutdown hook by default (can be set to false if desired) which stops tor automatically
        // when the JVM shuts down.
        stage.setOnCloseRequest(windowEvent -> {
            runtime.enqueue(
                /* action    */ Action.StopDaemon,
                /* onFailure */ Throwable::printStackTrace,
                /* onSuccess */ success -> Platform.exit()
            );

            windowEvent.consume();
        });

        // Use synchronous extension function APIs instead of
        // enqueue callback APIs (available for Action and TorCmd).
//        try {
//            Action.startDaemonSync(runtime);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }

        // Use callback API which will handle things on TorRuntime's dedicated BG thread
        runtime.enqueue(
            /* action    */ Action.StartDaemon,
            /* onFailure */ Throwable::printStackTrace,
            /* onSuccess */ OnSuccess.noOp()
        );
    }

    public static void main(String[] args) {
        launch();
    }
}
