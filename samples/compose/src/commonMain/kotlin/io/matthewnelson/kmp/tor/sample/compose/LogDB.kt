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
package io.matthewnelson.kmp.tor.sample.compose

import app.cash.paging.*
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.paging3.QueryPagingSource
import io.matthewnelson.kmp.file.parentFile
import io.matthewnelson.kmp.file.path
import io.matthewnelson.kmp.file.resolve
import io.matthewnelson.kmp.tor.runtime.RuntimeEvent
import io.toxicity.sqlite.mc.driver.SQLiteMCDriver
import io.toxicity.sqlite.mc.driver.config.DatabasesDir
import io.toxicity.sqlite.mc.driver.config.encryption.Key
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// Nobody should do things like this... This is simply to show
// logs via LazyColumn + Paging
object LogDB {

    fun <E: RuntimeEvent<*>> insert(event: E, data: String) {
        DB_SCOPE.launch {
            @Suppress("UNCHECKED_CAST")
            DB.logQueries.logInsert(event as RuntimeEvent<Any>, data)
        }

        println(data)
    }

    val logItems: Flow<PagingData<LogDbo>> by lazy {
        Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                QueryPagingSource(
                    countQuery = DB.logQueries.logGetCount(),
                    transacter = DB.logQueries,
                    context = Dispatchers.IO,
                    queryProvider = DB.logQueries::logGet,
                )
            }
        ).flow
    }

    @Suppress("OPT_IN_USAGE")
    private val DB_SCOPE = CoroutineScope(context =
        CoroutineName("LogDBScope")
        + Dispatchers.IO.limitedParallelism(1)
        + SupervisorJob()
    )

    private val DRIVER: SQLiteMCDriver by lazy {
        val factory = SQLiteMCDriver.Factory(dbName = "logs.db", schema = LogsDatabase.Schema) {

            // Just using parent + "databases" of whatever tor's
            // work directory was set up with b/c laziness...
            val dbDir = runtimeEnvironment().workDirectory.parentFile!!.resolve("databases")

            // Wipe it on every application start
            dbDir.resolve(dbName).delete()

            filesystem(DatabasesDir(dbDir.path)) {
                encryption {
                    chaCha20 {
                        default {
                            kdfIter(500_000)
                        }
                    }
                }
            }
        }

        // Don't do this in prod...... This is just for simplicity
        // and to showcase another cool library for DB encryption.
        factory.createBlocking(Key.passphrase("Hello World"))
    }

    private val DB: LogsDatabase by lazy {
        LogsDatabase(
            driver = DRIVER,
            logDboAdapter = LogDbo.Adapter(
                typeAdapter = object : ColumnAdapter<RuntimeEvent<Any>, String> {
                    override fun encode(
                        value: RuntimeEvent<Any>,
                    ): String {
                        return value.name
                    }

                    override fun decode(
                        databaseValue: String,
                    ): RuntimeEvent<Any> {
                        @Suppress("UNCHECKED_CAST")
                        return RuntimeEvent.valueOf(databaseValue) as RuntimeEvent<Any>
                    }
                }
            )
        )
    }
}
